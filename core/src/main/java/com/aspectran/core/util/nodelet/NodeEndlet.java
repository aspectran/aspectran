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
package com.aspectran.core.util.nodelet;

/**
 * A nodelet is a sort of callback or event handler that can be registered
 * to handle an XPath event registered with the NodeParser.
 * In particular, nodelets for processing end elements, text, and CDATA data
 * are called NodeEndlet.
 *
 * <p>Created: 2017. 11. 2.</p>
 */
public interface NodeEndlet {

    /**
     * For a registered XPath, the NodeletParser will call the Nodelet's
     * process method for processing.
     *
     * @param text the text and CDATA data collected
     * @throws Exception if an error occurs while processing the nodelet
     */
    void process(String text) throws Exception;

}
