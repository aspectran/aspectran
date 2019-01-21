/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.util.StringUtils;
import com.aspectran.shell.command.option.DefaultOptionParser;
import com.aspectran.shell.command.option.OptionParser;
import com.aspectran.shell.command.option.OptionParserException;
import com.aspectran.shell.command.option.Options;
import com.aspectran.shell.command.option.ParsedOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Command Line Parser.
 */
public class CommandLineParser {

    private static final Pattern ARGS_SPLITTING_PATTERN = Pattern.compile("\"([^\"]*)\"|'([^']*)'|([^ ]+)");

    private final String commandLine;

    private String commandName;

    private String[] args;

    public CommandLineParser(String commandLine) {
        this.commandLine = commandLine;
        parse();
    }

    public CommandLineParser(String[] args) {
        this.commandLine = null;
        this.args = args;
        shift();
    }

    private void parse() {
        args = splitCommandLine(commandLine);
        shift();
    }

    public void shift() {
        if (args.length > 0) {
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

    public ParsedOptions getParsedOptions(Options options) throws OptionParserException {
        OptionParser parser = new DefaultOptionParser();
        return parser.parse(options, args);
    }

    private static String[] splitCommandLine(String commandLine) {
        List<String> list = new ArrayList<>();
        Matcher m = ARGS_SPLITTING_PATTERN.matcher(commandLine);
        while (m.find()) {
            if (m.group(1) != null) {
                list.add(m.group(1));
            } else if (m.group(2) != null) {
                list.add(m.group(2));
            } else {
                list.add(m.group(3));
            }
        }
        return list.toArray(new String[0]);
    }

}
