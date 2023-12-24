/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.undertow.server.accesslog;

import com.aspectran.utils.StringUtils;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import io.undertow.server.handlers.accesslog.AccessLogReceiver;

/**
 * Access log receiver that logs messages at INFO level.
 *
 * <p>Created: 2019-08-18</p>
 */
public class AspectranAccessLogReceiver implements AccessLogReceiver {

    private static final String DEFAULT_CATEGORY = "io.undertow.accesslog";

    private final Logger logger;

    public AspectranAccessLogReceiver() {
        this.logger = LoggerFactory.getLogger(DEFAULT_CATEGORY);
    }

    public AspectranAccessLogReceiver(String category) {
        if (StringUtils.hasText(category)) {
            this.logger = LoggerFactory.getLogger(category);
        } else {
            this.logger = LoggerFactory.getLogger(DEFAULT_CATEGORY);
        }
    }

    @Override
    public void logMessage(String message) {
        logger.info(message);
    }

}
