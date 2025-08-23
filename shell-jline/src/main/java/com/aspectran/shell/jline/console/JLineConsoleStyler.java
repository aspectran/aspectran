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

import com.aspectran.shell.console.DefaultConsoleStyler;
import org.jline.utils.AttributedStyle;

/**
 * <p>Created: 2025-08-11</p>
 */
public class JLineConsoleStyler extends DefaultConsoleStyler {

    private final JLineTerminal jlineTerminal;

    private Style baseStyle;

    public JLineConsoleStyler(JLineTerminal jlineTerminal) {
        this.jlineTerminal = jlineTerminal;
    }

    protected Style getBaseStyle() {
        return baseStyle;
    }

    protected Style getStyle() {
        return jlineTerminal.getStyle();
    }

    protected void setStyle(Style style) {
        jlineTerminal.setStyle(style);
    }

    @Override
    public boolean hasStyle() {
        return jlineTerminal.hasStyle();
    }

    @Override
    public void setStyle(String... styles) {
        jlineTerminal.applyStyle(styles);
    }

    @Override
    public void resetStyle() {
        if (baseStyle == null && getPrimaryStyle() != null) {
            baseStyle = new Style(getPrimaryStyle());
        }
        setStyle(baseStyle);
    }

    @Override
    public void resetStyle(String... styles) {
        resetStyle();
        setStyle(styles);
    }

    @Override
    public void secondaryStyle() {
        if (getSecondaryStyle() != null) {
            setStyle(new Style(getSecondaryStyle()));
        }
    }

    @Override
    public void successStyle() {
        if (getSuccessStyle() != null) {
            setStyle(new Style(getSuccessStyle()));
        }
    }

    @Override
    public void dangerStyle() {
        if (getDangerStyle() != null) {
            setStyle(new Style(getDangerStyle()));
        }
    }

    @Override
    public void warningStyle() {
        if (getWarningStyle() != null) {
            setStyle(new Style(getWarningStyle()));
        }
    }

    @Override
    public void infoStyle() {
        if (getInfoStyle() != null) {
            setStyle(new Style(getInfoStyle()));
        }
    }

    public static class Style {

        private final AttributedStyle attributedStyle;

        protected Style(String... styles) {
            this(null, styles);
        }

        protected Style(Style defaultStyle, String... styles) {
            if (defaultStyle != null) {
                this.attributedStyle = JLineTextStyler.style(defaultStyle.getAttributedStyle(), styles);
            } else {
                this.attributedStyle = JLineTextStyler.style(styles);
            }
        }

        public AttributedStyle getAttributedStyle() {
            return attributedStyle;
        }

    }

}
