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
import org.slf4j.spi.LocationAwareLogger;

/**
 * The Class Slf4jLocationAwareLoggerWrapper.
 */
class Slf4jLocationAwareLoggerWrapper implements Logger {

    private static final String FQCN = Slf4jLogger.class.getName();

    private final transient LocationAwareLogger locationAwareLogger;

    Slf4jLocationAwareLoggerWrapper(LocationAwareLogger locationAwareLogger) {
        this.locationAwareLogger = locationAwareLogger;
    }

    @Override
    public boolean isDebugEnabled() {
        return locationAwareLogger.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return locationAwareLogger.isTraceEnabled();
    }

    @Override
    public void error(String s, Throwable e) {
        locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, s, null, e);
    }

    @Override
    public void error(String s) {
        locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, s, null, null);
    }

    @Override
    public void debug(String s) {
        locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, s, null, null);
    }

    @Override
    public void debug(String s, Throwable e) {
        locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, s, null, e);
    }

    @Override
    public void info(String s) {
        locationAwareLogger.log(null, FQCN, LocationAwareLogger.INFO_INT, s, null, null);
    }

    @Override
    public void trace(String s) {
        locationAwareLogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, s, null, null);
    }

    @Override
    public void warn(String s) {
        locationAwareLogger.log(null, FQCN, LocationAwareLogger.WARN_INT, s, null, null);
    }

    @Override
    public void warn(String s, Throwable e) {
        locationAwareLogger.log(null, FQCN, LocationAwareLogger.WARN_INT, s, null, e);
    }

}
