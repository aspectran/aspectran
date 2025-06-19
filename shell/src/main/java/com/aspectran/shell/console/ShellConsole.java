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
import com.aspectran.shell.command.ConsoleCommander;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * The Interface for Console I/O.
 *
 * <p>Created: 2017. 3. 5.</p>
 *
 * @since 4.0.0
 */
public interface ShellConsole {

    String DEFAULT_PROMPT = "Aspectran> ";

    String MULTILINE_PROMPT = "> ";

    String COMMENT_PROMPT = "// ";

    String MULTILINE_DELIMITER = "\\";

    String COMMENT_DELIMITER = "//";

    char MASK_CHAR = '*';

    String getEncoding();

    PrintStream getOutput();

    PrintWriter getWriter();

    File getWorkingDir();

    void setWorkingDir(File workingDir);

    ConsoleCommander getConsoleCommander();

    void setConsoleCommander(ConsoleCommander runner);

    void setCommandHistoryFile(String historyFile);

    List<String> getCommandHistory();

    void clearCommandHistory();

    String getCommandPrompt();

    void setCommandPrompt(String commandPrompt);

    PromptStringBuilder newPromptStringBuilder();

    String readCommandLine();

    String readLine();

    String readLine(PromptStringBuilder promptStringBuilder);

    String readPassword();

    String readPassword(PromptStringBuilder promptStringBuilder);

    void write(String str);

    void write(String format, Object... args);

    void writeLine(String str);

    void writeLine(String format, Object... args);

    void writeLine();

    void writeError(String str);

    void writeError(String format, Object... args);

    void writeAbove(String str);

    void clearScreen();

    void clearLine();

    void redrawLine();

    boolean isReading();

    boolean hasStyle();

    void setStyle(String... styles);

    void resetStyle();

    void resetStyle(String... styles);

    void setShellStyleConfig(ShellStyleConfig shellStyleConfig);

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

    boolean confirmRestart();

    boolean confirmQuit();

}
