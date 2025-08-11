package com.aspectran.shell.console;

import com.aspectran.core.context.config.ShellStyleConfig;

/**
 * <p>Created: 2025-08-11</p>
 */
public interface ConsoleStyler {

    void setShellStyleConfig(ShellStyleConfig shellStyleConfig);

    boolean hasStyle();

    void setStyle(String... styles);

    void resetStyle();

    void resetStyle(String... styles);

    String[] getPrimaryStyle();

    String[] getSecondaryStyle();

    String[] getSuccessStyle();

    String[] getDangerStyle();

    String[] getWarningStyle();

    String[] getInfoStyle();

    void secondaryStyle();

    void successStyle();

    void dangerStyle();

    void warningStyle();

    void infoStyle();

}
