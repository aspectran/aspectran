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
package com.aspectran.shell.jline;

import com.aspectran.utils.ResourceUtils;

import java.io.File;
import java.io.IOException;

/**
 * This is a test class for bootstrapping the JLineAspectranShell
 * with a specific Aspectran configuration file.
 * <p>
 * The class initializes the shell using the Aspectran configuration
 * defined in the specified file path. It demonstrates how to load
 * the configuration and start the shell.
 *
 * <p>Created: 2019. 1. 23.</p>
 */
class JLineAspectranShellOnlyTest {

    public static void main(String[] args) throws IOException {
        String configFilePath = "config/shell/jline/aspectran-config-jline-shell-only-test.apon";
        File aspectranConfigFile = ResourceUtils.getResourceAsFile(configFilePath);
        JLineAspectranShell.bootstrap(aspectranConfigFile);
    }

}
