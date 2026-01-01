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
package com.aspectran.shell.command.option;

import java.io.Serial;
import java.util.LinkedHashMap;

/**
 * Represents a collection of arguments for a command.
 * Stores argument names and their descriptions.
 *
 * <p>Created: 2019-01-18</p>
 */
public class Arguments extends LinkedHashMap<String, String> {

    @Serial
    private static final long serialVersionUID = 2065201886243368933L;

    private String title;

    private boolean required;

    public Arguments() {
        super();
    }

    /**
     * Returns the title of this argument group.
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of this argument group.
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns whether these arguments are required.
     * @return true if required, false otherwise
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Sets whether these arguments are required.
     * @param required true if required, false otherwise
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

}
