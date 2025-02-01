/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.utils.logging.slf4j;

import com.aspectran.utils.logging.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

/**
 * <a href="http://www.slf4j.org/">SLF4J</a> logger.
 */
public class Slf4jLogger implements Logger {

    private final transient Logger logger;

    public Slf4jLogger(String name) {
        org.slf4j.Logger internalLogger = LoggerFactory.getLogger(name);
        if (internalLogger instanceof LocationAwareLogger locationAwareLogger) {
            this.logger = new Slf4jLocationAwareLoggerWrapper(locationAwareLogger);
        } else {
            // Logger is not LocationAwareLogger or slf4j version < 1.6
            this.logger = new Slf4jLoggerWrapper(internalLogger);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void error(String s) {
        logger.error(s);
    }

    @Override
    public void error(String s, Throwable e) {
        logger.error(s, e);
    }

    @Override
    public void debug(String s) {
        logger.debug(s);
    }

    @Override
    public void debug(String s, Throwable e) {
        logger.debug(s, e);
    }

    @Override
    public void info(String s) {
        logger.info(s);
    }

    @Override
    public void trace(String s) {
        logger.trace(s);
    }

    @Override
    public void warn(String s) {
        logger.warn(s);
    }

    @Override
    public void warn(String s, Throwable e) {
        logger.warn(s, e);
    }

}
