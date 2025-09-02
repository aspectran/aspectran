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
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
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

    public NodeletGroup() {
        this(StringUtils.EMPTY);
    }

    public NodeletGroup(String name) {
        this(name, NodeletPath.DIVIDER + name, null);
    }

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

    public String getName() {
        return name;
    }

    public String getXpath() {
        return xpath;
    }

    public Map<String, Nodelet> getNodeletMap() {
        if (parent != null) {
            return parent.getNodeletMap();
        } else {
            return nodeletMap;
        }
    }

    public Map<String, EndNodelet> getEndNodeletMap() {
        if (parent != null) {
            return parent.getEndNodeletMap();
        } else {
            return endNodeletMap;
        }
    }

    public Map<String, NodeletGroup> getMountedGroups() {
        if (parent != null) {
            return parent.getMountedGroups();
        } else {
            return mountedGroups;
        }
    }

    public Nodelet getNodelet(String xpath) {
        return getNodeletMap().get(xpath);
    }

    public EndNodelet getEndNodelet(String xpath) {
        return getEndNodeletMap().get(xpath);
    }

    public NodeletGroup getMountedGroup(String xpath) {
        return getMountedGroups().get(xpath);
    }

    public NodeletGroup parent() {
        Assert.state(parent != null, "parent is null");
        return parent;
    }

    public NodeletGroup child(String name) {
        Assert.hasLength(name, "Child name cannot be null or empty");
        if (ignoreFirstChild && getName().equals(name)) {
            return this;
        }
        return new NodeletGroup(name, makeXpath(name), this);
    }

    public NodeletGroup with(NodeletAdder nodeletAdder) {
        Assert.notNull(nodeletAdder, "nodeletAdder cannot be null");
        nodeletAdder.addTo(this);
        return this;
    }

    public NodeletGroup with(boolean condition, NodeletAdder nodeletAdder) {
        if (condition) {
            with(nodeletAdder);
        }
        return this;
    }

    public NodeletGroup with(Supplier<NodeletAdder> supplier) {
        if (supplier != null) {
            NodeletAdder nodeletAdder = supplier.get();
            if (nodeletAdder != null) {
                with(nodeletAdder);
            }
        }
        return this;
    }

    public NodeletGroup mount(NodeletGroup group) {
        mount(makeMountPath(getName(), group.getName()), group);
        return this;
    }

    public NodeletGroup mount(String mountPath, NodeletGroup group) {
        getMountedGroups().put(mountPath, group);
        return this;
    }

    public NodeletGroup nodelet(Nodelet nodelet) {
        getNodeletMap().put(xpath, nodelet);
        return this;
    }

    public NodeletGroup nodelet(String relativePath, Nodelet nodelet) {
        getNodeletMap().put(makeXpath(relativePath), nodelet);
        return this;
    }

    public NodeletGroup endNodelet(EndNodelet endNodelet) {
        getEndNodeletMap().put(xpath, endNodelet);
        return this;
    }

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
