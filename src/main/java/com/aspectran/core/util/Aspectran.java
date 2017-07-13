/*
 * Copyright 2008-2017 Juho Jeong
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

    static {
        Package pkg = Aspectran.class.getPackage();
        if (pkg != null && "Aspectran.com".equals(pkg.getImplementationVendor()) &&
                pkg.getImplementationVersion() != null) {
            VERSION = pkg.getImplementationVersion();
        } else {
            String version = System.getProperty("aspectran.version");
            if (version != null) {
                VERSION = version;
            } else {
                VERSION = "4.2.0-SNAPSHOT";
            }
        }

        POWERED_BY = "<a href=\"http://www.aspectran.com\">Powered by Aspectran " + VERSION + "</a>";
    }

    private Aspectran() {
    }

}
