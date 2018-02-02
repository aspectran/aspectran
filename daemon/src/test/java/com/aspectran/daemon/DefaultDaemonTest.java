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
package com.aspectran.daemon;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.util.ResourceUtils;

import java.io.File;

/**
 * <p>Created: 2017. 12. 12.</p>
 */
public class DefaultDaemonTest {

    public static void main(String[] args) {
        try {
            File current = ResourceUtils.getResourceAsFile("com/aspectran/daemon");
            File root = new File(current, "../../../../../../demo/app");
            File aspectranConfigFile = new File(root, "config/aspectran-config.apon");
            System.setProperty(ActivityContext.BASE_DIR_PROPERTY_NAME, root.getCanonicalPath());

            String[] args2 = { aspectranConfigFile.getCanonicalPath() };
            DefaultDaemon.main(args2);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}