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

/**
 * The Class Log4j2LoggerWrapper.
 */
public class Log4j2LoggerWrapper implements Logger {

    private final transient org.apache.logging.log4j.Logger internalLogger;

    Log4j2LoggerWrapper(org.apache.logging.log4j.Logger internalLogger) {
        this.internalLogger = internalLogger;
    }

    @Override
    public boolean isDebugEnabled() {
        return internalLogger.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return internalLogger.isTraceEnabled();
    }

    @Override
    public void error(String s) {
        internalLogger.error(s);
    }

    @Override
    public void error(String s, Throwable e) {
        internalLogger.error(s, e);
    }

    @Override
    public void debug(String s) {
        internalLogger.debug(s);
    }

    @Override
    public void debug(String s, Throwable e) {
        internalLogger.debug(s, e);
    }

    @Override
    public void info(String s) {
        internalLogger.info(s);
    }

    @Override
    public void trace(String s) {
        internalLogger.trace(s);
    }

    @Override
    public void warn(String s) {
        internalLogger.warn(s);
    }

    @Override
    public void warn(String s, Throwable e) {
        internalLogger.warn(s, e);
    }

}
