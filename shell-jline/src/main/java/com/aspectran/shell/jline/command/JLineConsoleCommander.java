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
package com.aspectran.shell.jline.command;

import com.aspectran.core.lang.NonNull;
import com.aspectran.core.lang.Nullable;
import com.aspectran.shell.command.DefaultConsoleCommander;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.shell.jline.console.ShellConsoleErrorStream;
import com.aspectran.shell.jline.console.ShellConsoleOutStream;

import java.io.File;
import java.io.PrintStream;

/**
 * The Shell Command Runner.
 *
 * <p>Created: 2017. 6. 3.</p>
 */
public class JLineConsoleCommander extends DefaultConsoleCommander {

    private PrintStream orgSystemOut;

    private PrintStream orgSystemErr;

    public JLineConsoleCommander(@NonNull ShellConsole console) {
        super(console);
    }

    public void prepare(@Nullable String basePath, File aspectranConfigFile) throws Exception {
        super.prepare(basePath, aspectranConfigFile);

        orgSystemOut = System.out;
        orgSystemErr = System.err;
        System.setOut(new ShellConsoleOutStream(getConsole()));
        System.setErr(new ShellConsoleErrorStream(getConsole()));
    }

    public void release() {
        if (orgSystemOut != null) {
            System.setOut(orgSystemOut);
            orgSystemOut = null;
        }
        if (orgSystemErr != null) {
            System.setErr(orgSystemErr);
            orgSystemErr = null;
        }
        super.release();
    }

}
