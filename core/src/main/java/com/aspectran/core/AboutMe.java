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
package com.aspectran.core;

import com.aspectran.utils.Assert;

import java.io.PrintStream;

/**
 * Class that exposes the build information of Aspectran.
 * Fetches the "Implementation-Version" manifest attribute from the jar file.
 */
public abstract class AboutMe {

    public static final String VERSION;

    public static final String POWERED_BY;

    public static final String POWERED_BY_LINK;

    public static final boolean STABLE;

    static {
        Package pkg = AboutMe.class.getPackage();
        if (pkg != null && "The Aspectran Project".equals(pkg.getImplementationVendor()) &&
                pkg.getImplementationVersion() != null) {
            VERSION = pkg.getImplementationVersion();
        } else {
            VERSION = System.getProperty("aspectran.version", "8.4.x");
        }

        POWERED_BY = "Powered by Aspectran " + VERSION;
        POWERED_BY_LINK = "<a href=\"https://aspectran.com\">Powered by Aspectran " + VERSION + "</a>";

        // Show warning when RC# or M# or -SNAPSHOT is in version string
        STABLE = !VERSION.matches("^.*[.-](RC|M|SNAPSHOT|x)[0-9]?$");
    }

    public static String getVersion() {
        return VERSION;
    }

    public static String getVersionDetail() {
        if (STABLE) {
            return VERSION;
        } else {
            return VERSION + " (THIS IS NOT A STABLE RELEASE! DO NOT USE IN PRODUCTION!)";
        }
    }

    public static String getPoweredBy() {
        return POWERED_BY;
    }

    public static String getPoweredByLink() {
        return POWERED_BY_LINK;
    }

    /**
     * Prints Aspectran information to the specified print stream.
     * @param output a {@link PrintStream} object to print
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
     * Prints Aspectran information to the specified print stream.
     * @param output a {@link PrintStream} object to print
     */
    public static void printPretty(PrintStream output) {
        Assert.notNull(output, "output must not be null");
        output.println("---------------------------------------------------------------------------------");
        output.printf(" %1$-9s : %2$s%n", "Aspectran", getVersionDetail());
        output.printf(" %1$-9s : %2$s%n", "JVM", System.getProperty("java.vm.name") + " (build " +
                System.getProperty("java.vm.version") + ", " + System.getProperty("java.vm.info") + ")");
        output.printf(" %1$-9s : %2$s%n", "OS", System.getProperty("os.name") + " " +
                System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        output.println("---------------------------------------------------------------------------------");
    }

    /**
     * Prints Aspectran information to {@link System#out}.
     * @param args a string array containing the command line arguments
     */
    public static void main(String[] args) {
        printPretty(System.out);
    }

}
