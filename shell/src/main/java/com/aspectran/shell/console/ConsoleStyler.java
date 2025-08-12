package com.aspectran.shell.console;

import com.aspectran.core.context.config.ShellStyleConfig;

/**
 * Defines an abstraction for applying and managing console text styles.
 * Implementations may translate style names (e.g., "bold", "fg:green") into
 * platform-specific markup or ANSI escape sequences. This interface is kept
 * lightweight so UIs without styling capabilities can still implement a
 * no-op variant.
 *
 * <p>Created: 2025-08-11</p>
 */
public interface ConsoleStyler {

    /**
     * Applies the provided ShellStyleConfig.
     * @param shellStyleConfig the style configuration; never {@code null}
     * @throws IllegalArgumentException if {@code shellStyleConfig} is {@code null}
     */
    void setShellStyleConfig(ShellStyleConfig shellStyleConfig);

    /**
     * Returns whether this styler currently has any active style applied.
     * @return {@code true} if any style is active; {@code false} otherwise
     */
    boolean hasStyle();

    /**
     * Activates one or more styles until a subsequent reset method is called.
     * Duplicate or unknown style names should be safely ignored by
     * implementations.
     * @param styles one or more style names; may be empty
     */
    void setStyle(String... styles);

    /**
     * Resets all styles to the default (no styling).
     */
    void resetStyle();

    /**
     * Resets previously applied styles and then activates the given styles in
     * one operation.
     * @param styles the styles to activate after reset
     */
    void resetStyle(String... styles);

    /**
     * Returns the style tokens used for primary text (e.g., prompts).
     * @return style tokens for primary text, or {@code null} if not configured
     */
    String[] getPrimaryStyle();

    /**
     * Returns the style tokens used for secondary text.
     * @return style tokens for secondary text, or {@code null} if not configured
     */
    String[] getSecondaryStyle();

    /**
     * Returns the style tokens used for success messages.
     * @return style tokens for success messages, or {@code null} if not configured
     */
    String[] getSuccessStyle();

    /**
     * Returns the style tokens used for danger or error messages.
     *
     * @return style tokens for danger messages, or {@code null} if not configured
     */
    String[] getDangerStyle();

    /**
     * Returns the style tokens used for warning messages.
     * @return style tokens for warnings, or {@code null} if not configured
     */
    String[] getWarningStyle();

    /**
     * Returns the style tokens used for informational messages.
     * @return style tokens for info messages, or {@code null} if not configured
     */
    String[] getInfoStyle();

    /**
     * Convenience to reset and then apply the {@linkplain #getSecondaryStyle() secondary} style.
     */
    void secondaryStyle();

    /**
     * Convenience to reset and then apply {@linkplain #getSuccessStyle() success} style.
     */
    void successStyle();

    /**
     * Convenience to reset and then apply {@linkplain #getDangerStyle() danger} style.
     */
    void dangerStyle();

    /**
     * Convenience to reset and then apply {@linkplain #getWarningStyle() warning} style.
     */
    void warningStyle();

    /**
     * Convenience to reset and then apply {@linkplain #getInfoStyle() info} style.
     */
    void infoStyle();

}
