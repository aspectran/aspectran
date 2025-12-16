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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
 * A specialized {@link ClassLoader} for Aspectran that modifies the standard Java delegation model.
 * Instead of a "parent-first" approach, this class loader uses a "sibling-first" strategy.
 * When a class or resource is requested, it first searches within its own resources and those
 * of its "sibling" class loaders. Only if the resource is not found among the siblings does it
 * delegate the request to the parent class loader.
 *
 * <p>This mechanism is essential for Aspectran's modular architecture, allowing different modules
 * (each with its own class loader) to share classes and resources as if they were in the same
 * logical classpath. It is also the foundation for the hot-reloading feature, enabling dynamic
 * updates to the application without a full restart.</p>
 *
 * @see ResourceManager
 * @see LocalResourceManager
 */
public final class SiblingClassLoader extends ClassLoader {

    /** A unique identifier for this class loader instance. */
    private final int id;

    /** The root class loader of the entire sibling group. */
    private final SiblingClassLoader root;

    /** A flag indicating whether this is the first child of its parent. */
    private final boolean firstborn;

    /** The file system path (directory or JAR) that this class loader manages. */
    private final String resourceLocation;

    /** The resource manager responsible for scanning and caching resources from the location. */
    private final ResourceManager resourceManager;

    /** The list of other class loaders that are siblings to this one. */
    private final List<SiblingClassLoader> siblings = new LinkedList<>();

    /** A set of fully qualified class names to be excluded from this class loader. */
    private Set<String> excludeClassNames;

    /** A set of package names to be excluded from this class loader. */
    private Set<String> excludePackageNames;

    /** A counter for how many times this class loader has been reloaded. */
    private int reloadedCount;

    /**
     * Creates a new root SiblingClassLoader with the default parent class loader.
     * @throws InvalidResourceException if an error occurs during initialization
     */
    public SiblingClassLoader() throws InvalidResourceException {
        this(null, (ClassLoader)null);
    }

    /**
     * Creates a new root SiblingClassLoader with a specified name and the default parent.
     * @param name the name of the class loader
     * @throws InvalidResourceException if an error occurs during initialization
     */
    public SiblingClassLoader(String name) throws InvalidResourceException {
        this(name, (ClassLoader)null);
    }

    /**
     * Creates a new root SiblingClassLoader with a specified name and parent.
     * This constructor establishes the root of a new sibling hierarchy.
     * @param name the name of the class loader, or {@code null} for an anonymous loader
     * @param parent the parent class loader for delegation
     * @throws InvalidResourceException if an error occurs during initialization
     */
    public SiblingClassLoader(String name, ClassLoader parent) throws InvalidResourceException {
        super(name, parent != null ? parent : ClassUtils.getDefaultClassLoader());

        this.id = 1000;
        this.root = this;
        this.firstborn = true;
        this.resourceLocation = null;
        this.resourceManager = new LocalResourceManager(this);
    }

    /**
     * Creates a new root SiblingClassLoader with specified resource locations.
     * @param resourceLocations paths to directories or JAR files
     * @throws InvalidResourceException if a resource location is invalid
     */
    public SiblingClassLoader(String[] resourceLocations) throws InvalidResourceException {
        this(null, null, resourceLocations);
    }

    /**
     * Creates a new root SiblingClassLoader with a name and specified resource locations.
     * @param name the name of the class loader
     * @param resourceLocations paths to directories or JAR files
     * @throws InvalidResourceException if a resource location is invalid
     */
    public SiblingClassLoader(String name, String[] resourceLocations) throws InvalidResourceException {
        this(name, null, resourceLocations);
    }

    /**
     * Creates a new root SiblingClassLoader with a parent and specified resource locations.
     * @param parent the parent class loader
     * @param resourceLocations paths to directories or JAR files
     * @throws InvalidResourceException if a resource location is invalid
     */
    public SiblingClassLoader(ClassLoader parent, String[] resourceLocations) throws InvalidResourceException {
        this(null, parent, resourceLocations);
    }

    /**
     * Creates a new root SiblingClassLoader with a name, parent, and specified resource locations.
     * @param name the name of the class loader
     * @param parent the parent class loader
     * @param resourceLocations paths to directories or JAR files
     * @throws InvalidResourceException if a resource location is invalid
     */
    public SiblingClassLoader(String name, ClassLoader parent, String[] resourceLocations)
            throws InvalidResourceException {
        this(name, parent != null ? parent : ClassUtils.getDefaultClassLoader());
        if (resourceLocations != null) {
            createSibling(resourceLocations);
        }
    }

