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
package com.aspectran.shell.console;

import com.aspectran.core.lang.NonNull;

import java.io.IOException;
import java.io.PrintStream;
public class ShellConsolePrintStream extends PrintStream {

    private final ShellConsole console;

    public ShellConsolePrintStream(PrintStream parent, ShellConsole console) {
        super(parent);
        this.console = console;
    }

    @Override
    public void write(int b) {
        write(new byte[] { (byte)b }, 0, 1);
    }

    @Override
    public void write(@NonNull byte[] buf, int off, int len) {
        try {
            String str = new String(buf, off, len, console.getEncoding());
            console.writeAbove(str);
        } catch (IOException e) {
            // ignore
        }
    }

}
