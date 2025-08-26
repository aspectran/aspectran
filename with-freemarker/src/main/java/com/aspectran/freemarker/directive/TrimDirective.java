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
package com.aspectran.freemarker.directive;

/**
 * The base interface for custom trim directives in FreeMarker.
 * <p>Implementations of this interface define a custom directive that can be used in
 * FreeMarker templates to trim the body content and optionally add or remove
 * prefixes and suffixes. This is particularly useful for dynamically generating
 * structured text like SQL queries, where you might need to remove leading "AND"s
 * or wrap a non-empty block with parentheses.</p>
 *
 * <p>A typical trim directive would support the following parameters:</p>
 * <ul>
 *   <li><b>prefix</b>: A string to prepend to the output if the trimmed body is not empty.</li>
 *   <li><b>suffix</b>: A string to append to the output if the trimmed body is not empty.</li>
 *   <li><b>deprefixes</b>: An array of strings to remove from the beginning of the body.</li>
 *   <li><b>desuffixes</b>: An array of strings to remove from the end of the body.</li>
 *   <li><b>caseSensitive</b>: A boolean indicating if prefix/suffix removal is case-sensitive.</li>
 * </ul>
 *
 * @since 2016. 1. 29.
 */
public interface TrimDirective {

    /** The parameter name for specifying a prefix to add. */
    String PREFIX_PARAM_NAME = "prefix";

    /** The parameter name for specifying a suffix to add. */
    String SUFFIX_PARAM_NAME = "suffix";

    /** The parameter name for specifying prefixes to remove. */
    String DEPREFIXES_PARAM_NAME = "deprefixes";

    /** The parameter name for specifying suffixes to remove. */
    String DESUFFIXES_PARAM_NAME = "desuffixes";

    /** The parameter name for specifying case sensitivity. */
    String CASE_SENSITIVE_PARAM_NAME = "caseSensitive";

    /**
     * Returns the name of the group this directive belongs to.
     * <p>For example, in {@code @sql.where}, "sql" is the group name.</p>
     * @return the group name
     */
    String getGroupName();

    /**
     * Returns the name of the directive within its group.
     * <p>For example, in {@code @sql.where}, "where" is the directive name.</p>
     * @return the directive name
     */
    String getDirectiveName();

}
