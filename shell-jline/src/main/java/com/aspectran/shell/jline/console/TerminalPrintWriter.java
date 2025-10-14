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
package com.aspectran.shell.jline.console;

import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.PrintWriter;

/**
 * A {@link PrintWriter} that delegates write operations to a {@link JLineTerminal}.
 *
 * <p>This class ensures that all text written through it is processed by the
 * terminal's styling mechanism, allowing for consistent, styled output. It
 * intercepts standard {@code write} methods and routes them to
 * {@link JLineTerminal#write(String)}, which handles the conversion to
 * ANSI-escaped strings before printing.
 */
public class TerminalPrintWriter extends PrintWriter {

    private final JLineTerminal jlineTerminal;

    /**
     * Instantiates a new TerminalPrintWriter.
     * @param jlineTerminal the JLine terminal to write to
     */
    public TerminalPrintWriter(@NonNull JLineTerminal jlineTerminal) {
        super(jlineTerminal.getWriter());
        this.jlineTerminal = jlineTerminal;
    }

    @Override
    public void write(int c) {
        String str = Character.toString(c);
        write(str, 0, str.length());
    }

    @Override
    public void write(@NonNull char[] buf, int off, int len) {
        try {
            String str = new String(buf, off, len);
            jlineTerminal.write(str);
        } catch (Exception e) {
            super.write(buf, off, len);
        }
    }

    @Override
    public void write(@NonNull String str, int off, int len) {
        try {
            if (str.length() != len) {
                str = str.substring(off, off + len);
            }
            jlineTerminal.write(str);
        } catch (Exception e) {
            super.write(str, off, len);
        }
    }

}
