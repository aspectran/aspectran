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
import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * Tracks the name and source code location (line and column number) of a node
 * during document parsing. This is primarily used by {@link NodeletParser}
 * to provide contextual information for errors or for debugging purposes.
 */
public class NodeTracker implements Cloneable {

    private String resource;

    private String xpath;

    private int lineNumber;

    private int columnNumber;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
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
     * Creates and returns a new {@code NodeTracker} instance that represents a snapshot
     * of the current state of this tracker.
     * @return a new {@code NodeTracker} instance with the current state
     */
    @Nullable
    public NodeTracker createSnapshot() {
        try {
            return (NodeTracker)clone();
        } catch (CloneNotSupportedException e) {
            // This should not happen as NodeTracker implements Cloneable
            throw new RuntimeException("Failed to clone NodeTracker", e);
        }
    }

    /**
     * Restores the state of this {@code NodeTracker} instance by copying the state
     * from a previously created snapshot.
     * @param snapshot the {@code NodeTracker} instance to restore state from
     */
    void restoreStateFrom(@NonNull NodeTracker snapshot) {
        this.resource = snapshot.resource;
        this.xpath = snapshot.xpath;
        this.lineNumber = snapshot.lineNumber;
        this.columnNumber = snapshot.columnNumber;
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
        return "node \"" + xpath + "\" at line " + lineNumber + ", column " + columnNumber +
                (resource != null ? " on " + resource : "");
    }

}
