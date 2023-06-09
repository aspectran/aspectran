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

import com.aspectran.shell.console.PromptStringBuilder;

public class JLinePromptStringBuilder extends PromptStringBuilder {

    private final JLineTerminal jlineTerminal;

    private JLineTerminal.Style style;

    public JLinePromptStringBuilder(JLineTerminal jlineTerminal) {
        this.jlineTerminal = jlineTerminal;
    }

    public void setStyle(String... styles) {
        this.style = new JLineTerminal.Style(style, styles);
    }

    public void resetStyle(String... styles) {
        this.style = new JLineTerminal.Style(styles);
    }

    public void append(String str) {
        super.append(jlineTerminal.toAnsi(str, style));
    }

}
