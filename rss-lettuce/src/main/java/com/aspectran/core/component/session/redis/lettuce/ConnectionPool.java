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
package com.aspectran.core.component.session.redis.lettuce;

/**
 * Abstraction for a pool that supplies Lettuce connections and manages their lifecycle.
 * <p>
 * Implementations create, borrow, and return underlying Lettuce connections and
 * provide simple hooks to initialize with a {@link SessionDataCodec} and to destroy
 * resources when the pool is no longer needed.
 * </p>
 *
 * <p>Created: 2019/12/23</p>
 */
public interface ConnectionPool<T> {

    /**
     * Borrows a connection from the pool.
     * @return a ready-to-use connection
     * @throws Exception if a connection cannot be obtained
     */
    T getConnection() throws Exception;

    /**
     * Initializes the pool with the given session codec for value serialization.
     * @param codec the codec used to encode/decode {@code SessionData}
     */
    void initialize(SessionDataCodec codec);

    /**
     * Destroys this pool and releases all underlying resources.
     */
    void destroy();

}
