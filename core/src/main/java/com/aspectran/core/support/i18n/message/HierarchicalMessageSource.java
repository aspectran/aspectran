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
package com.aspectran.core.support.i18n.message;

/**
 * Sub-interface of MessageSource to be implemented by objects that
 * can resolve messages hierarchically.
 *
 * <p>Created: 2016. 3. 8.</p>
 */
public interface HierarchicalMessageSource extends MessageSource {

    /**
     * Set the parent that will be used to try to resolve messages
     * that this object can't resolve.
     * @param parent the parent MessageSource that will be used to
     *      resolve messages that this object can't resolve.
     *      May be {@code null}, in which case no further resolution is possible.
     */
    void setParentMessageSource(MessageSource parent);

    /**
     * Return the parent of this MessageSource, or {@code null} if none.
     * @return the parent message source
     */
    MessageSource getParentMessageSource();

}
