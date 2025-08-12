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
/**
 * Command-line option model and parser for the Aspectran Shell.
 * <p>
 * This package provides a small, focused API to declare options and parse
 * command lines used by shell commands. Core types include:
 * </p>
 * <ul>
 *   <li>{@link com.aspectran.shell.command.option.Option Option} — describes a single
 *       option (short/long name, value arity, type, requirement, description)</li>
 *   <li>{@link com.aspectran.shell.command.option.Options Options} — a collection of
 *       declared options and option groups for a command</li>
 *   <li>{@link com.aspectran.shell.command.option.OptionGroup OptionGroup} — a set of
 *       mutually exclusive options (optionally required)</li>
 *   <li>{@link com.aspectran.shell.command.option.OptionParser OptionParser} and
 *       {@link com.aspectran.shell.command.option.DefaultOptionParser DefaultOptionParser}
 *       — parse a String[] against an {@code Options} descriptor</li>
 *   <li>{@link com.aspectran.shell.command.option.ParsedOptions ParsedOptions} — results
 *       of a parse, including selected options, values and leftover arguments</li>
 *   <li>{@link com.aspectran.shell.command.option.OptionValueType OptionValueType} — built-in
 *       value type conversions for option arguments</li>
 *   <li>Exceptions: {@link com.aspectran.shell.command.option.OptionParserException},
 *       {@link com.aspectran.shell.command.option.MissingOptionException},
 *       {@link com.aspectran.shell.command.option.MissingOptionValueException},
 *       {@link com.aspectran.shell.command.option.UnrecognizedOptionException},
 *       {@link com.aspectran.shell.command.option.AlreadySelectedException}</li>
 * </ul>
 */
package com.aspectran.shell.command.option;
