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
package com.aspectran.undertow.server.handler.resource;

import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.wildcard.IncludeExcludeParameters;

/**
 * A specialized {@link IncludeExcludeParameters} class for defining path patterns
 * for static resource handling.
 * <p>This class can be configured as a bean to specify which URL paths should be
 * included or excluded from being served as static resources by the {@link TowResourceHandler}.</p>
 */
public class ResourcePathPatterns extends IncludeExcludeParameters {

    /**
     * Constructs a new, empty ResourcePathPatterns instance.
     */
    public ResourcePathPatterns() {
        super();
    }

    /**
     * Constructs a new ResourcePathPatterns instance from an APON string.
     * @param apon the APON string containing include/exclude patterns
     * @throws AponParseException if the APON string is invalid
     */
    public ResourcePathPatterns(String apon) throws AponParseException {
        super();
        readFrom(apon);
    }

}
