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
 * Enumeration of the possible outcomes of a graceful shutdown attempt.
 * This result is passed to the {@link GracefulShutdownCallback} to indicate the final state.
 *
 * <p>Created: 1/21/24</p>
 */
public enum GracefulShutdownResult {

    /**
     * The server was shut down, but some requests were still active when the grace period ended.
     * This may imply a forceful termination of in-flight requests.
     */
    REQUESTS_ACTIVE,

    /**
     * The server had no active requests and was able to shut down cleanly.
     * This is the ideal outcome for a graceful shutdown.
     */
    IDLE,

    /**
     * The server was shut down immediately, without waiting for active requests to complete.
     */
    IMMEDIATE

}
