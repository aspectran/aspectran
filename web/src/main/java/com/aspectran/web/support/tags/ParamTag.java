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
package com.aspectran.web.support.tags;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import org.jspecify.annotations.Nullable;

import java.io.Serial;

/**
 * The {@code <param>} tag collects name-value parameters and passes them to a
 * {@link ParamAware} ancestor in the tag hierarchy.
 *
 * <p>This tag must be nested under a param aware tag.
 *
 * <table>
 * <caption>Attribute Summary</caption>
 * <thead>
 * <tr>
 * <th>Attribute</th>
 * <th>Required?</th>
 * <th>Runtime Expression?</th>
 * <th>Description</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>name</td>
 * <td>true</td>
 * <td>true</td>
 * <td>The name of the parameter.</td>
 * </tr>
 * <tr>
 * <td>value</td>
 * <td>false</td>
 * <td>true</td>
 * <td>The value of the parameter.</td>
 * </tr>
 * </tbody>
 * </table>
 *
 * @see Param
 * @see UrlTag
 */
public class ParamTag extends BodyTagSupport {

    @Serial
    private static final long serialVersionUID = 2318398257284641244L;

    private String name = "";

    @Nullable
    private String value;

    private boolean valueSet;

    /**
     * Set the name of the parameter (required).
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the value of the parameter (optional).
     */
    public void setValue(String value) {
        this.value = value;
        this.valueSet = true;
    }

    @Override
    public int doEndTag() throws JspException {
        Param param = new Param();
        param.setName(this.name);
        if (this.valueSet) {
            param.setValue(this.value);
        } else if (getBodyContent() != null) {
            // Get the value from the tag body
            param.setValue(getBodyContent().getString().trim());
        }

        // Find a param aware ancestor
        ParamAware paramAwareTag = (ParamAware)findAncestorWithClass(this, ParamAware.class);
        if (paramAwareTag == null) {
            throw new JspException("The param tag must be a descendant of a tag that supports parameters");
        }

        paramAwareTag.addParam(param);

        return EVAL_PAGE;
    }

    @Override
    public void release() {
        super.release();
        this.name = "";
        this.value = null;
        this.valueSet = false;
    }

}
