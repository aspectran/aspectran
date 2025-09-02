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

/**
 * An {@link Nodelet} variant specifically designed to handle events related to
 * the end of an XML element, or the processing of text and CDATA content.
 * <p>This interface is used when the {@link NodeletParser} encounters an end tag,
 * or when it has finished collecting text or CDATA within an element.</p>
 *
 * <p>Created: 2017. 11. 2.</p>
 */
public interface EndNodelet {

    /**
     * Processes the collected text and CDATA data for a registered XPath.
     * <p>The {@link NodeletParser} will call this method when it encounters the
     * corresponding element's end tag.</p>
     * @param text the accumulated text and CDATA content within the element; may be null if the element is empty
     * @throws Exception if an error occurs while processing the nodelet
     */
    void process(String text) throws Exception;

}
