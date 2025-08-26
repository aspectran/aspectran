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
package com.aspectran.undertow.server.handler.accesslog;

import com.aspectran.utils.StringUtils;
import io.undertow.server.handlers.accesslog.AccessLogReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AccessLogReceiver} implementation that logs access messages via SLF4J.
 * <p>This class acts as a bridge, directing Undertow's access log output to a
 * configurable SLF4J logger, typically for routing to a dedicated access log file.</p>
 *
 * <p>Created: 2019-08-18</p>
 */
public class TowAccessLogReceiver implements AccessLogReceiver {

    private static final String DEFAULT_CATEGORY = "io.undertow.accesslog";

    private final Logger logger;

    /**
     * Constructs a new TowAccessLogReceiver with the default logger category.
     */
    public TowAccessLogReceiver() {
        this(null);
    }

    /**
     * Constructs a new TowAccessLogReceiver with a specified logger category.
     * @param category the SLF4J logger category to use
     */
    public TowAccessLogReceiver(String category) {
        if (StringUtils.hasText(category)) {
            this.logger = LoggerFactory.getLogger(category);
        } else {
            this.logger = LoggerFactory.getLogger(DEFAULT_CATEGORY);
        }
    }

    /**
     * Logs the formatted access log message at the INFO level.
     * @param message the access log message string
     */
    @Override
    public void logMessage(String message) {
        logger.info(message);
    }

}
