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

import com.aspectran.utils.annotation.jsr305.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Inner helper class that assists with building XPath paths.
 */
public class NodeletPath {

    static final String DIVIDER = "/";

    private final List<String> nameList = new ArrayList<>();

    private String xpath;

    private int mountIndex;

    private String mountXpath;

    NodeletPath() {
    }

    public void add(String node) {
        nameList.add(node);
        xpath = null;
        mountXpath = null;
    }

    public void remove() {
        int index = nameList.size() - 1;
        nameList.remove(index);
        xpath = null;
        mountXpath = null;
    }

    public int mount() {
        mountIndex = nameList.size() - 1;
        mountXpath = null;
        return mountIndex;
    }

    public void remount(int index) {
        mountIndex = index;
        mountXpath = null;
    }

    public void unmount() {
        mountIndex = 0;
        mountXpath = null;
    }

    @Nullable
    public String getMountXpath() {
        if (mountXpath == null) {
            if (nameList.isEmpty()) {
                return null;
            }
            if (nameList.size() == 1) {
                mountXpath = nameList.get(0);
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
