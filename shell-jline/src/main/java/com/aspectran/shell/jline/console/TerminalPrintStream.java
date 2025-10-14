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
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.PrintStream;

public class TerminalPrintStream extends PrintStream {

    private final JLineTerminal jlineTerminal;

    private final boolean above;

    private final String[] styles;

    public TerminalPrintStream(JLineTerminal jlineTerminal) {
        this(jlineTerminal, false, null);
    }

    public TerminalPrintStream(JLineTerminal jlineTerminal, boolean above) {
        this(jlineTerminal, above, null);
    }

    public TerminalPrintStream(@NonNull JLineTerminal jlineTerminal, boolean above, String[] styles) {
        super(jlineTerminal.getOutput());
        this.jlineTerminal = jlineTerminal;
        this.above = above;
        this.styles = styles;
    }

    @Override
    public void write(@NonNull byte[] buf, int off, int len) {
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
