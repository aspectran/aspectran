/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

public class ShellStyleConfig extends AbstractParameters {

    private static final ParameterKey primary;
    private static final ParameterKey secondary;
    private static final ParameterKey success;
    private static final ParameterKey danger;
    private static final ParameterKey warning;
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

    public ShellStyleConfig() {
        super(parameterKeys);
    }

    public String[] getPrimaryStyle() {
        return getStringArray(primary);
    }

    public ShellStyleConfig setPrimaryStyle(String primary) {
        putValue(ShellStyleConfig.primary, primary);
        return this;
    }

    public String[] getSecondaryStyle() {
        return getStringArray(secondary);
    }

    public ShellStyleConfig setSecondaryStyle(String secondary) {
        putValue(ShellStyleConfig.secondary, secondary);
        return this;
    }

    public String[] getSuccessStyle() {
        return getStringArray(success);
    }

    public ShellStyleConfig setSuccessStyle(String success) {
        putValue(ShellStyleConfig.success, success);
        return this;
    }

    public String[] getDangerStyle() {
        return getStringArray(danger);
    }

    public ShellStyleConfig setDangerStyle(String danger) {
        putValue(ShellStyleConfig.danger, danger);
        return this;
    }

    public String[] getWarningStyle() {
        return getStringArray(warning);
    }

    public ShellStyleConfig setWarningStyle(String warning) {
        putValue(ShellStyleConfig.warning, warning);
        return this;
    }

    public String[] getInfoStyle() {
        return getStringArray(info);
    }

    public ShellStyleConfig setInfoStyle(String info) {
        putValue(ShellStyleConfig.info, info);
        return this;
    }

}
