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

import com.aspectran.shell.jline.console.JLineConsoleStyler.Style;
import org.jspecify.annotations.NonNull;

import java.io.PrintStream;

/**
 * A {@link PrintStream} that writes to a JLine terminal, supporting styled
 * text and the ability to print messages above the current command prompt.
 *
 * <p>This stream intercepts write operations and routes them through the
 * {@link JLineTerminal}'s styling and printing mechanisms. This ensures that
 * output is correctly formatted with ANSI styles and does not interfere with
 * the user's current input line, especially when using the "above" mode.
 */
public class TerminalPrintStream extends PrintStream {

    private final JLineTerminal jlineTerminal;

    private final boolean above;

    private final String[] styles;

    /**
     * Instantiates a new TerminalPrintStream.
     * @param jlineTerminal the JLine terminal to write to
     */
    public TerminalPrintStream(JLineTerminal jlineTerminal) {
        this(jlineTerminal, false, null);
    }

    /**
     * Instantiates a new TerminalPrintStream.
     * @param jlineTerminal the JLine terminal to write to
     * @param above if true, prints the output above the current prompt line
     */
    public TerminalPrintStream(JLineTerminal jlineTerminal, boolean above) {
        this(jlineTerminal, above, null);
    }

    /**
     * Instantiates a new TerminalPrintStream.
     * @param jlineTerminal the JLine terminal to write to
     * @param above if true, prints the output above the current prompt line
     * @param styles the styles to apply to the output
     */
    public TerminalPrintStream(@NonNull JLineTerminal jlineTerminal, boolean above, String[] styles) {
        super(jlineTerminal.getOutput());
        this.jlineTerminal = jlineTerminal;
        this.above = above;
        this.styles = styles;
    }

    @Override
    public void write(byte @NonNull [] buf, int off, int len) {
        try {
            String str = new String(buf, off, len, jlineTerminal.getEncoding());
            if (above) {
                str = str.stripTrailing();
                if (str.isBlank()) {
                    return;
                }
            }

            Style oldStyle = null;
            if (styles != null) {
                oldStyle = jlineTerminal.getStyle();
                jlineTerminal.applyStyle(styles);
            }
            if (above) {
                jlineTerminal.writeAbove(str);
            } else {
                jlineTerminal.write(str);
            }
            if (styles != null && oldStyle != null) {
                jlineTerminal.setStyle(oldStyle);
            }
        } catch (Exception e) {
            super.write(buf, off, len);
        }
    }

    @Override
    public void flush() {
        jlineTerminal.flush();
    }

}
