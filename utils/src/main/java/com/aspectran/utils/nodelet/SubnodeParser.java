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
 * An interface for parsing sub-nodes within a larger document structure.
 * <p>Implementations of this interface are responsible for registering specific
 * {@link Nodelet}s and {@link EndNodelet}s with a {@link NodeletParser}
 * for a given XPath, effectively defining how a portion of the document should be parsed.</p>
 */
public interface SubnodeParser {

    /**
     * Parses sub-nodes starting from the given XPath and registers corresponding
     * nodelets with the provided {@link NodeletParser}.
     * @param xpath the XPath to the sub-node to parse
     * @param parser the {@link NodeletParser} to register nodelets with
     */
    void parse(String xpath, NodeletParser parser);

}
