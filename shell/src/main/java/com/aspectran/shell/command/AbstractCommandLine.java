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

import com.aspectran.core.activity.request.ParameterMap;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

/**
 * An abstract base class for parsing command lines that target translets or commands.
 * <p>Given a {@link CommandLineParser}, this class extracts a {@link ParameterMap}
 * by interpreting arguments prefixed with "--" as parameter names. It supports
 * formats like {@code --name value}, {@code --name=value}, and repeated values for
 * the same parameter. Subclasses can provide domain-specific accessors over the
 * parsed data.</p>
 */
public abstract class AbstractCommandLine {

    private static final String PARAM_NAME_PREFIX = "--";

    private final ParameterMap parameterMap = new ParameterMap();

    private final CommandLineParser lineParser;

    private boolean verbose;

    /**
     * Instantiates a new abstract command line.
     * @param lineParser the command line parser
     */
    public AbstractCommandLine(@NonNull CommandLineParser lineParser) {
        this.lineParser = lineParser;
        parse();
    }

    /**
     * Returns whether verbose output is enabled.
     * @return true if verbose output is enabled, false otherwise
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Sets whether verbose output is enabled.
     * @param verbose true to enable verbose output, false otherwise
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Returns the command line parser.
     * @return the command line parser
     */
    public CommandLineParser getLineParser() {
        return lineParser;
    }

    /**
     * Returns the name of the request extracted
     * from the command line.
     * @return the request name
     */
    public String getRequestName() {
        return lineParser.getCommandName();
    }

    /**
     * Returns the parameters to pass to the execution target
     * extracted from the command line.
     * @return the parameter map
     */
    public ParameterMap getParameterMap() {
        return parameterMap;
    }

    /**
     * Parses the command line to extract parameters.
     */
    protected void parse() {
        if (lineParser.getCommandName() != null) {
            extractParameterMap();
        }
    }

    /**
     * Extracts parameters from the command line arguments.
     */
    private void extractParameterMap() {
        if (!lineParser.hasArgs()) {
            return;
        }
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
                    String[] values = parameterMap.getParameterValues(name);
                    if (values != null) {
                        values = Arrays.copyOf(values, values.length + 1);
                        values[values.length - 1] = value;
                    } else {
                        values = new String[] { value };
                    }
                    parameterMap.setParameterValues(name, values);
                    name = null;
                } else {
                    parameterMap.setParameterValues(name, null);
                }
            } else if (name != null) {
                String[] values = parameterMap.getParameterValues(name);
                if (values != null) {
                    values = Arrays.copyOf(values, values.length + 1);
                    values[values.length - 1] = arg;
                } else {
                    values = new String[] { arg };
                }
                parameterMap.setParameterValues(name, values);
                name = null;
            }
        }
    }

}
