package com.aspectran.utils.nodelet;

import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;

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

    private final String xpath;

    private final NodeletGroup parent;

    public NodeletGroup() {
        this("/", null);
    }

    public NodeletGroup(String xpath) {
        this("/" + xpath, null);
    }

    private NodeletGroup(String xpath, NodeletGroup parent) {
        this.parent = parent;
        this.xpath = xpath;
        if (parent == null) {
            this.nodeletMap = new HashMap<>();
            this.endNodeletMap = new HashMap<>();
            this.mountedGroups = new HashMap<>();
        } else {
            this.nodeletMap = parent.nodeletMap;
            this.endNodeletMap = parent.endNodeletMap;
            this.mountedGroups = parent.mountedGroups;
        }
    }

    public String getXpath() {
        return xpath;
    }

    public Map<String, Nodelet> getNodeletMap() {
//        if (parent != null) {
//            return parent.getNodeletMap();
//        } else {
            return nodeletMap;
//        }
    }

    public Map<String, EndNodelet> getEndNodeletMap() {
//        if (parent != null) {
//            return parent.getEndNodeletMap();
//        } else {
            return endNodeletMap;
//        }
    }

    public Map<String, NodeletGroup> getMountedGroups() {
//        if (parent != null) {
//            return parent.getMountedGroups();
//        } else {
            return mountedGroups;
//        }
    }

    public NodeletGroup parent() {
        Assert.state(parent != null, "parent is null");
        return parent;
    }

    public NodeletGroup child(String relativePath) {
        Assert.hasLength(relativePath, "xpath cannot be null or empty");
        return new NodeletGroup(makeXpath(relativePath), this);
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
        getMountedGroups().put(xpath, group);
        return this;
    }

    public NodeletGroup mount(String triggerPath, NodeletGroup group) {
        getMountedGroups().put(makeXpath(triggerPath), group);
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
        if (xpath.endsWith("/")) {
            return xpath + relativePath;
        } else {
            return xpath + "/" + relativePath;
        }
    }

}
