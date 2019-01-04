/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.util.logging.nologging;

import com.aspectran.core.util.logging.Log;

/**
 * @author Clinton Begin
 */
public class NoLoggingImpl implements Log {

    public NoLoggingImpl(String clazz) {
        // Do Nothing
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void error(String s) {
        // Do Nothing
    }

    @Override
    public void error(String s, Throwable e) {
        // Do Nothing
    }

    @Override
    public void debug(String s) {
        // Do Nothing
    }

    @Override
    public void debug(String s, Throwable e) {
        // Do Nothing
    }

    @Override
    public void info(String s) {
        // Do Nothing
    }

    @Override
    public void trace(String s) {
        // Do Nothing
    }

    @Override
    public void warn(String s) {
        // Do Nothing
    }

    @Override
    public void warn(String s, Throwable e) {
        // Do Nothing
    }

}
