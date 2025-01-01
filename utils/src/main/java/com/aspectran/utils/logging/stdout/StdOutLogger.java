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
package com.aspectran.utils.logging.stdout;

import com.aspectran.utils.logging.Logger;

/**
 * A {@link Logger} that logs directly to the standard output stream (System.out)
 * or standard error output stream (System.err) depending on the log level.
 */
public class StdOutLogger implements Logger {

    public StdOutLogger(String name) {
        // Do Nothing
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public void error(String s, Throwable e) {
        System.err.println(s);
        if (e != null) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void error(String s) {
        System.err.println(s);
    }

    @Override
    public void debug(String s) {
        System.out.println(s);
    }

    @Override
    public void debug(String s, Throwable e) {
        System.out.println(s);
        if (e != null) {
            e.printStackTrace(System.out);
        }
    }

    @Override
    public void info(String s) {
        System.out.println(s);
    }

    @Override
    public void trace(String s) {
        System.out.println(s);
    }

    @Override
    public void warn(String s) {
        System.out.println(s);
    }

    @Override
    public void warn(String s, Throwable e) {
        System.out.println(s);
        if (e != null) {
            e.printStackTrace(System.out);
        }
    }

}
