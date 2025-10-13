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
package com.aspectran.demo;

import com.aspectran.shell.jline.JLineAspectranShell;
import com.aspectran.utils.ResourceUtils;

import java.io.File;
import java.io.IOException;

import static com.aspectran.core.context.config.AspectranConfig.BASE_PATH_PROPERTY_NAME;

/**
 * Provides the main entry point for running the Aspectran Demo application
 * in a rich, JLine-based shell environment.
 * <p>
 * This class configures the base path and launches the {@link JLineAspectranShell},
 * which provides advanced features like command history and auto-completion.
 * </p>
 */
public class DemoRichShell {

    /**
     * The main method that starts the Aspectran Demo JLine shell.
     * @param args application startup arguments
     */
    public static void main(String[] args) {
        try {
            File root = new File(ResourceUtils.getResourceAsFile(""), "../../app");
            System.setProperty(BASE_PATH_PROPERTY_NAME, root.getCanonicalPath()); // for logback
            JLineAspectranShell.main(new String[] { root.getCanonicalPath(), "config/aspectran-config.apon" });
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

}
