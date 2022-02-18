/*
 * Copyright (c) 2008-2022 The Aspectran Project
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

import com.aspectran.core.lang.Nullable;
import jakarta.servlet.jsp.JspTagException;

/**
 * Allows implementing tag to utilize nested {@code spring:argument} tags.
 */
public interface ArgumentAware {

    /**
     * Callback hook for nested spring:argument tags to pass their value
     * to the parent tag.
     * @param argument the result of the nested {@code spring:argument} tag
     */
    void addArgument(@Nullable Object argument) throws JspTagException;

}
