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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

/**
 * A utility class for safely accessing {@link System} properties, environment variables,
 * host networking, and runtime information.
 * <p>Provides methods to get, set, and clear system properties while gracefully
 * handling security exceptions.</p>
 */
public abstract class SystemUtils {

    private static final Logger logger = LoggerFactory.getLogger(SystemUtils.class);

    public static final String JAVA_IO_TMPDIR_PROPERTY = "java.io.tmpdir";

    public static final String USER_HOME_PROPERTY = "user.home";

    public static final String USER_DIR_PROPERTY = "user.dir";

    public static final String USER_NAME_PROPERTY = "user.name";

    public static final String OS_NAME_PROPERTY = "os.name";

    public static final String OS_VERSION_PROPERTY = "os.version";

    public static final String OS_ARCH_PROPERTY = "os.arch";

    public static final String JAVA_VERSION_PROPERTY = "java.version";

    private static final String OS_NAME = getProperty(OS_NAME_PROPERTY);

    /**
     * Gets a system property, returning {@code null} if the property cannot be read.
     * If a {@link SecurityException} is caught, the return value is {@code null}
     * and a debug message is logged.
     * @param key the name of the system property
     * @return the system property value, or {@code null} if a security error occurs
     */
    public static @Nullable String getProperty(@NonNull String key) {
        Assert.notNull(key, "key must not be null");
        try {
            return System.getProperty(key);
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception when accessing system property [{}]; " +
                        "its value will be returned [null]. Reason: {}", key, ex.getMessage());
            }
            return null;
        }
    }

    /**
     * Gets a system property, returning a default value if the property is not found or cannot be read.
     * @param key the name of the system property
     * @param defVal the default value to return
     * @return the system property value, or the default value if not found or a security error occurs
     */
    public static String getProperty(@NonNull String key, @Nullable String defVal) {
        String val = getProperty(key);
        return (val != null ? val : defVal);
    }

    /**
     * Sets a system property, returning the previous value.
     * If {@code value} is {@code null}, the property is cleared.
     * @param key the name of the system property
     * @param value the value to set, or {@code null} to clear
     * @return the previous string value of the system property, or {@code null}
     */
    public static @Nullable String setProperty(@NonNull String key, @Nullable String value) {
        Assert.notNull(key, "key must not be null");
        if (value == null) {
            return clearProperty(key);
        }
        try {
            return System.setProperty(key, value);
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception when setting system property [{}] to [{}]. Reason: {}",
                        key, value, ex.getMessage());
            }
            return null;
        }
    }

    /**
     * Clears a system property, returning the previous value.
     * If a {@link SecurityException} is caught, {@code null} is returned.
     * @param key the name of the system property to clear
     * @return the previous string value of the system property, or {@code null}
     */
    public static @Nullable String clearProperty(@NonNull String key) {
        Assert.notNull(key, "key must not be null");
        try {
            return System.clearProperty(key);
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception when clearing system property [{}]. Reason: {}",
                        key, ex.getMessage());
            }
            return null;
        }
    }

    /**
     * Gets an environment variable, returning {@code null} if it cannot be read.
     * @param name the name of the environment variable
     * @return the environment variable value, or {@code null} if not found or a security error occurs
     */
    public static @Nullable String getEnv(@NonNull String name) {
        Assert.notNull(name, "name must not be null");
        try {
            return System.getenv(name);
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception when accessing environment variable [{}]. Reason: {}",
                        name, ex.getMessage());
            }
            return null;
        }
    }

    /**
     * Gets an environment variable, returning a default value if not found.
     * @param name the name of the environment variable
     * @param defVal the default value to return
     * @return the environment variable value, or the default value
     */
    public static String getEnv(@NonNull String name, @Nullable String defVal) {
        String val = getEnv(name);
        return (val != null ? val : defVal);
    }

    /**
     * Retrieves a system property value, falling back to environment variables
     * if the property is not defined.
     * <p>When checking environment variables, both the exact name and a transformed
     * name (dots and dashes replaced by underscores, in uppercase) are attempted.</p>
     * @param name the property name
     * @return the resolved property or environment value, or {@code null}
     */
    public static @Nullable String getPropertyOrEnv(@NonNull String name) {
        return getPropertyOrEnv(name, null);
    }

    /**
     * Retrieves a system property value, falling back to environment variables
     * if the property is not defined.
     * @param name the property name
     * @param defVal the default value to return if not found
     * @return the resolved property or environment value, or {@code defVal}
     */
    public static String getPropertyOrEnv(@NonNull String name, @Nullable String defVal) {
        Assert.notNull(name, "name must not be null");
        String val = getProperty(name);
        if (val != null) {
            return val;
        }
        val = getEnv(name);
        if (val != null) {
            return val;
        }
        String envKey = name.replace('.', '_').replace('-', '_').toUpperCase(Locale.ROOT);
        val = getEnv(envKey);
        return (val != null ? val : defVal);
    }

    /**
     * Returns the value of the {@code java.io.tmpdir} system property.
     * @return the temporary directory path
     */
    public static @Nullable String getJavaIoTmpDir() {
        return getProperty(JAVA_IO_TMPDIR_PROPERTY);
    }

    /**
     * Returns the value of the {@code user.home} system property.
     * @return the user's home directory path
     */
    public static @Nullable String getUserHome() {
        return getProperty(USER_HOME_PROPERTY);
    }

    /**
     * Returns the value of the {@code user.dir} system property.
     * @return the user's current working directory path
     */
    public static @Nullable String getUserDir() {
        return getProperty(USER_DIR_PROPERTY);
    }

    /**
     * Returns the value of the {@code user.name} system property.
     * @return the current user name
     */
    public static @Nullable String getUserName() {
        return getProperty(USER_NAME_PROPERTY);
    }

    /**
     * Returns the value of the {@code os.name} system property.
     * @return the operating system name
     */
    public static @Nullable String getOsName() {
        return getProperty(OS_NAME_PROPERTY);
    }

    /**
     * Returns the value of the {@code os.version} system property.
     * @return the operating system version
     */
    public static @Nullable String getOsVersion() {
        return getProperty(OS_VERSION_PROPERTY);
    }

    /**
     * Returns the value of the {@code os.arch} system property.
     * @return the operating system architecture
     */
    public static @Nullable String getOsArch() {
        return getProperty(OS_ARCH_PROPERTY);
    }

    /**
     * Returns the value of the {@code java.version} system property.
     * @return the Java runtime version
     */
    public static @Nullable String getJavaVersion() {
        return getProperty(JAVA_VERSION_PROPERTY);
    }

    /**
     * Returns {@code true} if the current operating system is Windows.
     * @return {@code true} on Windows, {@code false} otherwise
     */
    public static boolean isWindows() {
        return (OS_NAME != null && OS_NAME.toLowerCase(Locale.ROOT).contains("windows"));
    }

    /**
     * Returns {@code true} if the current operating system is Linux.
     * @return {@code true} on Linux, {@code false} otherwise
     */
    public static boolean isLinux() {
        return (OS_NAME != null && OS_NAME.toLowerCase(Locale.ROOT).contains("linux"));
    }

    /**
     * Returns {@code true} if the current operating system is macOS.
     * @return {@code true} on macOS, {@code false} otherwise
     */
    public static boolean isMac() {
        return (OS_NAME != null && (OS_NAME.toLowerCase(Locale.ROOT).contains("mac")
                || OS_NAME.toLowerCase(Locale.ROOT).contains("darwin")));
    }

    /**
     * Returns {@code true} if the current operating system is a Unix-like system
     * (Linux, macOS, AIX, Solaris, etc.).
     * @return {@code true} on Unix-like OS, {@code false} otherwise
     */
    public static boolean isUnix() {
        if (OS_NAME == null) {
            return false;
        }
        String name = OS_NAME.toLowerCase(Locale.ROOT);
        return (name.contains("linux") || name.contains("mac") || name.contains("darwin")
                || name.contains("nix") || name.contains("nux") || name.contains("aix")
                || name.contains("solaris") || name.contains("sunos") || name.contains("freebsd"));
    }

    /**
     * Returns the process ID (PID) of the current Java virtual machine.
     * @return the current process ID
     */
    public static long getPid() {
        return ProcessHandle.current().pid();
    }

    /**
     * Returns the number of processors available to the Java virtual machine.
     * @return the number of available processors
     */
    public static int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Returns the maximum amount of memory that the Java virtual machine will attempt to use, in bytes.
     * @return the maximum memory in bytes
     */
    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * Returns the total amount of memory in the Java virtual machine, in bytes.
     * @return the total memory in bytes
     */
    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    /**
     * Returns the amount of free memory in the Java virtual machine, in bytes.
     * @return the free memory in bytes
     */
    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    /**
     * Returns the local IP address of the system.
     * <p>This method attempts to find a non-loopback IPv4 address.</p>
     * @return the local IP address, or "localhost" if no address is found
     */
    public static @NonNull String getLocalIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()) {
                        continue;
                    }
                    if (addr.getHostAddress().contains(":")) { // Skip IPv6
                        continue;
                    }
                    return addr.getHostAddress();
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
            return "localhost";
        }
    }

    /**
     * Returns a list of all non-loopback IPv4 addresses on the system.
     * @return the list of local IPv4 addresses
     */
    public static @NonNull List<String> getAllLocalIPs() {
        List<String> ips = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()) {
                        continue;
                    }
                    String hostAddress = addr.getHostAddress();
                    if (!hostAddress.contains(":") && !ips.contains(hostAddress)) {
                        ips.add(hostAddress);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return ips;
    }

    /**
     * Returns the local host name of the system.
     * @return the local host name, or "localhost" if it cannot be determined
     */
    public static @NonNull String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "localhost";
        }
    }

}
