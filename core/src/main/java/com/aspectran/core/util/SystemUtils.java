/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
package com.aspectran.core.util;

import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;

import java.security.AccessControlException;

/**
 * Helpers for java.lang.System.
 */
public class SystemUtils {

    private static final Logger logger = LoggerFactory.getLogger(SystemUtils.class);

    private static final String JAVA_IO_TMPDIR_PROPERTY = "java.io.tmpdir";

    /**
     * <p>Gets a System property, defaulting to <code>null</code> if the property
     * cannot be read.</p>
     * <p>If a <code>SecurityException</code> is caught, the return value is <code>null</code>.</p>
     * @param name the system property name
     * @return the system property value or <code>null</code> if a security problem occurs
     */
    public static String getProperty(String name) {
        try {
            return System.getProperty(name);
        } catch (AccessControlException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format(
                        "Caught AccessControlException when accessing system property [%s]; " +
                                "its value will be returned [null]. Reason: %s",
                        name, ex.getMessage()));
            }
        }
        return null;
    }

    public static String getProperty(String name, String defVal) {
        String val = getProperty(name);
        return (val != null ? val : defVal);
    }

    public static String getJavaIoTmpDir() {
        return getProperty(JAVA_IO_TMPDIR_PROPERTY);
    }

}
