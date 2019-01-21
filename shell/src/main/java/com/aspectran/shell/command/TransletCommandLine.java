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

import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.shell.command.TransletOutputRedirection.Operator;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Command Line Parser.
 */
public class TransletCommandLine {

    private static final String PARAM_NAME_PREFIX = "--";

    private static final char ESCAPE = '\\';

    private static final Pattern REDIRECTION_OPERATOR_PATTERN = Pattern.compile("(>>)|(>)|(\")|(\')");

    private final CommandLineParser lineParser;

    private MethodType requestMethod;

    private ParameterMap parameterMap;

    private List<TransletOutputRedirection> redirectionList;

    public TransletCommandLine(CommandLineParser lineParser) {
        this.lineParser = lineParser;
        parse();
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
        return lineParser.getCommandName();
    }

    /**
     * Gets the command parameters.
     *
     * @return the command arguments
     */
    public ParameterMap getParameterMap() {
        return parameterMap;
    }

    public List<TransletOutputRedirection> getRedirectionList() {
        return redirectionList;
    }

    public Writer[] getRedirectionWriters(Console console) throws FileNotFoundException, UnsupportedEncodingException {
        if (redirectionList != null) {
            List<Writer> writerList = new ArrayList<>(redirectionList.size());
            for (TransletOutputRedirection redirection : redirectionList) {
                File file = new File(redirection.getOperand());
                boolean append = (redirection.getOperator() == Operator.APPEND_OUT);
                OutputStream stream = new FileOutputStream(file, append);
                writerList.add(new OutputStreamWriter(stream, console.getEncoding()));
            }
            return writerList.toArray(new Writer[0]);
        } else {
            return null;
        }
    }

    private void parse() {
        if (lineParser.getCommandName() == null) {
            return;
        }

        String commandName = lineParser.getCommandName();
        requestMethod = MethodType.resolve(commandName);
        if (requestMethod != null) {
            lineParser.shift();
        }
        if (lineParser.getCommandName() == null) {
            lineParser.setCommandName(commandName);
            return;
        }

        parameterMap = extractParameterMap();
        redirectionList = extractRedirectionList();
    }

    private ParameterMap extractParameterMap() {
        if (lineParser.getArgs().length == 0) {
            return null;
        }
        ParameterMap params = new ParameterMap();
        String name = null;
        for (String arg : lineParser.getArgs()) {
            if (arg.startsWith(PARAM_NAME_PREFIX)) {
                name = arg.substring(PARAM_NAME_PREFIX.length());
                int index = name.indexOf('=');
                if (index == 0) {
                    name = null;
                } else if (index > 0) {
                    String value = name.substring(index + 1);
                    name = name.substring(0, index);
                    String[] values = params.getParameterValues(name);
                    if (values != null) {
                        values = Arrays.copyOf(values, values.length + 1);
                        values[values.length - 1] = value;
                    } else {
                        values = new String[] { value };
                    }
                    params.setParameterValues(name, values);
                    name = null;
                } else {
                    params.setParameterValues(name, null);
                }
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
        return (!params.isEmpty() ? params : null);
    }

    private List<TransletOutputRedirection> extractRedirectionList() {
        if (!StringUtils.hasLength(lineParser.getCommandLine())) {
            return null;
        }
        String line = lineParser.getCommandLine();
        Matcher matcher = REDIRECTION_OPERATOR_PATTERN.matcher(line);
        List<TransletOutputRedirection> redirectionList = new ArrayList<>();
        TransletOutputRedirection prevRedirection = null;
        boolean hasDoubleQuote = false;
        boolean hasSingleQuote = false;
        while (matcher.find()) {
            if (matcher.group(1) != null && !hasDoubleQuote && !hasSingleQuote) {
                String str = line.substring(0, matcher.start(1)).trim();
                if (prevRedirection != null) {
                    prevRedirection.setOperand(str);
                }
                prevRedirection = new TransletOutputRedirection(Operator.APPEND_OUT);
                redirectionList.add(prevRedirection);
                line = line.substring(matcher.end(1));
                matcher = REDIRECTION_OPERATOR_PATTERN.matcher(line);
            }
            else if (matcher.group(2) != null && !hasDoubleQuote && !hasSingleQuote) {
                String str = line.substring(0, matcher.start(2)).trim();
                if (prevRedirection != null) {
                    prevRedirection.setOperand(str);
                }
                prevRedirection = new TransletOutputRedirection(Operator.OVERWRITE_OUT);
                redirectionList.add(prevRedirection);
                line = line.substring(matcher.end(2));
                matcher = REDIRECTION_OPERATOR_PATTERN.matcher(line);
            }
            else if (matcher.group(3) != null) {
                if ((matcher.start(3) == 0 || line.charAt(matcher.start(3) - 1) != ESCAPE) &&
                        !hasSingleQuote) {
                    hasDoubleQuote = !hasDoubleQuote;
                }
            }
            else if (matcher.group(4) != null) {
                if ((matcher.start(4) == 0 || line.charAt(matcher.start(4) - 1) != ESCAPE) &&
                        !hasDoubleQuote) {
                    hasSingleQuote = !hasSingleQuote;
                }
            }
        }
        if (prevRedirection != null) {
            prevRedirection.setOperand(line.trim());
        }
        return (!redirectionList.isEmpty() ? redirectionList : null);
    }

}
