/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.aspectran.core.util.ClassUtils.CLASS_FILE_SUFFIX;
import static com.aspectran.core.util.ClassUtils.PACKAGE_SEPARATOR_CHAR;
import static com.aspectran.core.util.ResourceUtils.CLASSPATH_URL_PREFIX;
import static com.aspectran.core.util.ResourceUtils.FILE_URL_PREFIX;
import static com.aspectran.core.util.ResourceUtils.REGULAR_FILE_SEPARATOR_CHAR;
import static com.aspectran.core.util.ResourceUtils.URL_PROTOCOL_JAR;

/**
 * Specialized aspectran class loader.
 */
public class AspectranClassLoader extends ClassLoader {

    private static final Log log = LogFactory.getLog(AspectranClassLoader.class);

    private final int id;

    private final AspectranClassLoader root;

    private final String resourceLocation;

    private final ResourceManager resourceManager;

    private final List<AspectranClassLoader> children = new LinkedList<>();

    private final boolean firstborn;

    private int reloadedCount;

    private Set<String> excludeClassNames;

    private Set<String> excludePackageNames;

    public AspectranClassLoader() {
        this(getDefaultClassLoader());
    }

    public AspectranClassLoader(ClassLoader parent) {
        super(parent);

        this.id = 1000;
        this.root = this;
        this.firstborn = true;
        this.resourceLocation = null;
        this.resourceManager = new LocalResourceManager(this);

        if (log.isTraceEnabled()) {
            log.trace("Root AspectranClassLoader " + this);
        }
    }

    public AspectranClassLoader(String resourceLocation) throws InvalidResourceException {
        this(resourceLocation, getDefaultClassLoader());
    }

    public AspectranClassLoader(String resourceLocation, ClassLoader parent) throws InvalidResourceException {
        super(parent);

        this.id = 1000;
        this.root = this;
        this.firstborn = true;
        this.resourceLocation = resourceLocation;
        this.resourceManager = new LocalResourceManager(resourceLocation, this);

        if (log.isTraceEnabled()) {
            log.trace("Root AspectranClassLoader " + this);
        }
    }

    public AspectranClassLoader(String[] resourceLocations) throws InvalidResourceException {
        this(resourceLocations, getDefaultClassLoader());
    }

    public AspectranClassLoader(String[] resourceLocations, ClassLoader parent) throws InvalidResourceException {
        this(parent);

        AspectranClassLoader acl = this;
        for (String resourceLocation : resourceLocations) {
            acl = acl.createChild(resourceLocation);
        }
    }

    protected AspectranClassLoader(String resourceLocation, AspectranClassLoader parent) throws InvalidResourceException {
        super(parent);

        int numOfChildren = parent.addChild(this);

        this.id = (Math.abs(parent.getId() / 1000) + 1) * 1000 + numOfChildren;
        this.root = parent.getRoot();
        this.firstborn = (numOfChildren == 1);
        this.resourceLocation = resourceLocation;
        this.resourceManager = new LocalResourceManager(resourceLocation, this);
    }

