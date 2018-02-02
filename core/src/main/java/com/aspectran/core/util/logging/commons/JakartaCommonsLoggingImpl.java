/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.util.logging.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Clinton Begin
 */
public class JakartaCommonsLoggingImpl implements com.aspectran.core.util.logging.Log {

    private Log log;

    public JakartaCommonsLoggingImpl(String clazz) {
        log = LogFactory.getLog(clazz);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public void error(String s) {
        log.error(s);
    }

    @Override
    public void error(String s, Throwable e) {
        log.error(s, e);
    }

    @Override
    public void debug(String s) {
        log.debug(s);
    }

    @Override
    public void debug(String s, Throwable e) {
        log.debug(s, e);
    }

    @Override
    public void info(String s) {
        log.info(s);
    }

    @Override
    public void trace(String s) {
        log.trace(s);
    }

    @Override
    public void warn(String s) {
        log.warn(s);
    }

    @Override
    public void warn(String s, Throwable e) {
        log.warn(s, e);
    }

}
