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

/**
 * Tracks the name and source code location (line and column number) of a node
 * during document parsing. This is primarily used by {@link NodeletParser}
 * to provide contextual information for errors or for debugging purposes.
 */
public class NodeTracker implements Cloneable {

    private String name;

    private int lineNumber;

    private int columnNumber;

    /**
     * Creates a new NodeTracker with no initial name or location.
     */
    public NodeTracker() {
    }

    /**
     * Creates a new NodeTracker with the specified name and location.
     * @param name the name of the node
     * @param lineNumber the line number where the node is located
     * @param columnNumber the column number where the node is located
     */
    public NodeTracker(String name, int lineNumber, int columnNumber) {
        this.name = name;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    /**
     * Returns the name of the tracked node.
     * @return the node name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the tracked node.
     * @param name the node name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the line number of the tracked node.
     * @return the line number
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Returns the column number of the tracked node.
     * @return the column number
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * Sets the location (line and column number) of the tracked node.
     * @param lineNumber the line number
     * @param columnNumber the column number
     */
    public void setLocation(int lineNumber, int columnNumber) {
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    /**
     * Returns a cloned NodeTracker instance that represents the current state of this tracker.
     * @return a cloned NodeTracker
     */
    @Nullable
    public NodeTracker getClonedNodeTracker() {
        try {
            return (NodeTracker) clone();
        } catch (CloneNotSupportedException e) {
            return null; // Should not happen as NodeTracker implements Cloneable
        }
    }

    /**
     * Sets the cloned NodeTracker instance.
     * @param clonedNodeTracker the cloned NodeTracker
     */
    public void setClonedNodeTracker(NodeTracker clonedNodeTracker) {
        this.name = clonedNodeTracker.name;
        this.lineNumber = clonedNodeTracker.lineNumber;
        this.columnNumber = clonedNodeTracker.columnNumber;
    }

    /**
     * Creates and returns a shallow copy of this NodeTracker instance.
     * @return a clone of this instance
     * @throws CloneNotSupportedException if the object's class does not support the {@code Cloneable} interface.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Returns a string representation of the NodeTracker, including its name and location.
     * @return a string representation of the NodeTracker
     */
    @Override
    public String toString() {
        return "node '" + name + "' at line " + lineNumber + ", column " + columnNumber;
    }

}