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
package com.aspectran.core.util;

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
            VERSION = System.getProperty("aspectran.version", "5.2.3-SNAPSHOT");
        }

        POWERED_BY = "Powered by Aspectran " + VERSION;
        POWERED_BY_LINK = "<a href=\"http://www.aspectran.com\">Powered by Aspectran " + VERSION + "</a>";

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

    /**
     * Prints Aspectran information to {@link System#out}.
     *
     * @param args a string array containing the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Aspectran version: " + VERSION);
        System.out.println("OS Name:           " + System.getProperty("os.name"));
        System.out.println("OS Version:        " + System.getProperty("os.version"));
        System.out.println("Architecture:      " + System.getProperty("os.arch"));
        System.out.println("JVM Version:       " + System.getProperty("java.runtime.version"));
        System.out.println("JVM Vendor:        " + System.getProperty("java.vm.vendor"));
    }

}
