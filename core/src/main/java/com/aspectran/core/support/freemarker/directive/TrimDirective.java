/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.support.freemarker.directive;

/**
 * The Class TrimDirective.
 *
 * <dl>
 * <dt>Basically, this trims the body string(removing leading and tailing spaces).
 * <dt>If the result of trimming is empty, it will return just empty string.
 * <dt>prefix="string" <dd>If the result of trimming is not empty, prefix "string" to the result.
 * <dt>suffix="string" <dd>If the result of trim is not empty, suffix "string" to the result.
 * <dt>deprefixes=["string1", "string2", ...] <dd>If the result of trimming is not empty,
 * the first appearing string in the leading of the result will be removed.
 * <dt>desuffixes=["string1", "string2", ...] <dd>If the result of trimming is not empty,
 * the first appearing string in the tail of the result will be removed.
 * <dt>caseSensitive="true" or "false" <dd>true to case sensitive; false to ignore case sensitive.
 * </dl>
 *
 * <p>Created: 2016. 1. 29.</p>
 */
public interface TrimDirective {

    String PREFIX_PARAM_NAME = "prefix";

    String SUFFIX_PARAM_NAME = "suffix";

    String DEPREFIXES_PARAM_NAME = "deprefixes";

    String DESUFFIXES_PARAM_NAME = "desuffixes";

    String CASE_SENSITIVE_PARAM_NAME = "caseSensitive";

    /**
     * Gets group name.
     *
     * @return the group name
     */
    String getGroupName();

    /**
     * Gets directive name.
     *
     * @return the directive name
     */
    String getDirectiveName();

}
