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
package com.aspectran.utils.nodelet;

import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A collection of {@link Nodelet}s that are mapped to specific XPath expressions.
 * This class provides a fluent API for building a hierarchy of parsing rules,
 * making it easy to define and reuse complex parsing logic for structured documents.
 * A group can be nested within another to create a hierarchical structure, and
 * other groups can be "mounted" to handle specific sub-sections of a document,
 * enabling modular and reusable parsing configurations.
 *
 * <p>Created: 2025-08-29</p>
 */
public class NodeletGroup {

    private final Map<String, Nodelet> nodeletMap;

    private final Map<String, EndNodelet> endNodeletMap;

    private final Map<String, NodeletGroup> mountedGroups;

    private final String name;

    private final String xpath;

    private final NodeletGroup parent;

    private final boolean ignoreFirstChild;

    /**
     * Constructs a new, empty root nodelet group.
     */
    public NodeletGroup() {
        this(StringUtils.EMPTY);
    }

    /**
     * Constructs a new root nodelet group with the specified name.
     * @param name the name of the group
     */
    public NodeletGroup(String name) {
        this(name, NodeletPath.DIVIDER + name, null);
    }

    /**
     * Constructs a new root nodelet group with the specified name and ignore-first-child flag.
     * @param name the name of the group
     * @param ignoreFirstChild if true, ignores the first child with the same name
     */
    public NodeletGroup(String name, boolean ignoreFirstChild) {
        this(name, NodeletPath.DIVIDER + name, null, ignoreFirstChild);
    }

    private NodeletGroup(String name, String xpath, NodeletGroup parent) {
        this(name, xpath, parent, false);
    }

    private NodeletGroup(String name, String xpath, NodeletGroup parent, boolean ignoreFirstChild) {
        this.name = name;
        this.xpath = xpath;
        this.parent = parent;
        this.ignoreFirstChild = ignoreFirstChild;
        if (parent == null) {
            this.nodeletMap = new HashMap<>();
            this.endNodeletMap = new HashMap<>();
            this.mountedGroups = new HashMap<>();
        } else {
            this.nodeletMap = Collections.emptyMap();
            this.endNodeletMap = Collections.emptyMap();
            this.mountedGroups = Collections.emptyMap();
        }
    }

    /**
     * Returns the name of this nodelet group.
     * @return the name of the group
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the XPath that this nodelet group is associated with.
     * @return the XPath of the group
     */
    public String getXpath() {
        return xpath;
    }

    /**
     * Returns the map of XPath expressions to {@link Nodelet}s for this group.
     * @return the map of nodelets
     */
    public Map<String, Nodelet> getNodeletMap() {
        if (parent != null) {
            return parent.getNodeletMap();
        } else {
            return nodeletMap;
        }
    }

    /**
     * Returns the map of XPath expressions to {@link EndNodelet}s for this group.
     * @return the map of end-nodelets
     */
    public Map<String, EndNodelet> getEndNodeletMap() {
        if (parent != null) {
            return parent.getEndNodeletMap();
        } else {
            return endNodeletMap;
        }
    }

    /**
     * Returns the map of mount paths to nested {@link NodeletGroup}s.
     * @return the map of mounted groups
     */
    public Map<String, NodeletGroup> getMountedGroups() {
        if (parent != null) {
            return parent.getMountedGroups();
        } else {
            return mountedGroups;
        }
    }

    /**
     * Retrieves a {@link Nodelet} for the given XPath.
     * @param xpath the XPath to look up
     * @return the found {@link Nodelet}, or {@code null} if not found
     */
    public Nodelet getNodelet(String xpath) {
        return getNodeletMap().get(xpath);
    }

    /**
     * Retrieves an {@link EndNodelet} for the given XPath.
     * @param xpath the XPath to look up
     * @return the found {@link EndNodelet}, or {@code null} if not found
     */
    public EndNodelet getEndNodelet(String xpath) {
        return getEndNodeletMap().get(xpath);
    }

    /**
     * Retrieves a mounted {@link NodeletGroup} for the given XPath.
     * @param xpath the XPath to look up
     * @return the found {@link NodeletGroup}, or {@code null} if not found
     */
    public NodeletGroup getMountedGroup(String xpath) {
        return getMountedGroups().get(xpath);
    }

    /**
     * Returns the parent of this nodelet group.
     * @return the parent nodelet group
     * @throws IllegalStateException if this is a root group and has no parent
     */
    public NodeletGroup parent() {
        Assert.state(parent != null, "parent is null");
        return parent;
    }

