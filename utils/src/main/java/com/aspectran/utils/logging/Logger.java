/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.utils.logging;

/**
 * This class provides a static logging interface.
 */
public interface Logger {

    boolean isDebugEnabled();

    boolean isTraceEnabled();

    void error(String s);

    void error(String s, Throwable e);

    default void error(Throwable e) {
        error(LoggerFactory.EXCEPTION_MESSAGE + e.getMessage(), e);
    }

    void debug(String s);

    void debug(String s, Throwable e);

    default void debug(Throwable e) {
        debug(LoggerFactory.EXCEPTION_MESSAGE + e.getMessage(), e);
    }

    void info(String s);

    void trace(String s);

    void warn(String s);

    void warn(String s, Throwable e);

    default void warn(Throwable e) {
        warn(LoggerFactory.EXCEPTION_MESSAGE + e.getMessage(), e);
    }

}
