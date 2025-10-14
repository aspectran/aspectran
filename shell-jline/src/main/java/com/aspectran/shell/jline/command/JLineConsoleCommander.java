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
package com.aspectran.shell.jline.command;

import com.aspectran.shell.command.DefaultConsoleCommander;
import com.aspectran.shell.jline.console.JLineShellConsole;
import com.aspectran.shell.jline.console.TerminalPrintStream;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * A {@link DefaultConsoleCommander} implementation that is specifically
 * tailored for the JLine environment.
 *
 * <p>This commander extends the default functionality by redirecting the
 * standard output ({@code System.out}) and standard error ({@code System.err})
 * streams to the JLine terminal. This ensures that all console output,
 * including stack traces and other system-level messages, is properly
 * displayed above the command prompt without interfering with the user's
 * current input line.
 *
 * <p>Created: 2017. 6. 3.</p>
 */
public class JLineConsoleCommander extends DefaultConsoleCommander {

    /**
     * Instantiates a new JLineConsoleCommander.
     * @param console the JLine-based shell console
     */
    public JLineConsoleCommander(@NonNull JLineShellConsole console) {
        super(console);
    }

    @Override
    protected void consoleReady() {
        super.consoleReady();

        JLineShellConsole console = getConsole();
        System.setOut(new TerminalPrintStream(console.getJlineTerminal(), true));
        System.setErr(new TerminalPrintStream(console.getJlineTerminal(), true, console.getStyler().getDangerStyle()));
    }

}
