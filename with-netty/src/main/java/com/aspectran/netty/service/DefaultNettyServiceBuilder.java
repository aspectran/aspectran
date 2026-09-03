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
package com.aspectran.netty.service;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.WebConfig;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.utils.Assert;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A builder class for creating and configuring {@link DefaultNettyService} instances.
 *
 * <p>Created: 2026-09-02</p>
 */
public class DefaultNettyServiceBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultNettyServiceBuilder.class);

    @NonNull
    public static DefaultNettyService build(CoreService parentService) {
        Assert.notNull(parentService, "parentService must not be null");
        DefaultNettyService nettyService = new DefaultNettyService(parentService, true);
        AspectranConfig aspectranConfig = parentService.getAspectranConfig();
        if (aspectranConfig != null) {
            WebConfig webConfig = aspectranConfig.getWebConfig();
            if (webConfig != null) {
                nettyService.configure(webConfig);
            }
        }
        setServiceStateListener(nettyService);
        return nettyService;
    }

    @NonNull
    public static DefaultNettyService build(CoreService parentService, AspectranConfig aspectranConfig) {
        Assert.notNull(aspectranConfig, "aspectranConfig must not be null");
        DefaultNettyService nettyService = new DefaultNettyService(parentService, false);
        nettyService.configure(aspectranConfig);
        setServiceStateListener(nettyService);
        return nettyService;
    }

    private static void setServiceStateListener(@NonNull DefaultNettyService nettyService) {
        nettyService.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                CoreServiceHolder.hold(nettyService);
                nettyService.pauseTimeout = 0L;
                if (nettyService.getNettyContext() != null) {
                    nettyService.getNettyContext().exportServerEndpoints();
                }
            }

            @Override
            public void stopped() {
                CoreServiceHolder.release(nettyService);
                nettyService.pauseTimeout = -1L;
            }

            @Override
            public void paused(long millis) {
                if (millis < 0L) {
                    nettyService.pauseTimeout = -1L;
                } else {
                    nettyService.pauseTimeout = System.currentTimeMillis() + millis;
                }
            }

            @Override
            public void paused() {
                nettyService.pauseTimeout = -1L;
            }

            @Override
            public void resumed() {
                nettyService.pauseTimeout = 0L;
            }
        });
    }

}
