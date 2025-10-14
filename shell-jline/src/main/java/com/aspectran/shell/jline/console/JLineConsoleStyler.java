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

import com.aspectran.core.context.config.ShellStyleConfig;
import com.aspectran.shell.console.DefaultConsoleStyler;
import org.jline.utils.AttributedStyle;

/**
 * Manages text styling for the JLine-based shell console.
 *
 * <p>This class extends {@link DefaultConsoleStyler} to integrate with the
 * JLine terminal's styling capabilities. It translates abstract style names
 * (e.g., "success", "danger") into JLine's {@link AttributedStyle} objects.
 *
 * <p>For performance, it caches the created {@code Style} objects to avoid
 * redundant parsing and object creation when styles are frequently reused.
 * The cache is invalidated and rebuilt if the shell style configuration is
 * updated.
 *
 * <p>Created: 2025-08-11</p>
 */
public class JLineConsoleStyler extends DefaultConsoleStyler {

    private final JLineTerminal jlineTerminal;

    /** Cache for Style objects to avoid repeated creation. */
    private Style baseStyle;
    private Style secondaryStyle;
    private Style successStyle;
    private Style dangerStyle;
    private Style warningStyle;
    private Style infoStyle;

    public JLineConsoleStyler(JLineTerminal jlineTerminal) {
        this.jlineTerminal = jlineTerminal;
    }

    @Override
    public void setShellStyleConfig(ShellStyleConfig shellStyleConfig) {
        // Invalidate all cached styles. The superclass method will call resetStyle(),
        // which will then re-initialize the baseStyle from the new configuration.
        this.baseStyle = null;
        this.secondaryStyle = null;
        this.successStyle = null;
        this.dangerStyle = null;
        this.warningStyle = null;
        this.infoStyle = null;
        super.setShellStyleConfig(shellStyleConfig);
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
            if (this.secondaryStyle == null) {
                this.secondaryStyle = new Style(getSecondaryStyle());
            }
            setStyle(this.secondaryStyle);
        }
    }

    @Override
    public void successStyle() {
        if (getSuccessStyle() != null) {
            if (this.successStyle == null) {
                this.successStyle = new Style(getSuccessStyle());
            }
            setStyle(this.successStyle);
        }
    }

    @Override
    public void dangerStyle() {
        if (getDangerStyle() != null) {
            if (this.dangerStyle == null) {
                this.dangerStyle = new Style(getDangerStyle());
            }
            setStyle(this.dangerStyle);
        }
    }

    @Override
    public void warningStyle() {
        if (getWarningStyle() != null) {
            if (this.warningStyle == null) {
                this.warningStyle = new Style(getWarningStyle());
            }
            setStyle(this.warningStyle);
        }
    }

    @Override
    public void infoStyle() {
        if (getInfoStyle() != null) {
            if (this.infoStyle == null) {
                this.infoStyle = new Style(getInfoStyle());
            }
            setStyle(this.infoStyle);
        }
    }

    /**
     * Represents a JLine-specific text style, wrapping an {@link AttributedStyle}.
     */
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