    /**
     * Private constructor for creating a non-root (child) sibling class loader.
     * @param name the name of the class loader
     * @param parent the parent SiblingClassLoader
     * @param resourceLocation the resource path for this new sibling
     * @throws InvalidResourceException if the resource location is invalid
     */
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

    /**
     * Creates a new sibling for the parent class loader to manage a new resource location.
     * @param resourceLocation the resource path for the new sibling
     * @throws InvalidResourceException if the resource location is invalid
     */
    void joinSibling(String resourceLocation) throws InvalidResourceException {
        SiblingClassLoader parent = (SiblingClassLoader)getParent();
        parent.createSibling(resourceLocation);
    }

    /**
     * Creates multiple siblings from an array of resource locations.
     * @param resourceLocations the resource paths to create siblings for
     * @throws InvalidResourceException if a resource location is invalid
     */
    private void createSibling(@NonNull String[] resourceLocations) throws InvalidResourceException {
        SiblingClassLoader scl = this;
        for (String resourceLocation : resourceLocations) {
            if (resourceLocation != null && !resourceLocation.isEmpty()) {
                scl = scl.createSibling(resourceLocation);
            }
        }
    }

    /**
     * Creates a single new sibling for a given resource location.
     * Only the 'firstborn' of a parent can create other siblings.
     * @param resourceLocation the resource path for the new sibling
     * @return the newly created SiblingClassLoader
     * @throws InvalidResourceException if the resource location is invalid
     */
    @NonNull
    private SiblingClassLoader createSibling(String resourceLocation) throws InvalidResourceException {
        if (!firstborn) {
            throw new IllegalStateException("Only the firstborn can create other siblings");
        }
        return new SiblingClassLoader(getName(), this, resourceLocation);
    }

    /**
     * Registers package names to be excluded from this class loader.
     * Any class within these packages will be loaded by the parent class loader.
     * @param packageNames package names to exclude (e.g., "java.lang", "com.mycorp.shared")
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
     * Registers fully qualified class names to be excluded from this class loader.
     * The specified classes will be loaded by the parent class loader.
     * @param classNames fully qualified class names to exclude (e.g., "com.mycorp.LegacyClass")
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

    /**
     * Checks if a class is excluded by either package or class name rules.
     * @param className the fully qualified class name
     * @return true if the class is excluded, false otherwise
     */
    private boolean isExcluded(String className) {
        return (isExcludedPackage(className) || isExcludedClass(className));
    }

    /**
     * Checks if a class belongs to an excluded package.
     * @param className the fully qualified class name
     * @return true if the class is in an excluded package, false otherwise
     */
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

    /**
     * Checks if a class is explicitly excluded by name.
     * @param className the fully qualified class name
     * @return true if the class is excluded by name, false otherwise
     */
    private boolean isExcludedClass(String className) {
        return (excludeClassNames != null && excludeClassNames.contains(className));
    }

    /**
     * Returns the unique ID of this class loader.
     * @return the class loader ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the root of the sibling hierarchy.
     * @return the root SiblingClassLoader
     */
    public SiblingClassLoader getRoot() {
        return root;
    }

    /**
     * Checks if this instance is the root of the sibling hierarchy.
     * @return true if this is the root, false otherwise
     */
    public boolean isRoot() {
        return (this == root);
    }

    /**
     * Returns a list of immediate siblings of this class loader.
     * @return the list of siblings
     */
    public List<SiblingClassLoader> getSiblings() {
        return siblings;
    }

    /**
     * Adds a new sibling to this class loader's list of siblings.
     * @param sibling the SiblingClassLoader to add
     * @return the new number of siblings
     */
    private int addSibling(SiblingClassLoader sibling) {
        synchronized (siblings) {
            siblings.add(sibling);
            return siblings.size();
        }
    }

    /**
     * Checks if this class loader has any siblings.
     * @return true if there are one or more siblings, false otherwise
     */
    public boolean hasSiblings() {
        return !siblings.isEmpty();
    }

    /**
     * Checks if this is the firstborn child of its parent.
     * @return true if this is the firstborn, false otherwise
     */
    public boolean isFirstborn() {
        return firstborn;
    }

