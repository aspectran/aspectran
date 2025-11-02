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
 * A test class to bootstrap the JLineAspectranShell with a specified configuration file.
 *
 * <p>This class is primarily designed for testing purposes to evaluate the functionality
 * of the JLineAspectranShell. It initializes the shell using a configuration file
 * and executes the main method.</p>
 *
 * <p>Usage involves referencing a configuration file located at the specified
 * path and utilizing {@code ResourceUtils} to retrieve it as a {@code File} object.
 * The retrieved configuration is then passed into the bootstrap method of the
 * {@code JLineAspectranShell} to initiate the shell.</p>
 *
 * <p>The configuration file should adhere to the expected format and
 * structure required by Aspectran.</p>
 *
 * <p>Exceptions:</p>
 * <ul>
 *   <li>{@link IOException}: Thrown if there is an issue accessing or reading the specified configuration file.</li>
 * </ul>
 *
 * <p>Created: 2017. 3. 26.</p>
 */
class JLineAspectranShellTest {

    public static void main(String[] args) throws IOException {
        String configFilePath = "config/shell/jline/aspectran-config-jline-shell-test.apon";
        File aspectranConfigFile = ResourceUtils.getResourceAsFile(configFilePath);
        JLineAspectranShell.bootstrap(aspectranConfigFile);
    }

}
