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

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class for building and managing XPath-like paths during XML parsing.
 * This class is used internally by the {@link NodeletParser} to track the current
 * position in the document, and to handle path manipulation for mounted nodelet groups.
 */
public class NodeletPath {

    static final String DIVIDER = "/";

    private final List<String> nameList = new ArrayList<>();

    private String xpath;

    private int mountIndex;

    private String mountXpath;

    NodeletPath() {
    }

    /**
     * Adds a node name to the end of the path.
     * @param node the node name to add
     */
    public void add(String node) {
        nameList.add(node);
        xpath = null;
        mountXpath = null;
    }

    /**
     * Removes the last node name from the path.
     */
    public void remove() {
        int index = nameList.size() - 1;
        nameList.remove(index);
        xpath = null;
        mountXpath = null;
    }

    /**
     * Marks the current path position as a mount point.
     * @return the index of the mount point
     */
    public int mount() {
        mountIndex = nameList.size() - 1;
        mountXpath = null;
        return mountIndex;
    }

    /**
     * Changes the mount point to a new index.
     * @param index the new mount index
     */
    public void remount(int index) {
        mountIndex = index;
        mountXpath = null;
    }

    /**
     * Resets the mount point, effectively unmounting the current group.
     */
    public void unmount() {
        mountIndex = 0;
        mountXpath = null;
    }

    /**
     * Returns the XPath relative to the current mount point.
     * @return the relative XPath from the mount point
     */
    @Nullable
    public String getMountXpath() {
        if (mountXpath == null) {
            if (nameList.isEmpty()) {
                return null;
            }
            if (nameList.size() == 1) {
                mountXpath = nameList.getFirst();
            } else {
                StringBuilder sb = new StringBuilder(32);
                for (int i = mountIndex; i < nameList.size(); i++) {
                    sb.append(DIVIDER).append(nameList.get(i));
                }
                mountXpath = sb.toString();
            }
        }
        return mountXpath;
    }

    /**
     * Returns the name of the node that could trigger a mount,
     * which is the second to last node in the path.
     * @return the name of the trigger node, or {@code null} if not applicable
     */
    @Nullable
    public String findTriggerName() {
        return (nameList.size() > 2 ? nameList.get(nameList.size() - 2) : null);
    }

    @Override
    public String toString() {
        if (xpath == null) {
            if (nameList.isEmpty()) {
                xpath = DIVIDER;
            } else {
                StringBuilder sb = new StringBuilder(64);
                for (String name : nameList) {
                    sb.append(DIVIDER).append(name);
                }
                xpath = sb.toString();
            }
        }
        return xpath;
    }

}
