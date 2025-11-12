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
package com.aspectran.core.context.config;

import com.aspectran.utils.apon.DefaultParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

/**
 * Configuration for shell text style presets used by the console UI.
 * <p>
 * Each preset is a delimited string that can be split into an array of style
 * tokens (e.g., "bold,fg:green"). These tokens are interpreted by a styler
 * to apply platform-specific formatting.
 *
 * <p>Supported presets:
 * <ul>
 *   <li>primary: Default or prompt text</li>
 *   <li>secondary: Secondary text</li>
 *   <li>success: Success messages</li>
 *   <li>danger: Error or warning messages</li>
 *   <li>warning: Caution messages</li>
 *   <li>info: Informational messages</li>
 * </ul>
 */
public class ShellStyleConfig extends DefaultParameters {

    /** The style for primary text. */
    private static final ParameterKey primary;

    /** The style for secondary text. */
    private static final ParameterKey secondary;

    /** The style for success messages. */
    private static final ParameterKey success;

    /** The style for danger messages. */
    private static final ParameterKey danger;

    /** The style for warning messages. */
    private static final ParameterKey warning;

    /** The style for informational messages. */
    private static final ParameterKey info;

    private static final ParameterKey[] parameterKeys;

    static {
        primary = new ParameterKey("primary", ValueType.STRING, true);
        secondary = new ParameterKey("secondary", ValueType.STRING, true);
        success = new ParameterKey("success", ValueType.STRING, true);
        danger = new ParameterKey("danger", ValueType.STRING, true);
        warning = new ParameterKey("warning", ValueType.STRING, true);
        info = new ParameterKey("info", ValueType.STRING, true);

        parameterKeys = new ParameterKey[] {
                primary,
                secondary,
                success,
                danger,
                warning,
                info
        };
    }

    /**
     * Instantiates a new ShellStyleConfig.
     */
    public ShellStyleConfig() {
        super(parameterKeys);
    }

    /**
     * Returns the style for primary text.
     * @return the primary style tokens
     */
    public String[] getPrimaryStyle() {
        if (isAssigned(primary)) {
            return getStringArray(primary);
        } else {
            return null;
        }
    }

    /**
     * Sets the style for primary text.
     * @param primary the primary style tokens
     * @return this {@code ShellStyleConfig} instance
     */
    public ShellStyleConfig setPrimaryStyle(String primary) {
        putValue(ShellStyleConfig.primary, primary);
        return this;
    }

    /**
     * Returns the style for secondary text.
     * @return the secondary style tokens
     */
    public String[] getSecondaryStyle() {
        if (isAssigned(secondary)) {
            return getStringArray(secondary);
        } else {
            return null;
        }
    }

    /**
     * Sets the style for secondary text.
     * @param secondary the secondary style tokens
     * @return this {@code ShellStyleConfig} instance
     */
    public ShellStyleConfig setSecondaryStyle(String secondary) {
        putValue(ShellStyleConfig.secondary, secondary);
        return this;
    }

    /**
     * Returns the style for success messages.
     * @return the success style tokens
     */
    public String[] getSuccessStyle() {
        if (isAssigned(success)) {
            return getStringArray(success);
        } else {
            return null;
        }
    }

    /**
     * Sets the style for success messages.
     * @param success the success style tokens
     * @return this {@code ShellStyleConfig} instance
     */
    public ShellStyleConfig setSuccessStyle(String success) {
        putValue(ShellStyleConfig.success, success);
        return this;
    }

    /**
     * Returns the style for danger messages.
     * @return the danger style tokens
     */
    public String[] getDangerStyle() {
        if (isAssigned(danger)) {
            return getStringArray(danger);
        } else {
            return null;
        }
    }

    /**
     * Sets the style for danger messages.
     * @param danger the danger style tokens
     * @return this {@code ShellStyleConfig} instance
     */
    public ShellStyleConfig setDangerStyle(String danger) {
        putValue(ShellStyleConfig.danger, danger);
        return this;
    }

    /**
     * Returns the style for warning messages.
     * @return the warning style tokens
     */
    public String[] getWarningStyle() {
        if (isAssigned(warning)) {
            return getStringArray(warning);
        } else {
            return null;
        }
    }

    /**
     * Sets the style for warning messages.
     * @param warning the warning style tokens
     * @return this {@code ShellStyleConfig} instance
     */
    public ShellStyleConfig setWarningStyle(String warning) {
        putValue(ShellStyleConfig.warning, warning);
        return this;
    }

    /**
     * Returns the style for informational messages.
     * @return the info style tokens
     */
    public String[] getInfoStyle() {
        if (isAssigned(info)) {
            return getStringArray(info);
        } else {
            return null;
        }
    }

    /**
     * Sets the style for informational messages.
     * @param info the info style tokens
     * @return this {@code ShellStyleConfig} instance
     */
    public ShellStyleConfig setInfoStyle(String info) {
        putValue(ShellStyleConfig.info, info);
        return this;
    }

}
