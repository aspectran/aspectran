package com.aspectran.shell.console;

import com.aspectran.core.context.config.ShellStyleConfig;

/**
 * <p>Created: 2025-08-11</p>
 */
public class DefaultConsoleStyler implements ConsoleStyler {

    private String[] primaryStyle;

    private String[] secondaryStyle;

    private String[] successStyle;

    private String[] dangerStyle;

    private String[] warningStyle;

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

    @Override
    public boolean hasStyle() {
        return false;
    }

    @Override
    public void setStyle(String... styles) {
        // Nothing to do
    }

    @Override
    public void resetStyle() {
        // Nothing to do
    }

    @Override
    public void resetStyle(String... styles) {
        // Nothing to do
    }

    @Override
    public void secondaryStyle() {
        // Nothing to do
    }

    @Override
    public void successStyle() {
        // Nothing to do
    }

    @Override
    public void dangerStyle() {
        // Nothing to do
    }

    @Override
    public void warningStyle() {
        // Nothing to do
    }

    @Override
    public void infoStyle() {
        // Nothing to do
    }

}
