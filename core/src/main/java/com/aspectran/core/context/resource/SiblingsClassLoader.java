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

import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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

import static com.aspectran.core.util.ClassUtils.CLASS_FILE_SUFFIX;
import static com.aspectran.core.util.ClassUtils.PACKAGE_SEPARATOR_CHAR;
import static com.aspectran.core.util.ResourceUtils.CLASSPATH_URL_PREFIX;
import static com.aspectran.core.util.ResourceUtils.FILE_URL_PREFIX;
import static com.aspectran.core.util.ResourceUtils.REGULAR_FILE_SEPARATOR_CHAR;

/**
 * Specialized class loader for Aspectran.
 */
public class SiblingsClassLoader extends ClassLoader {

    private final int id;

    private final SiblingsClassLoader root;

    private final String resourceLocation;

    private final ResourceManager resourceManager;

    private final List<SiblingsClassLoader> children = new LinkedList<>();

    private final boolean firstborn;

    private int reloadedCount;

    private Set<String> excludeClassNames;

    private Set<String> excludePackageNames;

    public SiblingsClassLoader() {
        this(ClassUtils.getDefaultClassLoader());
    }

    public SiblingsClassLoader(ClassLoader parent) {
        super(parent);

        this.id = 1000;
        this.root = this;
        this.firstborn = true;
        this.resourceLocation = null;
        this.resourceManager = new LocalResourceManager(this);
    }

    public SiblingsClassLoader(String resourceLocation) throws InvalidResourceException {
        this(resourceLocation, ClassUtils.getDefaultClassLoader());
    }

    public SiblingsClassLoader(String resourceLocation, ClassLoader parent) throws InvalidResourceException {
        super(parent);

        this.id = 1000;
        this.root = this;
        this.firstborn = true;
        this.resourceLocation = resourceLocation;
        this.resourceManager = new LocalResourceManager(resourceLocation, this);
    }

    public SiblingsClassLoader(String[] resourceLocations) throws InvalidResourceException {
        this(resourceLocations, ClassUtils.getDefaultClassLoader());
    }

    public SiblingsClassLoader(String[] resourceLocations, ClassLoader parent) throws InvalidResourceException {
        this(parent);

        SiblingsClassLoader acl = this;
        for (String resourceLocation : resourceLocations) {
            acl = acl.createChild(resourceLocation);
        }
    }

    protected SiblingsClassLoader(String resourceLocation, SiblingsClassLoader parent) throws InvalidResourceException {
        super(parent);

        int numOfChildren = parent.addChild(this);

        this.id = (Math.abs(parent.getId() / 1000) + 1) * 1000 + numOfChildren;
        this.root = parent.getRoot();
        this.firstborn = (numOfChildren == 1);
        this.resourceLocation = resourceLocation;
        this.resourceManager = new LocalResourceManager(resourceLocation, this);
    }

