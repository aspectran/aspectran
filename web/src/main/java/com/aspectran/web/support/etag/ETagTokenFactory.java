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
package com.aspectran.web.support.etag;

import com.aspectran.core.activity.Translet;

/**
 * A factory for creating a token that will be used to generate an ETag.
 * <p>Implementations of this interface are responsible for providing the raw
 * data (as a byte array) that uniquely represents the state of a resource.
 * The {@link ETagInterceptor} will then compute a hash of this data to create
 * the final ETag value.</p>
 */
public interface ETagTokenFactory {

    /**
     * Returns a byte array that represents the state of the resource.
     * @param translet the current translet
     * @return a byte array representing the resource state
     */
    byte[] getToken(Translet translet);

}