    /**
     * Returns the resource manager associated with this class loader.
     * @return the ResourceManager instance
     */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    /**
     * Returns the resource location (path) managed by this class loader.
     * @return the resource location string
     */
    public String getResourceLocation() {
        return resourceLocation;
    }

    /**
     * Triggers a hot-reload of the entire sibling hierarchy, starting from the root.
     * This clears all cached resources and forces them to be re-discovered from their locations.
     * @throws InvalidResourceException if an error occurs during the reload
     */
    public synchronized void reload() throws InvalidResourceException {
        reload(root);
    }

    /**
     * Recursively reloads a class loader and its descendants.
     * @param self the class loader to reload
     * @throws InvalidResourceException if an error occurs during the reload
     */
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

    /**
     * Increments the reload counter.
     */
    private void increaseReloadedCount() {
        reloadedCount++;
    }

    /**
     * Removes a list of siblings and releases their resources.
     * @param siblings the list of SiblingClassLoaders to remove
     */
    private void leave(@NonNull List<SiblingClassLoader> siblings) {
        for (SiblingClassLoader sibling : siblings) {
            ResourceManager rm = sibling.getResourceManager();
            if (rm != null) {
                rm.release();
            }
            this.siblings.remove(sibling);
        }
    }

    /**
     * Overrides the default class loading logic to implement the sibling-first strategy.
     * The loading order is: (1) check if already loaded, (2) find in self and siblings,
     * (3) delegate to parent class loader.
     * @param name the fully qualified name of the class to load
     * @param resolve if {@code true}, the class will be linked
     * @return the resulting {@code Class} object
     * @throws ClassNotFoundException if the class could not be found
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    // Second, search from local/sibling repositories
                    c = findClass(name);
                } catch (ClassNotFoundException e) {
                    // ignored
                }
                if (c == null) {
                    // If not found locally, delegate to the parent classloader
                    ClassLoader parent = root.getParent();
                    if (parent != null) {
                        c = Class.forName(name, false, parent);
                    } else {
                        // If no parent, use the system class loader
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

    /**
     * Finds the specified class within this class loader or any of its siblings.
     * @param name the fully qualified name of the class
     * @return the resulting {@code Class} object
     * @throws ClassNotFoundException if the class could not be found
     */
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

    /**
     * Loads the raw byte data for a class from the first sibling that contains it.
     * @param className the fully qualified name of the class
     * @return a byte array containing the class data, or {@code null} if not found or excluded
     * @throws InvalidResourceException if the class file is found but cannot be read
     */
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

    /**
     * Finds a resource using the sibling-first strategy.
     * @param name the name of the resource
     * @return a {@link URL} for the resource, or {@code null} if not found
     */
    @Override
    public URL getResource(String name) {
        // Search from local/sibling repositories first
        URL url = findResource(name);
        if (url == null) {
            // If not found, delegate to parent
            ClassLoader parent = root.getParent();
            if (parent != null) {
                url = parent.getResource(name);
            } else {
                url = getSystemClassLoader().getResource(name);
            }
        }
        return url;
    }

    /**
     * Finds all occurrences of a resource using the sibling-first strategy and including parent resources.
     * @param name the name of the resource
     * @return an enumeration of {@link URL}s for the resource
     * @throws IOException if an I/O error occurs
     */
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

    /**
     * Finds the first occurrence of a resource within the sibling group.
     * @param name the name of the resource
     * @return a {@link URL} for the resource, or {@code null} if not found
     */
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

    /**
     * Finds all occurrences of a resource within the sibling group.
     * @param name the name of the resource
     * @return an enumeration of {@link URL}s for the resource
     */
    @Override
    @NonNull
    public Enumeration<URL> findResources(String name) {
        Objects.requireNonNull(name);
        return ResourceManager.findResources(name, getAllSiblings());
    }

    /**
     * Returns an enumeration of all resources found within the entire sibling group.
     * @return an enumeration of all resource {@link URL}s
     */
    @NonNull
    public Enumeration<URL> getAllResources() {
        return ResourceManager.findResources(getAllSiblings());
    }

    /**
     * Returns an iterator that traverses this class loader and all of its direct and indirect siblings.
     * @return an iterator for the entire sibling group
     */
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

    /**
     * Returns a custom iterator to traverse the nested hierarchy of siblings.
     * @param root the starting root class loader
     * @return an iterator for the sibling hierarchy
     */
    @NonNull
    private static Iterator<SiblingClassLoader> getSiblings(@NonNull SiblingClassLoader root) {
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
