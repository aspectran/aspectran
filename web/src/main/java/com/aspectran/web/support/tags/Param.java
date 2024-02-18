/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.web.support.tags;

import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * Bean used to pass name-value pair parameters from a {@link ParamTag} to a
 * {@link ParamAware} tag.
 *
 * <p>Attributes are the raw values passed to the aspectran:param tag and have not
 * been encoded or escaped.
 *
 * @see ParamTag
 */
public class Param {

    @Nullable
    private String name;

    @Nullable
    private String value;

    /**
     * Set the raw name of the parameter.
     */
    public void setName(@Nullable String name) {
        this.name = name;
    }

    /**
     * Return the raw parameter name.
     */
    @Nullable
    public String getName() {
        return this.name;
    }

    /**
     * Set the raw value of the parameter.
     */
    public void setValue(@Nullable String value) {
        this.value = value;
    }

    /**
     * Return the raw parameter value.
     */
    @Nullable
    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "JSP Tag Param: name '" + this.name + "', value '" + this.value + "'";
    }

}
