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

import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * Represents the state of a mounted {@link NodeletGroup} during parsing.
 * This class is used internally by {@link NodeletParser} to manage the context
 * when a different set of parsing rules is dynamically applied to a sub-section
 * of an XML document.
 */
public class MountStatus {

    private int index;

    private String path;

    private NodeletGroup group;

    MountStatus() {
    }

    /**
     * Returns the index in the path where this group was mounted.
     * @return the mount index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index in the path where this group was mounted.
     * @param index the mount index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Returns the full path that triggered the mount.
     * @return the mount path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the full path that triggered the mount.
     * @param path the mount path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns the mounted nodelet group.
     * @return the nodelet group
     */
    public NodeletGroup getGroup() {
        return group;
    }

    /**
     * Sets the mounted nodelet group.
     * @param group the nodelet group
     */
    public void setGroup(NodeletGroup group) {
        this.group = group;
    }

    /**
     * Retrieves a start nodelet from the mounted group for the given XPath.
     * @param xpath the XPath to look up
     * @return the found {@link Nodelet}, or {@code null} if not found
     */
    public Nodelet getNodelet(String xpath) {
        return group.getNodelet(xpath);
    }

    /**
     * Retrieves an end nodelet from the mounted group for the given XPath.
     * @param xpath the XPath to look up
     * @return the found {@link EndNodelet}, or {@code null} if not found
     */
    public EndNodelet getEndNodelet(String xpath) {
        return group.getEndNodelet(xpath);
    }

    /**
     * Creates a new {@code MountStatus} instance.
     * @param index the index in the path where this group was mounted
     * @param path the full path that triggered the mount
     * @param group the mounted nodelet group
     * @return a new {@code MountStatus} instance
     */
    @NonNull
    static MountStatus of(int index, String path, NodeletGroup group) {
        MountStatus mountedGroup = new MountStatus();
        mountedGroup.setIndex(index);
        mountedGroup.setGroup(group);
        mountedGroup.setPath(path);
        return mountedGroup;
    }

}
