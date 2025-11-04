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
package com.aspectran.core;

import com.aspectran.utils.Assert;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides information about the Aspectran framework build.
 * <p>This class retrieves the version of Aspectran from the manifest file
 * of the JAR in which it is packaged.</p>
 */
public abstract class AboutMe {

    /** The version of Aspectran. */
    public static final String VERSION;

    /** A string indicating that the application is "Powered by Aspectran" with the version. */
    public static final String POWERED_BY;

    /** An HTML link for "Powered by Aspectran" with the version. */
    public static final String POWERED_BY_LINK;

    /** A boolean indicating whether the current version is a stable release. */
    public static final boolean STABLE;

    static {
        Package pkg = AboutMe.class.getPackage();
        if (pkg != null && "The Aspectran Project".equals(pkg.getImplementationVendor()) &&
                pkg.getImplementationVersion() != null) {
            VERSION = pkg.getImplementationVersion();
        } else {
            VERSION = System.getProperty("aspectran.version", "9.3.x");
        }

        POWERED_BY = "Powered by Aspectran " + VERSION;
        POWERED_BY_LINK = "<a href=\"https://aspectran.com\">Powered by Aspectran " + VERSION + "</a>";

        // Show warning when RC# or M# or -SNAPSHOT is in version string
        STABLE = !VERSION.matches("^.*[.-](RC|M|SNAPSHOT|x)[0-9]?$");
    }

    /**
     * Returns the version of Aspectran.
     * @return the version string
     */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * Returns the detailed version information of Aspectran.
     * If the version is not a stable release, a warning message is appended.
     * @return the detailed version string
     */
    public static String getVersionDetail() {
        if (STABLE) {
            return VERSION;
        } else {
            return VERSION + " (THIS IS NOT A STABLE RELEASE! DO NOT USE IN PRODUCTION!)";
        }
    }

    /**
     * Returns the "Powered by" string.
     * @return the "Powered by" string
     */
    public static String getPoweredBy() {
        return POWERED_BY;
    }

    /**
     * Returns the "Powered by" HTML link.
     * @return the "Powered by" HTML link
     */
    public static String getPoweredByLink() {
        return POWERED_BY_LINK;
    }

    /**
     * Prints the Aspectran version and system information to the specified {@link PrintStream}.
     * @param output a {@link PrintStream} object to print to
     */
    public static void print(PrintStream output) {
        Assert.notNull(output, "output must not be null");
        output.println("Aspectran: " + getVersionDetail());
        output.println("JVM: " + System.getProperty("java.vm.name") + " (build " +
                System.getProperty("java.vm.version") + ", " + System.getProperty("java.vm.info") + ")");
        output.println("OS: " + System.getProperty("os.name") + " " +
                System.getProperty("os.version") + " " + System.getProperty("os.arch"));
    }

    /**
     * Prints the Aspectran version and system information in a pretty format to the specified {@link PrintStream}.
     * @param output a {@link PrintStream} object to print to
     */
    public static void printPretty(PrintStream output) {
        Assert.notNull(output, "output must not be null");

        Map<String, String> info = new LinkedHashMap<>();
        info.put("Aspectran", getVersionDetail());
        info.put("JVM", System.getProperty("java.vm.name") + " (build " +
                System.getProperty("java.vm.version") + ", " + System.getProperty("java.vm.info") + ")");
        info.put("OS", System.getProperty("os.name") + " " +
                System.getProperty("os.version") + " " + System.getProperty("os.arch"));

        int maxKeyLength = 0;
        for (String key : info.keySet()) {
            if (key.length() > maxKeyLength) {
                maxKeyLength = key.length();
            }
        }

        String format = " %1$-" + maxKeyLength + "s : %2$s%n";
        String line = "-".repeat(80);

        output.println(line);
        for (Map.Entry<String, String> entry : info.entrySet()) {
            output.printf(format, entry.getKey(), entry.getValue());
        }
        output.println(line);
    }

    /**
     * Prints the Aspectran version and system information in a pretty format to {@link System#out}.
     * @param args a string array containing the command line arguments
     */
    public static void main(String[] args) {
        printPretty(System.out);
    }

}
