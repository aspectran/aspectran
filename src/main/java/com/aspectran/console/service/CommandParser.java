/**
 * Copyright 2008-2016 Juho Jeong
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
package com.aspectran.console.service;

import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.StringUtils;

/**
 * The Command Parser.
 */
public class CommandParser {

	private MethodType requestMethod;

	private String transletName;

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

	/**
	 * Parse the command.
	 *
	 * @param command the command
	 */
	private void parse(String command) {
		String[] tokens = StringUtils.tokenize(command, " ", true);

		if(tokens.length > 1) {
			requestMethod = MethodType.resolve(tokens[0]);
			if(requestMethod != null) {
				transletName = command.substring(tokens[0].length()).trim();
			}
		}

		if(requestMethod == null) {
			transletName = command;
		}
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

}
