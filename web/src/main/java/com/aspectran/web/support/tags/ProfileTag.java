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

import java.io.Serial;

/**
 * The {@code <profile>} tag useful to display a piece of content in a JSP file,
 * based on a specific environment profile.
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
 * <td>expression</td>
 * <td>true</td>
 * <td>false</td>
 * <td>The profile expression to include.</td>
 * </tr>
 * </tbody>
 * </table>
 *
 * <p>Created: 2024/01/09</p>
 */
public class ProfileTag extends CurrentActivityAwareTag {

    @Serial
    private static final long serialVersionUID = -3879817095317245267L;

    private String expression;

    /**
     * Set the expression for profiles to include or exclude.
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    protected int doStartTagInternal() throws Exception {
        if (getCurrentActivity().getEnvironment().matchesProfiles(this.expression)) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

}
