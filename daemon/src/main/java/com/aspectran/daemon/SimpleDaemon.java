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
 * Since SimpleDaemon has an empty Activity context, it should be
 * used primarily to execute daemon commands.
 *
 * <p>Created: 2019. 01. 22.</p>
 *
 * @since 6.0.0
 */
public class SimpleDaemon extends AbstractDaemon {

    public SimpleDaemon() {
        super();
    }

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

    @Override
    public void start() throws Exception {
        super.start();
    }

    @Override
    public void start(boolean wait) throws Exception {
        super.start(wait);
    }

    @Override
    public void start(long wait) throws Exception {
        super.start(wait);
    }

}
