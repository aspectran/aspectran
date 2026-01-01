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
package com.aspectran.shell.command;

import com.aspectran.shell.command.option.DefaultOptionParser;
import com.aspectran.shell.command.option.OptionParser;
import com.aspectran.shell.command.option.OptionParserException;
import com.aspectran.shell.command.option.Options;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.utils.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a raw command-line string into a command name, arguments, and output redirections.
 * <p>This class is responsible for the initial breakdown of user input. It identifies
 * output redirection operators (e.g., {@code >}, {@code >>}) and separates them from
 * the main command and its arguments. It also handles arguments enclosed in single or
 * double quotes to allow for spaces within a single argument.</p>
 *
 * <p>Created: 2017. 10. 28.</p>
 */
public class CommandLineParser {

    private static final Pattern ARGS_PATTERN =
            Pattern.compile("\\s+\"([^\"]*)\"|\"([^\"]*)\"|\\s+'([^']*)'|'([^']*)'|([^\\s\"']+)");

    private static final Pattern REDIRECTION_PATTERN = Pattern.compile("(>>)|(>)|(\")|(')");

    private static final Logger logger = LoggerFactory.getLogger(CommandLineParser.class);

    private final String commandLine;

    private String commandName;

    private String[] args;

    private List<OutputRedirection> redirectionList;

    /**
     * Instantiates a new command line parser.
     * @param commandLine the raw command line string to parse
     */
    public CommandLineParser(String commandLine) {
        this.commandLine = parseOutputRedirection(commandLine);
        if (StringUtils.hasLength(this.commandLine)) {
            this.args = splitCommandLine(this.commandLine);
        }
        shift();
    }

    /**
     * Separates the command name from the arguments.
     * The first token in the argument list is treated as the command name,
     * and the rest remain as arguments.
     */
    public void shift() {
        if (args != null && args.length > 0) {
            if (!StringUtils.hasLength(args[0])) {
                shift();
            } else {
                commandName = args[0];
                if (args.length > 1) {
                    args = Arrays.copyOfRange(args, 1, args.length);
                } else {
                    args = new String[0];
                }
            }
        }
    }

    /**
     * Returns the original command line, stripped of any output redirection.
     * @return the command line string
     */
    public String getCommandLine() {
        return commandLine;
    }

    /**
     * Returns the parsed command name.
     * @return the command name
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * Sets the command name.
     * @param commandName the new command name
     */
    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    /**
     * Returns the arguments of the command.
     * @return an array of arguments
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * Checks if there are any arguments.
     * @return {@code true} if arguments exist, {@code false} otherwise
     */
    public boolean hasArgs() {
        return (args != null && args.length > 0);
    }

    /**
     * Parses the command-line arguments against the specified options.
     * @param options the options to parse against
     * @return the parsed options
     * @throws OptionParserException if an error occurs during parsing
     */
    public ParsedOptions parseOptions(Options options) throws OptionParserException {
        OptionParser parser = new DefaultOptionParser();
        return parser.parse(options, args, options.isSkipParsingAtNonOption());
    }

    /**
     * Returns a list of the output redirections extracted from the command line.
     * @return a list of output redirections, or {@code null} if none were found
     */
    public List<OutputRedirection> getRedirectionList() {
        return redirectionList;
    }

    /**
     * Splits the command line into an array of strings.
     * This method correctly handles arguments enclosed in quotes.
     * @param commandLine the command line to split
     * @return an array of strings representing the command and its arguments
     */
    private String @NonNull [] splitCommandLine(String commandLine) {
        List<String> list = new ArrayList<>();
        Matcher m = ARGS_PATTERN.matcher(commandLine);
        while (m.find()) {
            if (m.group(1) != null) {
                list.add(m.group(1));
            } else if (m.group(2) != null) {
                if (!list.isEmpty()) {
                    int index = list.size() - 1;
                    list.set(index, list.get(index) + m.group(2));
                } else {
                    list.add(m.group(2));
                }
            } else if (m.group(3) != null) {
                list.add(m.group(3));
            } else if (m.group(4) != null) {
                if (!list.isEmpty()) {
                    int index = list.size() - 1;
                    list.set(index, list.get(index) + m.group(4));
                } else {
                    list.add(m.group(4));
                }
            } else {
                list.add(m.group(5));
            }
        }
        return list.toArray(new String[0]);
    }

    /**
     * Parses and extracts output redirection operators ({@code >}, {@code >>}) from the line.
     * @param line the raw command line
     * @return the command line with redirection parts removed, or {@code null} if the line is empty
     */
    @Nullable
    private String parseOutputRedirection(String line) {
        if (!StringUtils.hasLength(line)) {
            return null;
        }
        String commandLine = line;
        Matcher matcher = REDIRECTION_PATTERN.matcher(line);
        List<OutputRedirection> redirectionList = new ArrayList<>();
        OutputRedirection prevRedirection = null;
        boolean hasDoubleQuote = false;
        boolean hasSingleQuote = false;
        while (matcher.find()) {
            if (matcher.group(1) != null && !hasDoubleQuote && !hasSingleQuote) {
                String str = line.substring(0, matcher.start(1)).trim();
                if (prevRedirection != null) {
                    prevRedirection.setOperand(stripQuotes(str));
                } else {
                    commandLine = str;
                }
                prevRedirection = new OutputRedirection(OutputRedirection.Operator.APPEND_OUT);
                redirectionList.add(prevRedirection);
                line = line.substring(matcher.end(1));
                matcher = REDIRECTION_PATTERN.matcher(line);
            }
            else if (matcher.group(2) != null && !hasDoubleQuote && !hasSingleQuote) {
                String str = line.substring(0, matcher.start(2)).trim();
                if (prevRedirection != null) {
                    prevRedirection.setOperand(stripQuotes(str));
                } else {
                    commandLine = str;
                }
                prevRedirection = new OutputRedirection(OutputRedirection.Operator.OVERWRITE_OUT);
                redirectionList.add(prevRedirection);
                line = line.substring(matcher.end(2));
                matcher = REDIRECTION_PATTERN.matcher(line);
            }
            else if (matcher.group(3) != null) {
                hasDoubleQuote = !hasDoubleQuote;
            }
            else if (matcher.group(4) != null) {
                hasSingleQuote = !hasSingleQuote;
            }
        }
        if (prevRedirection != null) {
            prevRedirection.setOperand(stripQuotes(line.trim()));
        }
        if (!redirectionList.isEmpty()) {
            this.redirectionList = redirectionList;
            if (logger.isDebugEnabled()) {
                logger.debug("Output Redirection: {}", OutputRedirection.serialize(redirectionList));
            }
        }
        if (StringUtils.hasLength(commandLine)) {
            return commandLine;
        } else {
            return null;
        }
    }

    /**
     * Removes leading and trailing quotes from a string if they match.
     * @param str the string to strip
     * @return the stripped string
     */
    @NonNull
    private String stripQuotes(@NonNull String str) {
        if (str.length() > 1 &&
                (str.startsWith("\"") && str.endsWith("\"") ||
                        str.startsWith("'") && str.endsWith("'"))) {
            return str.substring(1, str.length() - 1);
        } else {
            return str;
        }
    }

}
