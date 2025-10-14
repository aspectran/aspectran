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
 * Provides interfaces and classes for handling shell console input and output.
 * <p>This package defines the core abstractions for interacting with the user in a
 * command-line environment. The central interface is {@link com.aspectran.shell.console.ShellConsole},
 * which provides a contract for reading commands, writing output, and managing the
 * console state. It is supported by helpers like {@link com.aspectran.shell.console.ConsoleStyler}
 * for text styling and {@link com.aspectran.shell.console.PromptStringBuilder} for creating
 * dynamic prompts. The package includes a default, system-based implementation as
 * well as exceptions for console-specific error conditions.</p>
 */
package com.aspectran.shell.console;
