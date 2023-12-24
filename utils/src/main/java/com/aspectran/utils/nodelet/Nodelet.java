/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
 * A nodelet is a sort of callback or event handler that can be registered
 * to handle an XPath event registered with the NodeParser.
 */
public interface Nodelet {

    /**
     * For a registered XPath, the NodeletParser will call the Nodelet's
     * process method for processing.
     * @param attrs the attributes of the start element
     * @throws Exception if an error occurs while processing the nodelet
     */
    void process(Map<String, String> attrs) throws Exception;

}