    private AspectranClassLoader(ClassLoader parent, AspectranClassLoader latest) {
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

            AspectranClassLoader acl = this;
            for (String resourceLocation : resourceLocations) {
                if (resourceLocation != null && !resourceLocation.isEmpty()) {
                    acl = acl.createChild(resourceLocation);
                }
            }
        }
    }

    public AspectranClassLoader addGeneration(ClassLoader classLoader) {
        AspectranClassLoader latest = root;
        while (latest.hasChildren()) {
            latest = latest.getChildren().get(0);
        }
        return new AspectranClassLoader(classLoader, latest);
    }

    protected AspectranClassLoader joinBrother(String resourceLocation) throws InvalidResourceException {
        AspectranClassLoader parent = (AspectranClassLoader)getParent();
        return parent.createChild(resourceLocation);
    }

    private AspectranClassLoader createChild(String resourceLocation) throws InvalidResourceException {
        if (!firstborn) {
            throw new IllegalStateException("Only the firstborn AspectranClassLoader can create a child");
        }

        AspectranClassLoader newChild = new AspectranClassLoader(resourceLocation, this);

        if (log.isTraceEnabled()) {
            log.trace("New Child AspectranClassLoader " + newChild);
        }

        return newChild;
    }

    /**
     * Adds packages that this ClassLoader should not handle.
     * Any class whose fully-qualified name starts with the name registered here will be handled
     * by the parent ClassLoader in the usual fashion.
     *
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
     *
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

    public AspectranClassLoader getRoot() {
        return root;
    }

    public boolean isRoot() {
        return (this == root);
    }

    public List<AspectranClassLoader> getChildren() {
        return children;
    }

    private int addChild(AspectranClassLoader child) {
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

    private void reload(AspectranClassLoader self) throws InvalidResourceException {
        self.increaseReloadedCount();

        if (log.isTraceEnabled()) {
            log.trace("Reloading AspectranClassLoader " + self);
        }

        if (self.getResourceManager() != null) {
            self.getResourceManager().reset();
        }

        AspectranClassLoader firstborn = null;
        List<AspectranClassLoader> brothers = new ArrayList<>();
        for (AspectranClassLoader child : self.getChildren()) {
            if (child.isFirstborn()) {
                firstborn = child;
            } else {
                brothers.add(child);
            }
        }
        if (!brothers.isEmpty()) {
            self.leave(brothers);
        }
        if (firstborn != null) {
            reload(firstborn);
        }
    }

    private void increaseReloadedCount() {
        reloadedCount++;
    }

    private void leave(List<AspectranClassLoader> brothers) {
        for (AspectranClassLoader acl : brothers) {
            if (log.isTraceEnabled()) {
                log.trace("Remove a child AspectranClassLoader " + acl);
            }

            ResourceManager rm = acl.getResourceManager();
            if (rm != null) {
                rm.release();
            }
            children.remove(acl);
        }
    }

    public URL[] extractResources() {
        Enumeration<URL> res = ResourceManager.getResources(getAllMembers());
        List<URL> resources = new LinkedList<>();
        URL url;
        while (res.hasMoreElements()) {
            url = res.nextElement();
            if (!URL_PROTOCOL_JAR.equals(url.getProtocol())) {
                resources.add(url);
            }
        }
        return resources.toArray(new URL[0]);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> parentResources = null;
        ClassLoader parent = root.getParent();
        if (parent != null) {
            parentResources = parent.getResources(name);
        }
        return ResourceManager.getResources(getAllMembers(), name, parentResources);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            if (log.isTraceEnabled()) {
                log.trace("loadClass(" + name + ", " + resolve + ") from " + this);
            }

            ClassLoader system = getSystemClassLoader();

            // First check if the class is already loaded
            Class<?> c = findLoadedClass(name);
            if (c != null) {
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }

            // Try loading the class with the system class loader, to prevent
            // the webapp from overriding J2SE classes
            try {
                c = system.loadClass(name);
                if (c != null) {
                    if (log.isTraceEnabled()) {
                        log.trace("- Loading class " + name + " from system");
                    }
                    if (resolve) {
                        resolveClass(c);
                    }
                    return c;
                }
            } catch (ClassNotFoundException e) {
                // ignore
            }

            // Permission to access this class when using a SecurityManager
            try {
                // The policy file may have been modified to adjust
                // permissions, so we're reloading it when loading or
                // reloading a Context
                Policy policy = Policy.getPolicy();
                policy.refresh();
            } catch (AccessControlException e) {
                // Some policy files may restrict this, even for the core,
                // so this exception is ignored
            }
            SecurityManager securityManager = System.getSecurityManager();
            if (securityManager != null) {
                int index = name.lastIndexOf('.');
                if (index >= 0) {
                    try {
                        securityManager.checkPackageAccess(name.substring(0, index));
                    } catch (SecurityException se) {
                        String error = "Security Violation, attempt to use " + "Restricted Class: " + name;
                        log.warn(error, se);
                        throw new ClassNotFoundException(error, se);
                    }
                }
            }

            // Search local repositories
            try {
                c = findClass(name);
                if (c != null) {
                    if (log.isTraceEnabled()) {
                        log.trace("- Loading class " + name + " from local repository " + resourceManager);
                    }
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
                if (c != null) {
                    if (log.isTraceEnabled()) {
                        log.trace("- Loading class from parent " + getParent());
                    }
                    if (resolve) {
                        resolveClass(c);
                    }
                    return c;
                }
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }

        throw new ClassNotFoundException(name);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        try  {
            byte[] classData = loadClassData(name);
            if (classData != null) {
                return defineClass(name, classData, 0, classData.length);
            } else {
                throw new ClassNotFoundException(name);
            }
        } catch (InvalidResourceException e) {
            log.warn("Invalid resource " + name, e);
            throw new ClassNotFoundException(name, e);
        } catch (RuntimeException e) {
            log.warn("Unable to load class " + name, e);
            throw e;
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

            byte[] buffer = new byte[8192];
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
    public URL findResource(String name) {
        URL url = null;
        Enumeration<URL> res = ResourceManager.getResources(getAllMembers(), name);
        if (res.hasMoreElements()) {
            url = res.nextElement();
        }
        return url;
    }

    @Override
    public Enumeration<URL> findResources(String name) {
        LinkedHashSet<URL> result = new LinkedHashSet<>();
        Enumeration<URL> res = ResourceManager.getResources(getAllMembers(), name);
        if (res.hasMoreElements()) {
            result.add(res.nextElement());
        }
        return Collections.enumeration(result);
    }

    public Iterator<AspectranClassLoader> getAllMembers() {
        return getMembers(root);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("id", id);
        if (getParent() instanceof AspectranClassLoader) {
            tsb.append("parent", ((AspectranClassLoader)getParent()).getId());
        } else {
            tsb.append("parent", getParent().getClass().getName());
        }
        tsb.append("root", this == root);
        tsb.append("firstborn", firstborn);
        tsb.append("resourceLocation", resourceLocation);
        tsb.append("numberOfResource", resourceManager.getResourceEntriesSize());
        tsb.appendSize("numberOfChildren", children);
        tsb.append("reloadedCount", reloadedCount);
        return tsb.toString();
    }

    public static Iterator<AspectranClassLoader> getMembers(final AspectranClassLoader root) {
        return new Iterator<AspectranClassLoader>() {
            private AspectranClassLoader next = root;
            private Iterator<AspectranClassLoader> children = root.getChildren().iterator();
            private AspectranClassLoader firstChild;
            private AspectranClassLoader current;

            @Override
            public boolean hasNext() {
                return (next != null);
            }

            @Override
            public AspectranClassLoader next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }

                current = next;
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

    public static ClassLoader getDefaultClassLoader() {
        if (System.getSecurityManager() == null) {
            ClassLoader cl = null;
            try {
                cl = Thread.currentThread().getContextClassLoader();
            } catch (Throwable ex) {
                // ignore
            }
            if (cl == null) {
                cl = AspectranClassLoader.class.getClassLoader();
            }
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
            return cl;
        } else {
            return AccessController.doPrivileged((PrivilegedAction<ClassLoader>)() -> {
                ClassLoader cl = null;
                try {
                    cl = Thread.currentThread().getContextClassLoader();
                } catch (Throwable ex) {
                    // ignore
                }
                if (cl == null) {
                    cl = AspectranClassLoader.class.getClassLoader();
                }
                if (cl == null) {
                    cl = ClassLoader.getSystemClassLoader();
                }
                return cl;
            });
        }
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

        for (int i = 0; i < resourceLocations.length; i++) {
            if (resourceLocations[i].startsWith(CLASSPATH_URL_PREFIX)) {
                String path = resourceLocations[i].substring(CLASSPATH_URL_PREFIX.length());
                URL url = getDefaultClassLoader().getResource(path);
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
