/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.resource;

import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import static com.aspectran.utils.ClassUtils.PACKAGE_SEPARATOR_CHAR;

/**
 * Specialized class loader for Aspectran.
 */
public final class SiblingClassLoader extends ClassLoader {

    private final int id;

    private final SiblingClassLoader root;

    private final boolean firstborn;

    private final String resourceLocation;

    private final ResourceManager resourceManager;

    private final List<SiblingClassLoader> siblings = new LinkedList<>();

    private Set<String> excludeClassNames;

    private Set<String> excludePackageNames;

    private int reloadedCount;

    public SiblingClassLoader() throws InvalidResourceException {
        this(null, (ClassLoader)null);
    }

    public SiblingClassLoader(String name) throws InvalidResourceException {
        this(name, (ClassLoader)null);
    }

    public SiblingClassLoader(String name, ClassLoader parent) throws InvalidResourceException {
        super(name, parent != null ? parent : ClassUtils.getDefaultClassLoader());

        this.id = 1000;
        this.root = this;
        this.firstborn = true;
        this.resourceLocation = null;
        this.resourceManager = new LocalResourceManager(this);
    }

    public SiblingClassLoader(String[] resourceLocations) throws InvalidResourceException {
        this(null, null, resourceLocations);
    }

    public SiblingClassLoader(String name, String[] resourceLocations) throws InvalidResourceException {
        this(name, null, resourceLocations);
    }

    public SiblingClassLoader(ClassLoader parent, String[] resourceLocations) throws InvalidResourceException {
        this(null, parent, resourceLocations);
    }

    public SiblingClassLoader(String name, ClassLoader parent, String[] resourceLocations)
            throws InvalidResourceException {
        this(name, parent != null ? parent : ClassUtils.getDefaultClassLoader());
        if (resourceLocations != null) {
            createSibling(resourceLocations);
        }
    }

    private SiblingClassLoader(String name, @NonNull SiblingClassLoader parent, String resourceLocation)
            throws InvalidResourceException {
        super(name, parent);

        int numOfSiblings = parent.addSibling(this);

        this.id = (Math.abs(parent.getId() / 1000) + 1) * 1000 + numOfSiblings;
        this.root = parent.getRoot();
        this.firstborn = (numOfSiblings == 1);
        this.resourceLocation = resourceLocation;
        this.resourceManager = new LocalResourceManager(this, resourceLocation);
    }

    void joinSibling(String resourceLocation) throws InvalidResourceException {
        SiblingClassLoader parent = (SiblingClassLoader)getParent();
        parent.createSibling(resourceLocation);
    }

    private void createSibling(@NonNull String[] resourceLocations) throws InvalidResourceException {
        SiblingClassLoader scl = this;
        for (String resourceLocation : resourceLocations) {
            if (resourceLocation != null && !resourceLocation.isEmpty()) {
                scl = scl.createSibling(resourceLocation);
            }
        }
    }

    @NonNull
    private SiblingClassLoader createSibling(String resourceLocation) throws InvalidResourceException {
        if (!firstborn) {
            throw new IllegalStateException("Only the firstborn can create other siblings");
        }
        return new SiblingClassLoader(getName(), this, resourceLocation);
    }

    /**
     * Adds packages that this ClassLoader should not handle.
     * Any class whose fully-qualified name starts with the name registered here will be handled
     * by the parent ClassLoader in the usual fashion.
     * @param packageNames package names that we be compared against fully qualified package names to exclude
     */
    public void excludePackage(String... packageNames) {
        if (packageNames != null && packageNames.length > 0) {
            for (String packageName : packageNames) {
                if (excludePackageNames == null) {
                    excludePackageNames = new HashSet<>();
                }
                excludePackageNames.add(packageName + PACKAGE_SEPARATOR_CHAR);
            }
        } else {
            excludePackageNames = null;
        }
    }

    /**
     * Adds classes that this ClassLoader should not handle.
     * Any class whose fully-qualified name starts with the name registered here will be handled
     * by the parent ClassLoader in the usual fashion.
     * @param classNames class names that we be compared against fully qualified class names to exclude
     */
    public void excludeClass(String... classNames) {
        if (classNames != null && classNames.length > 0) {
            for (String className : classNames) {
                if (!isExcludedPackage(className)) {
                    if (excludeClassNames == null) {
                        excludeClassNames = new HashSet<>();
                    }
                    excludeClassNames.add(className);
                }
            }
        } else {
            excludeClassNames = null;
        }
    }

    private boolean isExcluded(String className) {
        return (isExcludedPackage(className) || isExcludedClass(className));
    }

