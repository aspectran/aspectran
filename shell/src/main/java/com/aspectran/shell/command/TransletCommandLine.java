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

import java.util.Arrays;

/**
 * Parses the command line entered to execute the Translet.
 */
public class TransletCommandLine {

    private static final String PARAM_NAME_PREFIX = "--";

    private final CommandLineParser lineParser;

    private MethodType requestMethod;

    private ParameterMap parameterMap;

    public TransletCommandLine(CommandLineParser lineParser) {
        this.lineParser = lineParser;
        parse();
    }

    /**
     * Returns the command line parser.
     *
     * @return the command line parser
     */
    public CommandLineParser getLineParser() {
        return lineParser;
    }

    /**
     * Returns the request method of the target Translet
     * extracted from the command line.
     *
     * @return the request method
     */
    public MethodType getRequestMethod() {
        return requestMethod;
    }

    /**
     * Returns the name of the target Translet extracted
     * from the command line.
     *
     * @return the translet name
     */
    public String getTransletName() {
        return lineParser.getCommandName();
    }

    /**
     * Returns the parameters to pass to the execution target
     * Translet extracted from the command line.
     *
     * @return the parameter map
     */
    public ParameterMap getParameterMap() {
        return parameterMap;
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
    }

    private ParameterMap extractParameterMap() {
        if (!lineParser.hasArgs()) {
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

}
