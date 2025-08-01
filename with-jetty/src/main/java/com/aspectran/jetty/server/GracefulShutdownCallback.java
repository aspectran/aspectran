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
package com.aspectran.jetty.server;

/**
 * A callback for the result of a graceful shutdown request.
 *
 * <p>Created: 1/21/24</p>
 */
@FunctionalInterface
public interface GracefulShutdownCallback {

    /**
     * Graceful shutdown has completed with the given {@code result}.
     * @param result the result of the shutdown
     */
    void shutdownComplete(GracefulShutdownResult result);

}
