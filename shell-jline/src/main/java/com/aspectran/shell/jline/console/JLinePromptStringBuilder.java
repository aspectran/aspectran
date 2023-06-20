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

import com.aspectran.shell.console.DefaultPromptStringBuilder;
import com.aspectran.shell.console.PromptStringBuilder;

class JLinePromptStringBuilder extends DefaultPromptStringBuilder {

    private final JLineTerminal jlineTerminal;

    private final JLineTerminal.Style primaryStyle;

    private JLineTerminal.Style style;

    public JLinePromptStringBuilder(JLineTerminal jlineTerminal, String... styles) {
        super();
        this.jlineTerminal = jlineTerminal;
        this.primaryStyle = new JLineTerminal.Style(styles);
    }

    @Override
    public PromptStringBuilder setStyle(String... styles) {
        style = new JLineTerminal.Style(style, styles);
        return this;
    }

    @Override
    public PromptStringBuilder resetStyle(String... styles) {
        style = new JLineTerminal.Style(primaryStyle, styles);
        return this;
    }

    @Override
    public PromptStringBuilder resetStyle() {
        style = primaryStyle;
        return this;
    }

    @Override
    public PromptStringBuilder append(String str) {
        super.append(jlineTerminal.toAnsi(str, style));
        return this;
    }

}
