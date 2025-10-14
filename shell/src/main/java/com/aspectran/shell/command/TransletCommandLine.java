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

import com.aspectran.core.context.rule.type.MethodType;

/**
 * A specialized command line parser for executing Aspectran Translets.
 * <p>This class extends {@link AbstractCommandLine} to add logic for parsing
 * an optional HTTP request method (e.g., GET, POST) from the command name.
 * For example, in the command {@code GET /some/translet --param1 value1},
 * "GET" is parsed as the request method and "/some/translet" becomes the
 * request name.</p>
 */
public class TransletCommandLine extends AbstractCommandLine {

    private MethodType requestMethod;

    /**
     * Instantiates a new translet command line parser.
     * @param lineParser the base command line parser
     */
    public TransletCommandLine(CommandLineParser lineParser) {
        super(lineParser);
    }

    /**
     * Returns the request method for the target translet, if specified.
     * @return the request method, or {@code null} if not specified
     */
    public MethodType getRequestMethod() {
        return requestMethod;
    }

    /**
     * Parses the command line to identify the request method and translet name.
     */
    @Override
    protected void parse() {
        if (getLineParser().getCommandName() != null) {
            String commandName = getLineParser().getCommandName();
            requestMethod = MethodType.resolve(commandName);
            if (requestMethod != null) {
                getLineParser().shift();
            }
            if (getLineParser().getCommandName() == null) {
                getLineParser().setCommandName(commandName);
            }
        }
        super.parse();
    }

}
