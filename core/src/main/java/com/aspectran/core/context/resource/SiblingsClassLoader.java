/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.util.ToStringBuilder;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import static com.aspectran.core.util.ClassUtils.PACKAGE_SEPARATOR_CHAR;

/**
 * Specialized class loader for Aspectran.
 */
public class SiblingsClassLoader extends ClassLoader {

    private final int id;

    private final SiblingsClassLoader root;

    private final boolean firstborn;

    private final String resourceLocation;

    private final ResourceManager resourceManager;

    private final List<SiblingsClassLoader> children = new LinkedList<>();

    private Set<String> excludeClassNames;

    private Set<String> excludePackageNames;

    private int reloadedCount;

    public SiblingsClassLoader() throws InvalidResourceException {
        this(SiblingsClassLoader.class.getClassLoader());
    }

    public SiblingsClassLoader(ClassLoader parent) throws InvalidResourceException {
        super(parent);

        this.id = 1000;
        this.root = this;
        this.firstborn = true;
        this.resourceLocation = null;
        this.resourceManager = new LocalResourceManager(this);
    }

    public SiblingsClassLoader(String resourceLocation) throws InvalidResourceException {
        this(resourceLocation, SiblingsClassLoader.class.getClassLoader());
    }

    public SiblingsClassLoader(String resourceLocation, ClassLoader parent) throws InvalidResourceException {
        super(parent);

        this.id = 1000;
        this.root = this;
        this.firstborn = true;
        this.resourceLocation = resourceLocation;
        this.resourceManager = new LocalResourceManager(this, resourceLocation);
    }

    public SiblingsClassLoader(String[] resourceLocations) throws InvalidResourceException {
        this(resourceLocations, SiblingsClassLoader.class.getClassLoader());
    }

    public SiblingsClassLoader(String[] resourceLocations, ClassLoader parent) throws InvalidResourceException {
        this(parent);

        SiblingsClassLoader scl = this;
        if (resourceLocations != null) {
            for (String resourceLocation : resourceLocations) {
                if (resourceLocation != null && !resourceLocation.isEmpty()) {
                    scl = scl.createChild(resourceLocation);
                }
            }
        }
    }

    protected SiblingsClassLoader(String resourceLocation, SiblingsClassLoader parent) throws InvalidResourceException {
        super(parent);

        if (parent == null) {
            throw new IllegalArgumentException("parent must not be null");
        }

        int numOfChildren = parent.addChild(this);

        this.id = (Math.abs(parent.getId() / 1000) + 1) * 1000 + numOfChildren;
        this.root = parent.getRoot();
        this.firstborn = (numOfChildren == 1);
        this.resourceLocation = resourceLocation;
        this.resourceManager = new LocalResourceManager(this, resourceLocation);
    }

    private SiblingsClassLoader(ClassLoader parent, SiblingsClassLoader youngest) throws InvalidResourceException {
        super(parent);

        if (youngest == null) {
            throw new IllegalArgumentException("youngest must not be null");
        }

        int numOfChildren = youngest.addChild(this);

        this.id = (Math.abs(youngest.getId() / 1000) + 1) * 1000 + numOfChildren;
        this.root = youngest;
        this.firstborn = (numOfChildren == 1);
        this.resourceLocation = null;
        this.resourceManager = new LocalResourceManager(this);
    }

    public void setResourceLocations(String... resourceLocations) throws InvalidResourceException {
        synchronized (children) {
            if (!children.isEmpty()) {
                children.clear();
            }

            SiblingsClassLoader scl = this;
            if (resourceLocations != null) {
                for (String resourceLocation : resourceLocations) {
                    if (resourceLocation != null && !resourceLocation.isEmpty()) {
                        scl = scl.createChild(resourceLocation);
                    }
                }
            }
        }
    }

    public SiblingsClassLoader addGeneration(ClassLoader classLoader) throws InvalidResourceException {
        SiblingsClassLoader youngest = root;
        while (youngest.hasChildren()) {
            youngest = youngest.getChildren().get(0);
        }
        return new SiblingsClassLoader(classLoader, youngest);
    }

    protected SiblingsClassLoader joinSibling(String resourceLocation) throws InvalidResourceException {
        SiblingsClassLoader parent = (SiblingsClassLoader)getParent();
        return parent.createChild(resourceLocation);
    }

    private SiblingsClassLoader createChild(String resourceLocation) throws InvalidResourceException {
        if (!firstborn) {
            throw new IllegalStateException("Only the first among siblings can create a child");
        }
        return new SiblingsClassLoader(resourceLocation, this);
    }

    /**
     * Adds packages that this ClassLoader should not handle.
     * Any class whose fully-qualified name starts with the name registered here will be handled
     * by the parent ClassLoader in the usual fashion.
     * @param packageNames package names that we be compared against fully qualified package names to exclude
     */
    public void excludePackage(String... packageNames) {
        if (packageNames == null) {
            excludePackageNames = null;
        } else {
            for (String packageName : packageNames) {
                if (excludePackageNames == null) {
                    excludePackageNames = new HashSet<>();
                }
                excludePackageNames.add(packageName + PACKAGE_SEPARATOR_CHAR);
            }
        }
    }