    private SiblingsClassLoader(ClassLoader parent, SiblingsClassLoader latest) {
        super(parent);

        int numOfChildren = latest.addChild(this);

        this.id = (Math.abs(latest.getId() / 1000) + 1) * 1000 + numOfChildren;
        this.root = latest;
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
            for (String resourceLocation : resourceLocations) {
                if (resourceLocation != null && !resourceLocation.isEmpty()) {
                    scl = scl.createChild(resourceLocation);
                }
            }
        }
    }

    public SiblingsClassLoader addGeneration(ClassLoader classLoader) {
        SiblingsClassLoader latest = root;
        while (latest.hasChildren()) {
            latest = latest.getChildren().get(0);
        }
        return new SiblingsClassLoader(classLoader, latest);
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
                if (!isExcludePackage(className)) {
                    if (excludeClassNames == null) {
                        excludeClassNames = new HashSet<>();
                    }
                    excludeClassNames.add(className);
                }
            }
        }
    }

    private boolean isExcluded(String className) {
        return (isExcludePackage(className) || isExcludeClass(className));
    }

    private boolean isExcludePackage(String className) {
        if (excludePackageNames != null) {
            for (String packageName : excludePackageNames) {
                if (className.startsWith(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isExcludeClass(String className) {
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
            // First check if the class is already loaded
            Class<?> c = findLoadedClass(name);
            if (c != null) {
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }

            ClassLoader system = getSystemClassLoader();

            // Try loading the class with the system class loader
            try {
                c = system.loadClass(name);
                if (c != null) {
                    if (resolve) {
                        resolveClass(c);
                    }
                    return c;
                }
            } catch (ClassNotFoundException e) {
                // ignore
            }

            // Search local repositories
            try {
                c = findClass(name);
                if (c != null) {
                    if (resolve) {
                        resolveClass(c);
                    }
                    return c;
                }
            } catch (ClassNotFoundException e) {
                // ignore
            }

            // Delegate to parent unconditionally
            ClassLoader loader = root.getParent();
            if (loader == null) {
                loader = system;
            }
            try {
                c = Class.forName(name, false, loader);
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }

        throw new ClassNotFoundException(name);
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

        String resourceName = classNameToResourceName(className);
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
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = input.read(buffer)) >= 0) {
                output.write(buffer, 0, len);
            }
            input.close();
            return output.toByteArray();
        } catch (IOException e) {
            throw new InvalidResourceException("Unable to read class file: " + url, e);
        }
    }

    @Override
    public URL getResource(String name) {
        // Search local repositories
        URL url = findResource(name);
        if (url == null) {
            url = getParent().getResource(name);
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
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("id", id);
        if (getParent() instanceof SiblingsClassLoader) {
            tsb.append("parent", ((SiblingsClassLoader)getParent()).getId());
        } else {
            tsb.append("parent", getParent().getClass().getName());
        }
        tsb.append("root", this == root);
        tsb.append("firstborn", firstborn);
        tsb.append("resourceLocation", resourceLocation);
        tsb.append("numberOfResource", resourceManager != null ? resourceManager.getNumberOfResources() : '?');
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

    public static String resourceNameToClassName(String resourceName) {
        String className = resourceName.substring(0, resourceName.length() - CLASS_FILE_SUFFIX.length());
        className = className.replace(REGULAR_FILE_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
        return className;
    }

    public static String classNameToResourceName(String className) {
        return className.replace(PACKAGE_SEPARATOR_CHAR, REGULAR_FILE_SEPARATOR_CHAR)
                + CLASS_FILE_SUFFIX;
    }

    public static String packageNameToResourceName(String packageName) {
        String resourceName = packageName.replace(PACKAGE_SEPARATOR_CHAR, REGULAR_FILE_SEPARATOR_CHAR);
        if (StringUtils.endsWith(resourceName, REGULAR_FILE_SEPARATOR_CHAR)) {
            resourceName = resourceName.substring(0, resourceName.length() - 1);
        }
        return resourceName;
    }

    public static String[] checkResourceLocations(String[] resourceLocations, String basePath)
            throws InvalidResourceException {
        if (resourceLocations == null) {
            return null;
        }

        ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
        for (int i = 0; i < resourceLocations.length; i++) {
            if (resourceLocations[i].startsWith(CLASSPATH_URL_PREFIX)) {
                String path = resourceLocations[i].substring(CLASSPATH_URL_PREFIX.length());
                URL url = classLoader.getResource(path);
                if (url == null) {
                    throw new InvalidResourceException("Class path resource [" + resourceLocations[i] +
                            "] cannot be resolved to URL because it does not exist");
                }
                resourceLocations[i] = url.getFile();
            } else if (resourceLocations[i].startsWith(FILE_URL_PREFIX)) {
                try {
                    URL url = new URL(resourceLocations[i]);
                    resourceLocations[i] = url.getFile();
                } catch (MalformedURLException e) {
                    throw new InvalidResourceException("Resource location [" + resourceLocations[i] +
                            "] is neither a URL not a well-formed file path");
                }
            } else {
                if (basePath != null) {
                    try {
                        File f = new File(basePath, resourceLocations[i]);
                        resourceLocations[i] = f.getCanonicalPath();
                    } catch (IOException e) {
                        throw new InvalidResourceException("Invalid resource location: " + resourceLocations[i], e);
                    }
                }
            }
            resourceLocations[i] = resourceLocations[i].replace(File.separatorChar, REGULAR_FILE_SEPARATOR_CHAR);
            if (StringUtils.endsWith(resourceLocations[i], REGULAR_FILE_SEPARATOR_CHAR)) {
                resourceLocations[i] = resourceLocations[i].substring(0, resourceLocations[i].length() - 1);
            }
        }

        String resourceLocation = null;
        int cleared = 0;

        try {
            for (int i = 0; i < resourceLocations.length - 1; i++) {
                if (resourceLocations[i] != null) {
                    resourceLocation = resourceLocations[i];
                    File f1 = new File(resourceLocations[i]);
                    String l1 = f1.getCanonicalPath();
                    for (int j = i + 1; j < resourceLocations.length; j++) {
                        if (resourceLocations[j] != null) {
                            resourceLocation = resourceLocations[j];
                            File f2 = new File(resourceLocations[j]);
                            String l2 = f2.getCanonicalPath();
                            if (l1.equals(l2)) {
                                resourceLocations[j] = null;
                                cleared++;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new InvalidResourceException("Invalid resource location: " + resourceLocation, e);
        }

        if (cleared > 0) {
            List<String> list = new ArrayList<>(resourceLocations.length);
            for (String r : resourceLocations) {
                if (r != null) {
                    list.add(r);
                }
            }
            return list.toArray(new String[0]);
        } else {
            return resourceLocations;
        }
    }

}
