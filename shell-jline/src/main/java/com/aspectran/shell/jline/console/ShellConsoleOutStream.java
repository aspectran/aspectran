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
package com.aspectran.shell.jline.console;

import com.aspectran.core.lang.NonNull;
import com.aspectran.shell.console.ShellConsole;

import java.io.IOException;
import java.io.PrintStream;

public class ShellConsoleOutStream extends PrintStream {

    private final ShellConsole console;

    private final String[] styles;

    public ShellConsoleOutStream(ShellConsole console) {
        this(console, null);
    }

    public ShellConsoleOutStream(ShellConsole console, String[] styles) {
        super(console.getOutput());
        this.console = console;
        this.styles = styles;
    }

    @Override
    public void write(@NonNull byte[] buf, int off, int len) {
        try {
            String str = new String(buf, off, len, console.getEncoding()).stripTrailing();
            if (!str.isBlank()) {
                if (styles != null) {
                    console.setStyle(styles);
                }
                console.writeAbove(str.stripTrailing());
                if (styles != null) {
                    console.resetStyle();
                }
            }
        } catch (IOException e) {
            super.write(buf, off, len);
        }
    }

}
