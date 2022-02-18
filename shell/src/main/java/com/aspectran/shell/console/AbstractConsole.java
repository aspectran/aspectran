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

import com.aspectran.shell.command.CommandInterpreter;

import java.io.File;
import java.nio.charset.Charset;

/**
 * The Abstract Class for Console I/O.
 *
 * <p>Created: 2017. 3. 4.</p>
 */
public abstract class AbstractConsole implements Console {

    private final String encoding;

    private String commandPrompt = DEFAULT_PROMPT;

    private final StringBuilder promptBuilder = new StringBuilder();

    private File workingDir;

    private CommandInterpreter interpreter;

    public AbstractConsole(String encoding) {
        if (encoding != null) {
            this.encoding = encoding;
        } else {
            this.encoding = Charset.defaultCharset().name();
        }
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public String getCommandPrompt() {
        return commandPrompt;
    }

    @Override
    public void setCommandPrompt(String commandPrompt) {
        this.commandPrompt = commandPrompt;
    }

    protected String getPrompt() {
        return (promptBuilder.length() > 0 ? promptBuilder.toString() : null);
    }

    @Override
    public void appendPrompt(String string) {
        promptBuilder.append(string);
    }

    @Override
    public void clearPrompt() {
        promptBuilder.setLength(0);
    }

    @Override
    public File getWorkingDir() {
        return workingDir;
    }

    @Override
    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }

    @Override
    public CommandInterpreter getInterpreter() {
        return interpreter;
    }

    @Override
    public void setInterpreter(CommandInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    protected String readMultiCommandLine(String line) {
        if (COMMENT_DELIMITER.equals(line)) {
            String next = readRawCommandLine(COMMENT_PROMPT);
            if (next.isEmpty()) {
                return next;
            }
            readMultiCommandLine(COMMENT_DELIMITER);
        }
        String quote = searchQuote(line);
        if (quote != null || line.endsWith(MULTILINE_DELIMITER)) {
            String next = readRawCommandLine(MULTILINE_PROMPT).trim();
            if (next.startsWith(COMMENT_DELIMITER)) {
                line = readMultiCommandLine(line);
            } else if (quote != null) {
                line += System.lineSeparator() + next;
            } else {
                line = line.substring(0, line.length() - MULTILINE_DELIMITER.length()).trim() + " " + next;
            }
        }
        quote = searchQuote(line);
        if (quote != null) {
            return readMultiCommandLine(line);
        }
        if (line.endsWith(MULTILINE_DELIMITER)) {
            line = readMultiCommandLine(line);
        }
        return line;
    }

    protected String readMultiLine(String line) {
        if (line == null) {
            line = readRawLine(MULTILINE_PROMPT);
        }
        if (line.endsWith(MULTILINE_DELIMITER)) {
            line = line.substring(0, line.length() - MULTILINE_DELIMITER.length()) +
                    System.lineSeparator() + readMultiLine(null);
        }
        return line;
    }

    abstract protected String readRawCommandLine(String prompt);

    abstract protected String readRawLine(String prompt);

    private String searchQuote(String line) {
        boolean doubleQuote = false;
        boolean singleQuote = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"' && !singleQuote) {
                doubleQuote = !doubleQuote;
            } else if (c == '\'' && !doubleQuote) {
                singleQuote = !singleQuote;
            }
        }
        if (doubleQuote) {
            return "\"";
        } else if (singleQuote) {
            return "'";
        } else {
            return null;
        }
    }

}
