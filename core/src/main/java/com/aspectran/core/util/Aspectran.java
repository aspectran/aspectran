/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import java.io.PrintStream;

/**
 * Class that exposes the build information of Aspectran.
 * Fetches the "Implementation-Version" manifest attribute from the jar file.
 */
public class Aspectran {

    public static final String VERSION;

    public static final String POWERED_BY;

    public static final String POWERED_BY_LINK;

    public static final boolean STABLE;

    static {
        Package pkg = Aspectran.class.getPackage();
        if (pkg != null && "The Aspectran Project".equals(pkg.getImplementationVendor()) &&
                pkg.getImplementationVersion() != null) {
            VERSION = pkg.getImplementationVersion();
        } else {
            VERSION = System.getProperty("aspectran.version", "7.3.x");
        }

        POWERED_BY = "Powered by Aspectran " + VERSION;
        POWERED_BY_LINK = "<a href=\"https://aspectran.com\">Powered by Aspectran " + VERSION + "</a>";

        // Show warning when RC# or M# is in version string
        STABLE = !VERSION.matches("^.*\\.(RC|M)[0-9]+$");
    }

    /**
     * No public constructor to prevent instances from being created.
     */
    private Aspectran() {
    }

    public static String getVersion() {
        return VERSION;
    }

    public static String getPoweredBy() {
        return POWERED_BY;
    }

    public static String getPoweredByLink() {
        return POWERED_BY_LINK;
    }

    /**
     * Prints Aspectran information to the specified print stream.
     * @param out a {@link PrintStream} object to print
     */
    public static void printAboutMe(PrintStream out) {
        out.println("Aspectran: " + VERSION);
        out.println("JVM: " + System.getProperty("java.version") +
                " (\"" + System.getProperty("java.vm.vendor") + "\"" + " " +
                System.getProperty("java.runtime.version") + ")");
        out.println("OS: " + System.getProperty("os.name") + " " +
                System.getProperty("os.version") + " " + System.getProperty("os.arch"));
    }

    /**
     * Prints Aspectran information to the specified print stream.
     * @param out a {@link PrintStream} object to print
     */
    public static void printPrettyAboutMe(PrintStream out) {
        out.println("---------------------------------------------------------------------");
        out.printf(" %1$-9s : %2$s%n", "Aspectran", VERSION);
        out.printf(" %1$-9s : %2$s%n", "JVM", System.getProperty("java.version") +
                " (\"" + System.getProperty("java.vm.vendor") + "\"" + " " +
                System.getProperty("java.runtime.version") + ")");
        out.printf(" %1$-9s : %2$s%n", "OS", System.getProperty("os.name") + " " +
                System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        out.println("---------------------------------------------------------------------");
    }

    /**
     * Prints Aspectran information to {@link System#out}.
     * @param args a string array containing the command line arguments
     */
    public static void main(String[] args) {
        printPrettyAboutMe(System.out);
    }

}
