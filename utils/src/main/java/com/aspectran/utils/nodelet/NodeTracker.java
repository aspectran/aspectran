/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
 * Inner helper class for tracking the line number and column number of
 * the element's start tag while reading an XML document.
 *
 * <p>Created: 2017. 11. 10.</p>
 */
public class NodeTracker implements Cloneable {

    private String name;

    private int lineNumber;

    private int columnNumber;

    private NodeTracker clonedNodeTracker;

    public NodeTracker() {
    }

    public String getName() {
        return name;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setLocation(int lineNumber, int columnNumber) {
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    protected void update(@NonNull NodeTracker tracker) {
        setName(tracker.getName());
        setLocation(tracker.getLineNumber(), tracker.getColumnNumber());
    }

    public NodeTracker getClonedNodeTracker() {
        return clonedNodeTracker;
    }

    protected void setClonedNodeTracker(NodeTracker nodeTracker) {
        this.clonedNodeTracker = nodeTracker;
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public NodeTracker clone() {
        NodeTracker tracker = new NodeTracker();
        tracker.setName(name);
        tracker.setLocation(lineNumber, columnNumber);
        setClonedNodeTracker(tracker);
        return tracker;
    }

    @Override
    public String toString() {
        return "<" +
            name +
            ">" +
            " [lineNumber: " +
            lineNumber +
            ", columnNumber: " +
            columnNumber +
            "]";
    }

}