    /**
     * Creates a new child nodelet group under the current group's XPath.
     * @param name the name of the child element
     * @return the new child nodelet group
     */
    public NodeletGroup child(String name) {
        Assert.hasLength(name, "Child name cannot be null or empty");
        if (ignoreFirstChild && getName().equals(name)) {
            return this;
        }
        return new NodeletGroup(name, makeXpath(name), this);
    }

    /**
     * Adds a set of nodelets to this group using a {@link NodeletAdder}.
     * @param nodeletAdder the adder that encapsulates the nodelets to be added
     * @return this nodelet group, for fluent method chaining
     */
    public NodeletGroup with(NodeletAdder nodeletAdder) {
        Assert.notNull(nodeletAdder, "nodeletAdder cannot be null");
        nodeletAdder.addTo(this);
        return this;
    }

    /**
     * Conditionally adds a set of nodelets to this group using a {@link NodeletAdder}.
     * @param condition the condition to evaluate
     * @param nodeletAdder the adder to use if the condition is true
     * @return this nodelet group, for fluent method chaining
     */
    public NodeletGroup with(boolean condition, NodeletAdder nodeletAdder) {
        if (condition) {
            with(nodeletAdder);
        }
        return this;
    }

    /**
     * Lazily adds a set of nodelets to this group using a {@link Supplier}.
     * The supplier is only invoked if it is not null.
     * @param supplier a supplier for the NodeletAdder
     * @return this nodelet group, for fluent method chaining
     */
    public NodeletGroup with(Supplier<NodeletAdder> supplier) {
        if (supplier != null) {
            NodeletAdder nodeletAdder = supplier.get();
            if (nodeletAdder != null) {
                with(nodeletAdder);
            }
        }
        return this;
    }

    /**
     * Mounts another {@link NodeletGroup} at the current path, triggered by the mounted group's name.
     * This allows for modular and reusable parsing logic.
     * @param group the nodelet group to mount
     * @return this nodelet group, for fluent method chaining
     */
    public NodeletGroup mount(NodeletGroup group) {
        mount(makeMountPath(getName(), group.getName()), group);
        return this;
    }

    /**
     * Mounts another {@link NodeletGroup} at a specific path.
     * @param mountPath the path that triggers the mount
     * @param group the nodelet group to mount
     * @return this nodelet group, for fluent method chaining
     */
    public NodeletGroup mount(String mountPath, NodeletGroup group) {
        getMountedGroups().put(mountPath, group);
        return this;
    }

    /**
     * Adds a {@link Nodelet} to handle the start event for the current XPath.
     * @param nodelet the nodelet to add
     * @return this nodelet group, for fluent method chaining
     */
    public NodeletGroup nodelet(Nodelet nodelet) {
        getNodeletMap().put(xpath, nodelet);
        return this;
    }

    /**
     * Adds a {@link Nodelet} to handle the start event for a path relative to the current XPath.
     * @param relativePath the relative path from the current group's path
     * @param nodelet the nodelet to add
     * @return this nodelet group, for fluent method chaining
     */
    public NodeletGroup nodelet(String relativePath, Nodelet nodelet) {
        getNodeletMap().put(makeXpath(relativePath), nodelet);
        return this;
    }

    /**
     * Adds an {@link EndNodelet} to handle the end event for the current XPath.
     * @param endNodelet the end-nodelet to add
     * @return this nodelet group, for fluent method chaining
     */
    public NodeletGroup endNodelet(EndNodelet endNodelet) {
        getEndNodeletMap().put(xpath, endNodelet);
        return this;
    }

    /**
     * Adds an {@link EndNodelet} to handle the end event for a path relative to the current XPath.
     * @param relativePath the relative path from the current group's path
     * @param endNodelet the end-nodelet to add
     * @return this nodelet group, for fluent method chaining
     */
    public NodeletGroup endNodelet(String relativePath, EndNodelet endNodelet) {
        getEndNodeletMap().put(makeXpath(relativePath), endNodelet);
        return this;
    }

    @NonNull
    private String makeXpath(String relativePath) {
        Assert.hasLength(relativePath, "relativePath cannot be null or empty");
        if (xpath.endsWith(NodeletPath.DIVIDER)) {
            return xpath + relativePath;
        } else {
            return xpath + NodeletPath.DIVIDER + relativePath;
        }
    }

    @NonNull
    static String makeMountPath(String triggerName, String groupName) {
        Assert.hasLength(triggerName, "triggerName cannot be null or empty");
        Assert.hasLength(groupName, "groupName cannot be null or empty");
        return triggerName + NodeletPath.DIVIDER + groupName;
    }

}
