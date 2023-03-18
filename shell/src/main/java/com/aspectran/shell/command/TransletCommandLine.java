/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
 * Parses the command line entered to execute the translet.
 */
public class TransletCommandLine extends AbstractCommandLine {

    private MethodType requestMethod;

    public TransletCommandLine(CommandLineParser lineParser) {
        super(lineParser);
    }

    /**
     * Returns the request method of the target translet
     * extracted from the command line.
     *
     * @return the request method
     */
    public MethodType getRequestMethod() {
        return requestMethod;
    }

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
