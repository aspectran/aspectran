/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.shell.command.option;

import java.util.Properties;

/**
 * A class that implements the <code>CommandLineParser</code> interface
 * can parse a String array according to the {@link Options} specified
 * and return a {@link ParsedOptions}.
 */
public interface OptionParser {

    /**
     * Parse the arguments according to the specified options.
     *
     * @param options the specified Options
     * @param arguments the command line arguments
     * @return the list of atomic option and value tokens
     * @throws OptionParseException if there are any problems encountered
     *      while parsing the command line tokens.
     */
    ParsedOptions parse(Options options, String[] arguments) throws OptionParseException;

    /**
     * Parse the arguments according to the specified options and
     * properties.
     *
     * @param options the specified Options
     * @param arguments the command line arguments
     * @param properties command line option name-value pairs
     * @return the list of atomic option and value tokens
     * @throws OptionParseException if there are any problems encountered
     *      while parsing the command line tokens.
     */
    ParsedOptions parse(Options options, String[] arguments, Properties properties) throws OptionParseException;

    /**
     * Parse the arguments according to the specified options.
     *
     * @param options the specified Options
     * @param arguments the command line arguments
     * @param stopAtNonOption if <tt>true</tt> an unrecognized argument stops
     *      the parsing and the remaining arguments are added to the
     *      {@link ParsedOptions}s args list. If <tt>false</tt> an unrecognized
     *      argument triggers a ParseException.
     * @return the list of atomic option and value tokens
     * @throws OptionParseException if there are any problems encountered
     *      while parsing the command line tokens.
     */
    ParsedOptions parse(Options options, String[] arguments, boolean stopAtNonOption) throws OptionParseException;

    /**
     * Parse the arguments according to the specified options and
     * properties.
     *
     * @param options the specified Options
     * @param arguments the command line arguments
     * @param properties command line option name-value pairs
     * @param stopAtNonOption if <tt>true</tt> an unrecognized argument stops
     *      the parsing and the remaining arguments are added to the
     *      {@link ParsedOptions}s args list. If <tt>false</tt> an unrecognized
     *      argument triggers a ParseException.
     * @return the list of atomic option and value tokens
     * @throws OptionParseException if there are any problems encountered
     *      while parsing the command line tokens.
     */
    ParsedOptions parse(Options options, String[] arguments, Properties properties, boolean stopAtNonOption)
            throws OptionParseException;

}
