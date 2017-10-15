/*
 * Copyright (c) 2008-2017 The Aspectran Project
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
package com.aspectran.console.service.command;

import com.aspectran.console.inout.ConsoleInout;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Command Parser.
 */
public class CommandParser {

    private static final Pattern redirectionOperatorPattern = Pattern.compile("(>>)|(>)|(\")|(\')");

    private static final char ESCAPE = '\\';

    private MethodType requestMethod;

    private String transletName;

    private List<CommandRedirection> redirectionList;

    /**
     * Instantiates a new Command parser.
     */
    private CommandParser() {
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
    public String getTransletName() {
        return transletName;
    }

    public List<CommandRedirection> getRedirectionList() {
        return redirectionList;
    }

    public Writer[] getRedirectionWriters(ConsoleInout consoleInout) throws FileNotFoundException, UnsupportedEncodingException {
        if (redirectionList != null) {
            List<Writer> writerList = new ArrayList<>(redirectionList.size());
            for (CommandRedirection redirection : redirectionList) {
                File file = new File(redirection.getOperand());
                boolean append = (redirection.getOperator() == CommandRedirection.Operator.APPEND_OUT);
                OutputStream stream = new FileOutputStream(file, append);
                writerList.add(new OutputStreamWriter(stream, consoleInout.getEncoding()));
            }
            return writerList.toArray(new Writer[writerList.size()]);
        } else {
            return null;
        }
    }

    /**
     * Parse the command.
     *
     * @param command the command
     */
    private void parse(String command) {
        String[] tokens = StringUtils.tokenize(command, " ", true);

        if (tokens.length > 1) {
            requestMethod = MethodType.resolve(tokens[0]);
            if (requestMethod != null) {
                transletName = command.substring(tokens[0].length()).trim();
            }
        }

        if (requestMethod == null) {
            transletName = command;
        }

        parseRedirection(transletName);
    }

    /**
     * Parse translet name and find all CommandRedirections
     *
     * @param buffer the translet name to parse
     */
    private void parseRedirection(String buffer) {
        Matcher matcher = redirectionOperatorPattern.matcher(buffer);
        List<CommandRedirection> redirectionList = new ArrayList<>();
        CommandRedirection prevRedirectionOperation = null;
        boolean haveDoubleQuote = false;
        boolean haveSingleQuote = false;

        while (matcher.find()) {
            if (matcher.group(1) != null && !haveDoubleQuote && !haveSingleQuote) {
                String string = buffer.substring(0, matcher.start(1)).trim();
                if (prevRedirectionOperation != null) {
                    prevRedirectionOperation.setOperand(string);
                } else {
                    this.transletName = string;
                }
                prevRedirectionOperation = new CommandRedirection(CommandRedirection.Operator.APPEND_OUT);
                redirectionList.add(prevRedirectionOperation);
                buffer = buffer.substring(matcher.end(1));
                matcher = redirectionOperatorPattern.matcher(buffer);
            }
            else if (matcher.group(2) != null && !haveDoubleQuote && !haveSingleQuote) {
                String string = buffer.substring(0, matcher.start(2)).trim();
                if (prevRedirectionOperation != null) {
                    prevRedirectionOperation.setOperand(string);
                } else {
                    this.transletName = string;
                }
                prevRedirectionOperation = new CommandRedirection(CommandRedirection.Operator.OVERWRITE_OUT);
                redirectionList.add(prevRedirectionOperation);
                buffer = buffer.substring(matcher.end(2));
                matcher = redirectionOperatorPattern.matcher(buffer);
            }
            else if (matcher.group(3) != null) {
                if ((matcher.start(3) == 0 || buffer.charAt(matcher.start(3) - 1) != ESCAPE) && !haveSingleQuote) {
                    haveDoubleQuote = !haveDoubleQuote;
                }
            }
            else if (matcher.group(4) != null) {
                if ((matcher.start(4) == 0 || buffer.charAt(matcher.start(4) - 1) != ESCAPE) && !haveDoubleQuote) {
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
     * Returns the command parser.
     *
     * @param command the command
     * @return the command parser
     */
    public static CommandParser parseCommand(String command) {
        CommandParser parser = new CommandParser();
        parser.parse(command);
        return parser;
    }

    public static String serialize(List<CommandRedirection> redirectionList) {
        StringBuilder sb = new StringBuilder();
        if (redirectionList != null) {
            for (CommandRedirection redirection : redirectionList) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(redirection.getOperator()).append(" ");
                sb.append(redirection.getOperand());
            }
        }
        return sb.toString();
    }

}
