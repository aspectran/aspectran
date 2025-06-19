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
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Command Line Parser.
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

    public CommandLineParser(String commandLine) {
        this.commandLine = parseOutputRedirection(commandLine);
        if (StringUtils.hasLength(this.commandLine)) {
            this.args = splitCommandLine(this.commandLine);
        }
        shift();
    }

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

    public String getCommandLine() {
        return commandLine;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String[] getArgs() {
        return args;
    }

    public boolean hasArgs() {
        return (args != null && args.length > 0);
    }

    public ParsedOptions parseOptions(Options options) throws OptionParserException {
        OptionParser parser = new DefaultOptionParser();
        return parser.parse(options, args, options.isSkipParsingAtNonOption());
    }

    /**
     * Returns a list of the output redirection extracted
     * from the command line.
     * @return a list of the output redirection
     */
    public List<OutputRedirection> getRedirectionList() {
        return redirectionList;
    }

    @NonNull
    private String[] splitCommandLine(String commandLine) {
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