    /**
     * Adds classes that this ClassLoader should not handle.
     * Any class whose fully-qualified name starts with the name registered here will be handled
     * by the parent ClassLoader in the usual fashion.
     * @param classNames class names that we be compared against fully qualified class names to exclude
     */
    public void excludeClass(String... classNames) {
        if (classNames == null) {
            excludeClassNames = null;
        } else {
            for (String className : classNames) {
                if (!isExcludedPackage(className)) {
                    if (excludeClassNames == null) {
                        excludeClassNames = new HashSet<>();
                    }
                    excludeClassNames.add(className);
                }
            }
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

    public SiblingsClassLoader getRoot() {
        return root;
    }

    public boolean isRoot() {
        return (this == root);
    }

    public List<SiblingsClassLoader> getChildren() {
        return children;
    }

    private int addChild(SiblingsClassLoader child) {
        synchronized (children) {
            children.add(child);
            return children.size();
        }
    }

    public boolean hasChildren() {
        return !children.isEmpty();
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

    private void reload(SiblingsClassLoader self) throws InvalidResourceException {
        self.increaseReloadedCount();

        if (self.getResourceManager() != null) {
            self.getResourceManager().reset();
        }

        SiblingsClassLoader firstborn = null;
        List<SiblingsClassLoader> siblings = new ArrayList<>();
        for (SiblingsClassLoader child : self.getChildren()) {
            if (child.isFirstborn()) {
                firstborn = child;
            } else {
                siblings.add(child);
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

    private void leave(List<SiblingsClassLoader> siblings) {
        for (SiblingsClassLoader sibling : siblings) {
            ResourceManager rm = sibling.getResourceManager();
            if (rm != null) {
                rm.release();
            }
            children.remove(sibling);
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    ClassLoader parent = root.getParent();
                    if (parent != null) {
                        c = Class.forName(name, false, parent);
                    } else {
                        // Try loading the class with the system class loader
                        c = findSystemClass(name);
                    }
                } catch (ClassNotFoundException e) {
                    // ClassNotFoundException thrown if class not found
                    // from the non-null parent class loader
                }
                if (c == null) {
                    // Search from local repositories
                    c = findClass(name);
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

    private byte[] loadClassData(String className) throws InvalidResourceException {
        if (isExcluded(className)) {
            return null;
        }

        String resourceName = ResourceManager.classNameToResourceName(className);
        Enumeration<URL> res = ResourceManager.getResources(getAllMembers(), resourceName);
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
        ClassLoader parent = root.getParent();
        URL url;
        if (parent != null) {
            url = parent.getResource(name);
        } else {
            url = getSystemClassLoader().getResource(name);
        }
        if (url == null) {
            // Search from local repositories
            url = findResource(name);
        }
        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Objects.requireNonNull(name);
        Enumeration<URL> parentResources = null;
        ClassLoader parent = root.getParent();
        if (parent != null) {
            parentResources = parent.getResources(name);
        }
        return ResourceManager.getResources(getAllMembers(), name, parentResources);
    }

    @Override
    public URL findResource(String name) {
        Objects.requireNonNull(name);
        URL url = null;
        Enumeration<URL> res = ResourceManager.getResources(getAllMembers(), name);
        if (res.hasMoreElements()) {
            url = res.nextElement();
        }
        return url;
    }

    @Override
    public Enumeration<URL> findResources(String name) {
        Objects.requireNonNull(name);
        Set<URL> urls = new LinkedHashSet<>();
        Enumeration<URL> res = ResourceManager.getResources(getAllMembers(), name);
        if (res.hasMoreElements()) {
            urls.add(res.nextElement());
        }
        return Collections.enumeration(urls);
    }

    public Iterator<SiblingsClassLoader> getAllMembers() {
        return getMembers(root);
    }

    public Enumeration<URL> getAllResources() {
        return ResourceManager.getResources(getAllMembers());
    }

    @Override
    public String toString() {
        String name = getClass().getSimpleName() + '@' + Integer.toString(hashCode(), 16);
        ToStringBuilder tsb = new ToStringBuilder(name);
        tsb.append("id", id);
        if (getParent() instanceof SiblingsClassLoader) {
            tsb.append("parent", ((SiblingsClassLoader)getParent()).getId());
        } else {
            tsb.append("parent", getParent().getClass().getName());
        }
        tsb.append("root", this == root);
        tsb.append("firstborn", firstborn);
        tsb.append("resourceLocation", resourceLocation);
        tsb.append("numberOfResources", resourceManager != null ? resourceManager.getNumberOfResources() : '?');
        tsb.appendSize("numberOfChildren", children);
        tsb.append("reloadedCount", reloadedCount);
        return tsb.toString();
    }

    public static Iterator<SiblingsClassLoader> getMembers(final SiblingsClassLoader root) {
        return new Iterator<>() {
            private SiblingsClassLoader next = root;
            private Iterator<SiblingsClassLoader> children = root.getChildren().iterator();
            private SiblingsClassLoader firstChild;

            @Override
            public boolean hasNext() {
                return (next != null);
            }

            @Override
            public SiblingsClassLoader next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }

                SiblingsClassLoader current = next;
                if (children.hasNext()) {
                    next = children.next();
                    if (firstChild == null) {
                        firstChild = next;
                    }
                } else {
                    if (firstChild != null) {
                        children = firstChild.getChildren().iterator();
                        if (children.hasNext()) {
                            next = children.next();
                            firstChild = next;
                        } else {
                            next = null;
                        }
                    } else {
                        next = null;
                    }
                }
                return current;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }

}
