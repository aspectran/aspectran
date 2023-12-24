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
package com.aspectran.utils.logging.jdk14;

import com.aspectran.utils.logging.Logger;

import java.util.logging.Level;

/**
 * <a href="http://java.sun.com/javase/6/docs/technotes/guides/logging/index.html">java.util.logging</a> logger.
 */
public class Jdk14Logger implements Logger {

    private final transient java.util.logging.Logger internalLogger;

    public Jdk14Logger(String name) {
        internalLogger = java.util.logging.Logger.getLogger(name);
    }

    @Override
    public boolean isDebugEnabled() {
        return internalLogger.isLoggable(Level.FINE);
    }

    @Override
    public boolean isTraceEnabled() {
        return internalLogger.isLoggable(Level.FINER);
    }

    @Override
    public void error(String s) {
        internalLogger.log(Level.SEVERE, s);
    }

    @Override
    public void error(String s, Throwable e) {
        internalLogger.log(Level.SEVERE, s, e);
    }

    @Override
    public void debug(String s) {
        internalLogger.log(Level.FINE, s);
    }

    @Override
    public void debug(String s, Throwable e) {
        internalLogger.log(Level.FINE, s, e);
    }

    @Override
    public void info(String s) {
        internalLogger.log(Level.INFO, s);
    }

    @Override
    public void trace(String s) {
        internalLogger.log(Level.FINER, s);
    }

    @Override
    public void warn(String s) {
        internalLogger.log(Level.WARNING, s);
    }

    @Override
    public void warn(String s, Throwable e) {
        internalLogger.log(Level.WARNING, s, e);
    }

}
