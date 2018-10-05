/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.shell.command;

import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.shell.console.Console;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Command Line Parser.
 */
public class CommandLineParser {

    private static final String PARAM_NAME_PREFIX = "--";

    private static final Pattern REDIRECTION_OPERATOR_PATTERN = Pattern.compile("(>>)|(>)|(\")|(\')");

    private static final char ESCAPE = '\\';

    private final boolean parseArgs;

    private MethodType requestMethod;

    private String commandName;

    private String[] args;

    private boolean hasParameters;

    private List<CommandLineRedirection> redirectionList;

    private CommandLineParser(boolean parseArgs) {
        this.parseArgs = parseArgs;
    }

    public boolean isParseArgs() {
        return parseArgs;
    }

    /**
     * Gets the request method.
     *
     * @return the request method
     */
    public MethodType getRequestMethod() {
        return requestMethod;
    }

    /**
     * Gets the translet name.
     *
     * @return the translet name
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * Gets the command arguments.
     *
     * @return the command arguments
     */
    public String[] getArgs() {
        return args;
    }

    public boolean hasParameters() {
        return hasParameters;
    }

    public ParameterMap extractParameters() {
        if (!hasParameters) {
            return null;
        }
        ParameterMap params = new ParameterMap();
        String name = null;
        for (String arg : args) {
            if (arg.startsWith(PARAM_NAME_PREFIX)) {
                name = arg.substring(PARAM_NAME_PREFIX.length());
                params.setParameterValues(name, null);
            } else if (name != null) {
                String[] values = params.getParameterValues(name);
                if (values != null) {
                    values = Arrays.copyOf(values, values.length + 1);
                    values[values.length - 1] = arg;
                } else {
                    values = new String[] { arg };
                }
                params.setParameterValues(name, values);
                name = null;
            }
        }
        return params;
    }

    public List<CommandLineRedirection> getRedirectionList() {
        return redirectionList;
    }

    public Writer[] getRedirectionWriters(Console console) throws FileNotFoundException, UnsupportedEncodingException {
        if (redirectionList != null) {
            List<Writer> writerList = new ArrayList<>(redirectionList.size());
            for (CommandLineRedirection redirection : redirectionList) {
                File file = new File(redirection.getOperand());
                boolean append = (redirection.getOperator() == CommandLineRedirection.Operator.APPEND_OUT);
                OutputStream stream = new FileOutputStream(file, append);
                writerList.add(new OutputStreamWriter(stream, console.getEncoding()));
            }
            return writerList.toArray(new Writer[0]);
        } else {
            return null;
        }
    }

    /**
     * Parse the command line.
     *
     * @param commandLine the command line
     */
    private void parseCommandLine(String commandLine) {
        String[] tokens = splitCommandLine(commandLine);
        if (tokens.length > 1) {
            requestMethod = MethodType.resolve(tokens[0]);
            if (requestMethod != null) {
                commandName = commandLine.substring(tokens[0].length()).trim();
            }
        }
        if (requestMethod == null) {
            commandName = commandLine;
        }
        parseRedirection(commandName);
        if (parseArgs) {
            tokens = splitCommandLine(commandName);
            if (tokens.length > 1) {
                commandName = tokens[0];
                args = Arrays.copyOfRange(tokens, 1, tokens.length);
                for (String arg : args) {
                    if (arg.startsWith(PARAM_NAME_PREFIX)) {
                        hasParameters = true;
                        break;
                    }
                }
            }
        }
    }

    /**
     * Parse translet name and find all {@code CommandRedirection}s.
     *
     * @param buffer the translet name to parse
     */
    private void parseRedirection(String buffer) {
        Matcher matcher = REDIRECTION_OPERATOR_PATTERN.matcher(buffer);
        List<CommandLineRedirection> redirectionList = new ArrayList<>();
        CommandLineRedirection prevRedirectionOperation = null;
        boolean haveDoubleQuote = false;
        boolean haveSingleQuote = false;
        while (matcher.find()) {
            if (matcher.group(1) != null && !haveDoubleQuote && !haveSingleQuote) {
                String string = buffer.substring(0, matcher.start(1)).trim();
                if (prevRedirectionOperation != null) {
                    prevRedirectionOperation.setOperand(string);
                } else {
                    this.commandName = string;
                }
                prevRedirectionOperation = new CommandLineRedirection(CommandLineRedirection.Operator.APPEND_OUT);
                redirectionList.add(prevRedirectionOperation);
                buffer = buffer.substring(matcher.end(1));
                matcher = REDIRECTION_OPERATOR_PATTERN.matcher(buffer);
            }
            else if (matcher.group(2) != null && !haveDoubleQuote && !haveSingleQuote) {
                String string = buffer.substring(0, matcher.start(2)).trim();
                if (prevRedirectionOperation != null) {
                    prevRedirectionOperation.setOperand(string);
                } else {
                    this.commandName = string;
                }
                prevRedirectionOperation = new CommandLineRedirection(CommandLineRedirection.Operator.OVERWRITE_OUT);
                redirectionList.add(prevRedirectionOperation);
                buffer = buffer.substring(matcher.end(2));
                matcher = REDIRECTION_OPERATOR_PATTERN.matcher(buffer);
            }
            else if (matcher.group(3) != null) {
                if ((matcher.start(3) == 0 || buffer.charAt(matcher.start(3) - 1) != ESCAPE) &&
                        !haveSingleQuote) {
                    haveDoubleQuote = !haveDoubleQuote;
                }
            }
            else if (matcher.group(4) != null) {
                if ((matcher.start(4) == 0 || buffer.charAt(matcher.start(4) - 1) != ESCAPE) &&
                        !haveDoubleQuote) {
                    haveSingleQuote = !haveSingleQuote;
                }
            }
        }
        if (prevRedirectionOperation != null) {
            prevRedirectionOperation.setOperand(buffer.trim());
        }
        this.redirectionList = (redirectionList.size() > 0 ? redirectionList : null);
    }

    /**
     * Returns the command line parser.
     *
     * @param commandLine the command line
     * @return the command line parser
     */
    public static CommandLineParser parse(String commandLine) {
        CommandLineParser parser = new CommandLineParser(true);
        parser.parseCommandLine(commandLine);
        return parser;
    }

    public static CommandLineParser parse(String commandLine, boolean parseArgs) {
        CommandLineParser parser = new CommandLineParser(parseArgs);
        parser.parseCommandLine(commandLine);
        return parser;
    }

    public static String serialize(List<CommandLineRedirection> redirectionList) {
        StringBuilder sb = new StringBuilder();
        if (redirectionList != null) {
            for (CommandLineRedirection redirection : redirectionList) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(redirection.getOperator()).append(" ");
                sb.append(redirection.getOperand());
            }
        }
        return sb.toString();
    }

    public static String[] splitCommandLine(String commandLine) {
        return StringUtils.tokenize(commandLine, " ", true);
    }

}