    private boolean isExcludedPackage(String className) {
        if (excludePackageNames != null) {
            for (String packageName : excludePackageNames) {
                if (className.startsWith(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isExcludedClass(String className) {
        return (excludeClassNames != null && excludeClassNames.contains(className));
    }

    public int getId() {
        return id;
    }

    public SiblingClassLoader getRoot() {
        return root;
    }

    public boolean isRoot() {
        return (this == root);
    }

    public List<SiblingClassLoader> getSiblings() {
        return siblings;
    }

    private int addSibling(SiblingClassLoader sibling) {
        synchronized (siblings) {
            siblings.add(sibling);
            return siblings.size();
        }
    }

    public boolean hasSiblings() {
        return !siblings.isEmpty();
    }

    public boolean isFirstborn() {
        return firstborn;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public String getResourceLocation() {
        return resourceLocation;
    }

    public synchronized void reload() throws InvalidResourceException {
        reload(root);
    }

    private void reload(@NonNull SiblingClassLoader self) throws InvalidResourceException {
        self.increaseReloadedCount();

        if (self.getResourceManager() != null) {
            self.getResourceManager().reset();
        }

        SiblingClassLoader firstborn = null;
        List<SiblingClassLoader> siblings = new ArrayList<>();
        for (SiblingClassLoader sibling : self.getSiblings()) {
            if (sibling.isFirstborn()) {
                firstborn = sibling;
            } else {
                siblings.add(sibling);
            }
        }
        if (!siblings.isEmpty()) {
            self.leave(siblings);
        }
        if (firstborn != null) {
            reload(firstborn);
        }
    }

    private void increaseReloadedCount() {
        reloadedCount++;
    }

    private void leave(@NonNull List<SiblingClassLoader> siblings) {
        for (SiblingClassLoader sibling : siblings) {
            ResourceManager rm = sibling.getResourceManager();
            if (rm != null) {
                rm.release();
            }
            this.siblings.remove(sibling);
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    // First, search from local repositories
                    c = findClass(name);
                } catch (ClassNotFoundException e) {
                    // ignored
                }
                if (c == null) {
                    // If not found locally, from parent classloader
                    ClassLoader parent = root.getParent();
                    if (parent != null) {
                        c = Class.forName(name, false, parent);
                    } else {
                        // Try loading the class with the system class loader
                        c = findSystemClass(name);
                    }
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        Objects.requireNonNull(name);
        try  {
            byte[] classData = loadClassData(name);
            if (classData != null) {
                return defineClass(name, classData, 0, classData.length);
            } else {
                throw new ClassNotFoundException(name);
            }
        } catch (InvalidResourceException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    @Nullable
    private byte[] loadClassData(String className) throws InvalidResourceException {
        if (isExcluded(className)) {
            return null;
        }

        String resourceName = ResourceManager.classNameToResourceName(className);
        Enumeration<URL> res = ResourceManager.findResources(resourceName, getAllSiblings());
        URL url = null;
        if (res.hasMoreElements()) {
            url = res.nextElement();
        }
        if (url == null) {
            return null;
        }

        try {
            URLConnection connection = url.openConnection();
            BufferedInputStream input = new BufferedInputStream(connection.getInputStream());
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            int i;
            while ((i = input.read()) != -1) {
                output.write(i);
            }
            input.close();
            byte[] classData = output.toByteArray();
            output.close();
            return classData;
        } catch (IOException e) {
            throw new InvalidResourceException("Unable to read class file: " + url, e);
        }
    }

    @Override
    public URL getResource(String name) {
        // Search from local repositories
        URL url = findResource(name);
        if (url == null) {
            ClassLoader parent = root.getParent();
            if (parent != null) {
                url = parent.getResource(name);
            } else {
                url = getSystemClassLoader().getResource(name);
            }
        }
        return url;
    }

    @Override
    @NonNull
    public Enumeration<URL> getResources(String name) throws IOException {
        Objects.requireNonNull(name);
        Enumeration<URL> parentResources = null;
        ClassLoader parent = root.getParent();
        if (parent != null) {
            parentResources = parent.getResources(name);
        }
        return ResourceManager.findResources(name, getAllSiblings(), parentResources);
    }

    @Override
    public URL findResource(String name) {
        Objects.requireNonNull(name);
        URL url = null;
        Enumeration<URL> res = ResourceManager.findResources(name, getAllSiblings());
        if (res.hasMoreElements()) {
            url = res.nextElement();
        }
        return url;
    }

    @Override
    @NonNull
    public Enumeration<URL> findResources(String name) {
        Objects.requireNonNull(name);
        return ResourceManager.findResources(name, getAllSiblings());
    }

    @NonNull
    public Enumeration<URL> getAllResources() {
        return ResourceManager.findResources(getAllSiblings());
    }

    @NonNull
    public Iterator<SiblingClassLoader> getAllSiblings() {
        return getSiblings(root);
    }

    @Override
    public String toString() {
        String thisName = ObjectUtils.simpleIdentityToString(this);
        ToStringBuilder tsb = new ToStringBuilder(thisName);
        tsb.append("id", id);
        tsb.append("name", getName());
        if (getParent() instanceof SiblingClassLoader parent) {
            tsb.append("parent", parent.getId());
        } else if (getParent() != null) {
            tsb.append("parent", ObjectUtils.simpleIdentityToString(getParent()));
        }
        tsb.append("root", this == root);
        tsb.append("firstborn", firstborn);
        tsb.append("resourceLocation", resourceLocation);
        tsb.append("numberOfResources", resourceManager.getNumberOfResources());
        tsb.appendSize("numberOfSiblings", siblings);
        tsb.append("reloadedCount", reloadedCount);
        return tsb.toString();
    }

    @NonNull
    private static Iterator<SiblingClassLoader> getSiblings(@NonNull final SiblingClassLoader root) {
        return new Iterator<>() {
            private SiblingClassLoader next = root;
            private Iterator<SiblingClassLoader> iter = root.getSiblings().iterator();
            private SiblingClassLoader first;

            @Override
            public boolean hasNext() {
                return (next != null);
            }

            @Override
            public SiblingClassLoader next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                SiblingClassLoader current = next;
                if (iter.hasNext()) {
                    next = iter.next();
                    if (first == null) {
                        first = next;
                    }
                } else {
                    if (first != null) {
                        iter = first.getSiblings().iterator();
                        if (iter.hasNext()) {
                            next = iter.next();
                            first = next;
                        } else {
                            next = null;
                        }
                    } else {
                        next = null;
                    }
                }
                return current;
            }
        };
    }

}
