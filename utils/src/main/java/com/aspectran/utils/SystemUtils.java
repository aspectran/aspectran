/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.utils;

import com.aspectran.utils.annotation.jsr305.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for safely accessing {@link System} properties.
 * <p>Provides methods to get and clear system properties while gracefully
 * handling security exceptions.</p>
 */
public abstract class SystemUtils {

    private static final Logger logger = LoggerFactory.getLogger(SystemUtils.class);

    private static final String JAVA_IO_TMPDIR_PROPERTY = "java.io.tmpdir";

    private static final String USER_HOME_PROPERTY = "user.home";

    private static final String USER_DIR_PROPERTY = "user.dir";

    /**
     * Gets a system property, returning {@code null} if the property cannot be read.
     * If a {@link SecurityException} is caught, the return value is {@code null}
     * and a debug message is logged.
     *
     * @param key the name of the system property
     * @return the system property value, or {@code null} if a security error occurs
     */
    @Nullable
    public static String getProperty(String key) {
        try {
            return System.getProperty(key);
        } catch (Exception ex) { // AccessControlException is deprecated for removal
            if (logger.isDebugEnabled()) {
                logger.debug("Caught AccessControlException when accessing system property [{}]; " +
                        "its value will be returned [null]. Reason: {}", key, ex.getMessage());
            }
            return null;
        }
    }

    /**
     * Gets a system property, returning a default value if the property is not found or cannot be read.
     *
     * @param name the name of the system property
     * @param defVal the default value to return
     * @return the system property value, or the default value if not found or a security error occurs
     */
    public static String getProperty(String name, String defVal) {
        String val = getProperty(name);
        return (val != null ? val : defVal);
    }

    /**
     * Clears a system property, returning the previous value.
     * If a {@link SecurityException} is caught, {@code null} is returned.
     *
     * @param key the name of the system property to clear
     * @return the previous string value of the system property, or {@code null}
     */
    @Nullable
    public static String clearProperty(String key) {
        try {
            return System.clearProperty(key);
        } catch (Exception ex) { // AccessControlException is deprecated for removal
            if (logger.isDebugEnabled()) {
                logger.debug("Caught AccessControlException when accessing system property [{}]. Reason: {}",
                        key, ex.getMessage());
            }
            return null;
        }
    }

    /**
     * Returns the value of the {@code java.io.tmpdir} system property.
     *
     * @return the temporary directory path
     */
    public static String getJavaIoTmpDir() {
        return getProperty(JAVA_IO_TMPDIR_PROPERTY);
    }

    /**
     * Returns the value of the {@code user.home} system property.
     *
     * @return the user's home directory path
     */
    public static String getUserHome() {
        return getProperty(USER_HOME_PROPERTY);
    }

    /**
     * Returns the value of the {@code user.dir} system property.
     *
     * @return the user's current working directory path
     */
    public static String getUserDir() {
        return getProperty(USER_DIR_PROPERTY);
    }

}
