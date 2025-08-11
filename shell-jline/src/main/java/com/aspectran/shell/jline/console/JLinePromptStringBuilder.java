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

import com.aspectran.shell.console.DefaultPromptStringBuilder;
import com.aspectran.shell.console.PromptStringBuilder;
import com.aspectran.shell.jline.console.JLineConsoleStyler.Style;
import com.aspectran.utils.annotation.jsr305.NonNull;

class JLinePromptStringBuilder extends DefaultPromptStringBuilder {

    private final JLineShellConsole console;

    private final JLineTerminal jlineTerminal;

    private final Style baseStyle;

    private Style style;

    public JLinePromptStringBuilder(@NonNull JLineShellConsole console) {
        super();
        this.console = console;
        this.jlineTerminal = console.getJlineTerminal();
        this.baseStyle = console.getStyler().getBaseStyle();
        this.style = console.getStyler().getBaseStyle();
    }

    @Override
    public PromptStringBuilder append(String str) {
        super.append(jlineTerminal.toAnsi(str, style));
        return this;
    }

    @Override
    public PromptStringBuilder setStyle(String... styles) {
        return setStyle(new Style(style, styles));
    }

    private PromptStringBuilder setStyle(Style style) {
        this.style = style;
        return this;
    }

    @Override
    public PromptStringBuilder resetStyle(String... styles) {
        resetStyle();
        return setStyle(styles);
    }

    @Override
    public PromptStringBuilder resetStyle() {
        return setStyle(baseStyle);
    }

    @Override
    public PromptStringBuilder secondaryStyle() {
        if (console.getStyler().getSecondaryStyle() != null) {
            return setStyle(new Style(console.getStyler().getSecondaryStyle()));
        } else {
            return resetStyle();
        }
    }

    @Override
    public PromptStringBuilder successStyle() {
        if (console.getStyler().getSuccessStyle() != null) {
            return setStyle(new Style(console.getStyler().getSuccessStyle()));
        } else {
            return resetStyle();
        }
    }

    @Override
    public PromptStringBuilder dangerStyle() {
        if (console.getStyler().getDangerStyle() != null) {
            return setStyle(new Style(console.getStyler().getDangerStyle()));
        } else {
            return resetStyle();
        }
    }

    @Override
    public PromptStringBuilder warningStyle() {
        if (console.getStyler().getWarningStyle() != null) {
            return setStyle(new Style(console.getStyler().getWarningStyle()));
        } else {
            return resetStyle();
        }
    }

    @Override
    public PromptStringBuilder infoStyle() {
        if (console.getStyler().getInfoStyle() != null) {
            return setStyle(new Style(console.getStyler().getInfoStyle()));
        } else {
            return resetStyle();
        }
    }

}
