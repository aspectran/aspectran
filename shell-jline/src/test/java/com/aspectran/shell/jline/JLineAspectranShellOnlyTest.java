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
package com.aspectran.shell.jline;

import com.aspectran.core.util.ResourceUtils;
import com.aspectran.shell.AspectranShell;
import com.aspectran.shell.jline.console.JLineConsole;

import java.io.File;
import java.io.IOException;

/**
 * <p>Created: 2019. 1. 23.</p>
 */
class JLineAspectranShellOnlyTest {

    public static void main(String[] args) throws IOException {
        File aspectranConfigFile = ResourceUtils.getResourceAsFile("config/shell/jline/aspectran-config-jline-shell-only-test.apon");
        AspectranShell.bootstrap(aspectranConfigFile, new JLineConsole());
    }

}
