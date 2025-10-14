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
package com.aspectran.shell.console;

import com.aspectran.core.context.config.ShellStyleConfig;

/**
 * Default no-op implementation of {@link ConsoleStyler}.
 * <p>
 * This implementation stores style presets from a {@link ShellStyleConfig}
 * so they can be queried, but it does not apply any visual styling when its
 * state-changing methods are invoked. It serves as a fallback for environments
 * where styling is unsupported or as a base for more sophisticated stylers.
 * </p>
 *
 * <p>Created: 2017. 10. 25.</p>
 */
public class DefaultConsoleStyler implements ConsoleStyler {

    /** Preset for primary text (e.g., prompts). */
    private String[] primaryStyle;

    /** Preset for secondary text. */
    private String[] secondaryStyle;

    /** Preset for success messages. */
    private String[] successStyle;

    /** Preset for danger or error messages. */
    private String[] dangerStyle;

    /** Preset for warning messages. */
    private String[] warningStyle;

    /** Preset for informational messages. */
    private String[] infoStyle;

    @Override
    public void setShellStyleConfig(ShellStyleConfig shellStyleConfig) {
        if (shellStyleConfig == null) {
            throw new IllegalArgumentException("shellStyleConfig must not be null");
        }
        primaryStyle = shellStyleConfig.getPrimaryStyle();
        secondaryStyle = shellStyleConfig.getSecondaryStyle();
        successStyle = shellStyleConfig.getSuccessStyle();
        dangerStyle = shellStyleConfig.getDangerStyle();
        warningStyle = shellStyleConfig.getWarningStyle();
        infoStyle = shellStyleConfig.getInfoStyle();
        resetStyle();
    }

    @Override
    public String[] getPrimaryStyle() {
        return primaryStyle;
    }

    @Override
    public String[] getSecondaryStyle() {
        return secondaryStyle;
    }

    @Override
    public String[] getSuccessStyle() {
        return successStyle;
    }

    @Override
    public String[] getDangerStyle() {
        return dangerStyle;
    }

    @Override
    public String[] getWarningStyle() {
        return warningStyle;
    }

    @Override
    public String[] getInfoStyle() {
        return infoStyle;
    }

    /**
     * Always returns {@code false} as this implementation does not track active styles.
     * @return always {@code false}
     */
    @Override
    public boolean hasStyle() {
        return false;
    }

    /**
     * This is a no-op method as this class does not support styling.
     */
    @Override
    public void setStyle(String... styles) {
        // This is a no-op
    }

    /**
     * This is a no-op method as this class does not support styling.
     */
    @Override
    public void resetStyle() {
        // This is a no-op
    }

    /**
     * This is a no-op method as this class does not support styling.
     */
    @Override
    public void resetStyle(String... styles) {
        // This is a no-op
    }

    /**
     * This is a no-op method as this class does not support styling.
     */
    @Override
    public void secondaryStyle() {
        // This is a no-op
    }

    /**
     * This is a no-op method as this class does not support styling.
     */
    @Override
    public void successStyle() {
        // This is a no-op
    }

    /**
     * This is a no-op method as this class does not support styling.
     */
    @Override
    public void dangerStyle() {
        // This is a no-op
    }

    /**
     * This is a no-op method as this class does not support styling.
     */
    @Override
    public void warningStyle() {
        // This is a no-op
    }

    /**
     * This is a no-op method as this class does not support styling.
     */
    @Override
    public void infoStyle() {
        // This is a no-op
    }

}
