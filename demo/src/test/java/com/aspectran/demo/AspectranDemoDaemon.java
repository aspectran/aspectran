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
package com.aspectran.demo;

import com.aspectran.core.context.env.EnvironmentProfiles;
import com.aspectran.daemon.DefaultDaemon;
import com.aspectran.utils.ResourceUtils;

import java.io.File;

import static com.aspectran.core.context.config.AspectranConfig.BASE_PATH_PROPERTY_NAME;

/**
 * Application server for Aspectran Demo.
 */
public class AspectranDemoDaemon {

    public static void main(String[] args) {
        try {
            System.setProperty(EnvironmentProfiles.ACTIVE_PROFILES_PROPERTY_NAME, "daemon");
            File current = ResourceUtils.getResourceAsFile(".");
            File root = new File(current, "../../app");
            System.setProperty(BASE_PATH_PROPERTY_NAME, root.getCanonicalPath()); // for logback
            String[] args2 = { root.getCanonicalPath(), "config/aspectran-config.apon" };
            DefaultDaemon.main(args2);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

}
