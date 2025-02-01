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
package com.aspectran.utils.logging.log4j2;

import com.aspectran.utils.logging.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.ExtendedLogger;

/**
 * <a href="https://logging.apache.org/log4j/2.x/">Apache Log4j 2</a> logger.
 */
public class Log4j2Logger implements Logger {

    private final transient Logger logger;

    public Log4j2Logger(String name) {
        org.apache.logging.log4j.Logger internalLogger = LogManager.getLogger(name);
        if (internalLogger instanceof ExtendedLogger extendedLogger) {
            this.logger = new Log4j2ExtendedLoggerWrapper(extendedLogger);
        } else {
            // It is not a logger that extends AbstractLogger
            this.logger = new Log4j2LoggerWrapper(internalLogger);
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
    public void error(String s, Throwable e) {
        logger.error(s, e);
    }

    @Override
    public void error(String s) {
        logger.error(s);
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
