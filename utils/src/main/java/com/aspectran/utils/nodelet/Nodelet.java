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

import java.util.Map;

/**
 * A Nodelet is a callback or event handler that can be registered
 * to process a specific XPath event during document parsing.
 * <p>It acts as a modular piece of logic that executes when a particular
 * node (identified by its XPath) is encountered by a {@link NodeletParser}.</p>
 */
public interface Nodelet {

    /**
     * Processes the node event for a registered XPath.
     * <p>The {@link NodeletParser} will call this method when it encounters
     * the corresponding element's start tag in the document.</p>
     * @param attrs a map of attributes from the start element, or an empty map if there are no attributes
     * @throws Exception if an error occurs while processing the nodelet
     */
    void process(Map<String, String> attrs) throws Exception;

}
