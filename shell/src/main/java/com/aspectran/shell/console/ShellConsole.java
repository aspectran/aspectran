/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
import com.aspectran.shell.command.CommandRunner;

import java.io.File;
import java.io.OutputStream;
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

    String getCommandPrompt();

    void setCommandPrompt(String commandPrompt);

    File getWorkingDir();

    void setWorkingDir(File workingDir);

    CommandRunner getCommandRunner();

    void setCommandRunner(CommandRunner runner);

    void setCommandHistoryFile(String historyFile);

    List<String> getCommandHistory();

    void clearCommandHistory();

    String readCommandLine();

    String readCommandLine(String prompt);

    String readLine();

    String readLine(String prompt);

    String readLine(String prompt, String buffer);

    String readPassword();

    String readPassword(String prompt);

    String readPassword(String prompt, String buffer);

    void write(String str);

    void write(String format, Object... args);

    void writeLine(String str);

    void writeLine(String format, Object... args);

    void writeLine();

    void writeError(String str);

    void writeError(String format, Object... args);

    void appendPrompt(String str);

    void clearPrompt();

    void clearScreen();

    void clearLine();

    void redrawLine();

    OutputStream getOutput();

    PrintWriter getWriter();

    boolean isBusy();

    boolean hasStyle();

    void setStyle(String... styles);

    void clearStyle();

    void setShellStyleConfig(ShellStyleConfig shellStyleConfig);

    String[] getPrimaryStyle();

    String[] getSecondaryStyle();

    String[] getSuccessStyle();

    String[] getDangerStyle();

    String[] getWarningStyle();

    String[] getInfoStyle();

    boolean confirmRestart();

    boolean confirmRestart(String message);

    boolean confirmQuit();

}
