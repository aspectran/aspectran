package com.aspectran.shell.console;

import com.aspectran.core.context.config.ShellStyleConfig;

/**
 * Default no-op implementation of ConsoleStyler.
 * <p>
 * This implementation stores style presets provided via {@link ShellStyleConfig}
 * so they can be queried, but it does not actually apply any visual styling
 * when its mutating methods are invoked. This is useful in environments where
 * styling is unsupported (e.g., basic consoles or logs without ANSI support),
 * or as a base class for more sophisticated stylers.
 * </p>
 *
 * <p>Created: 2025-08-11</p>
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
     * Always returns {@code false} as this implementation does not keep track
     * of active styles.
     */
    @Override
    public boolean hasStyle() {
        return false;
    }

    /** No-op. */
    @Override
    public void setStyle(String... styles) {
        // Nothing to do
    }

    /** No-op. */
    @Override
    public void resetStyle() {
        // Nothing to do
    }

    /** No-op. */
    @Override
    public void resetStyle(String... styles) {
        // Nothing to do
    }

    /** No-op. */
    @Override
    public void secondaryStyle() {
        // Nothing to do
    }

    /** No-op. */
    @Override
    public void successStyle() {
        // Nothing to do
    }

    /** No-op. */
    @Override
    public void dangerStyle() {
        // Nothing to do
    }

    /** No-op. */
    @Override
    public void warningStyle() {
        // Nothing to do
    }

    /** No-op. */
    @Override
    public void infoStyle() {
        // Nothing to do
    }

}
