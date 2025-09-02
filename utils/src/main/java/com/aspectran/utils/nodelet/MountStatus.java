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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public NodeletGroup getGroup() {
        return group;
    }

    public void setGroup(NodeletGroup group) {
        this.group = group;
    }

    public Nodelet getNodelet(String xpath) {
        return group.getNodelet(xpath);
    }

    public EndNodelet getEndNodelet(String xpath) {
        return group.getEndNodelet(xpath);
    }

    @NonNull
    static MountStatus of(int index, String path, NodeletGroup group) {
        MountStatus mountedGroup = new MountStatus();
        mountedGroup.setIndex(index);
        mountedGroup.setGroup(group);
        mountedGroup.setPath(path);
        return mountedGroup;
    }

}
