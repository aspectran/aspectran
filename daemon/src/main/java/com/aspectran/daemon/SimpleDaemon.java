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
package com.aspectran.daemon;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.DaemonConfig;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * Lightweight daemon variant primarily intended for executing daemon commands.
 * <p>
 * SimpleDaemon prepares an {@link com.aspectran.core.context.ActivityContext} with only
 * the daemon subsystem active (i.e., without a full web or shell environment) and delegates
 * lifecycle operations to {@link AbstractDaemon}. It is useful when you want to
 * bootstrap just enough infrastructure to run scheduled tasks or ad-hoc daemon commands
 * outside of a full application container.
 * </p>
 *
 * <p>Created: 2019. 01. 22.</p>
 * @since 6.0.0
 */
public class SimpleDaemon extends AbstractDaemon {

    /**
     * Creates a new SimpleDaemon instance.
     */
    public SimpleDaemon() {
        super();
    }

    /**
     * Prepares the daemon using the provided {@link DaemonConfig} while keeping the rest of the
     * {@link AspectranConfig} minimal.
     * <p>
     * Internally, this method creates a fresh {@link AspectranConfig}, sets the given
     * {@link DaemonConfig} on it, and delegates to {@link AbstractDaemon#prepare(String, AspectranConfig)}.
     * If preparation fails for any reason, {@link #destroy()} is invoked to clean up partially
     * initialized resources before rethrowing the original exception.
     * </p>
     *
     * @param basePath optional base path used to resolve resources; may be {@code null}
     * @param daemonConfig the daemon configuration to use; never {@code null}
     * @throws Exception if preparation fails
     */
    public void prepare(@Nullable String basePath, @NonNull DaemonConfig daemonConfig) throws Exception {
        AspectranConfig aspectranConfig = new AspectranConfig();
        aspectranConfig.setDaemonConfig(daemonConfig);
        try {
            prepare(basePath, aspectranConfig);
        } catch (Exception e) {
            destroy();
            throw e;
        }
    }

    /**
     * Starts the daemon in non-blocking mode.
     * <p>
     * Delegates to {@link AbstractDaemon#start()}.
     * </p>
     * @throws Exception if the daemon cannot be started
     */
    @Override
    public void start() throws Exception {
        super.start();
    }

    /**
     * Starts the daemon and optionally waits for completion.
     * <p>
     * Delegates to {@link AbstractDaemon#start(boolean)}.
     * </p>
     * @param wait whether to block the current thread until the daemon stops
     * @throws Exception if the daemon cannot be started
     */
    @Override
    public void start(boolean wait) throws Exception {
        super.start(wait);
    }

    /**
     * Starts the daemon and waits up to the given timeout before returning.
     * <p>
     * Delegates to {@link AbstractDaemon#start(long)}.
     * </p>
     * @param wait the maximum time to wait in milliseconds (0 for non-blocking)
     * @throws Exception if the daemon cannot be started
     */
    @Override
    public void start(long wait) throws Exception {
        super.start(wait);
    }

}
