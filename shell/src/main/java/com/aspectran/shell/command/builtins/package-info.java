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
 * Built-in commands provided by the Aspectran Shell.
 * <p>
 * This package contains the default command implementations available in the
 * interactive shell under the {@code "builtins"} namespace. These commands
 * extend the shell {@code Command} SPI and are typically registered by the
 * shell at startup, offering utilities for inspecting and executing translets,
 * managing scheduled jobs and aspects, printing system information, controlling
 * shell lifecycle, and simple console helpers.
 * </p>
 * <p>Representative commands include:</p>
 * <ul>
 *   <li>{@link com.aspectran.shell.command.builtins.TransletCommand TransletCommand}
 *       — list/describe/execute translets</li>
 *   <li>{@link com.aspectran.shell.command.builtins.AspectCommand AspectCommand}
 *       — list/describe/enable/disable aspects</li>
 *   <li>{@link com.aspectran.shell.command.builtins.JobCommand JobCommand}
 *       — list/describe/enable/disable scheduled jobs</li>
 *   <li>{@link com.aspectran.shell.command.builtins.SysInfoCommand SysInfoCommand}
 *       — print JVM/system information</li>
 *   <li>{@link com.aspectran.shell.command.builtins.HelpCommand HelpCommand}
 *       — display general or command-specific help</li>
 *   <li>{@link com.aspectran.shell.command.builtins.HistoryCommand HistoryCommand}
 *       — display or clear command history</li>
 *   <li>{@link com.aspectran.shell.command.builtins.ClearCommand ClearCommand}
 *       and {@link com.aspectran.shell.command.builtins.EchoCommand EchoCommand}
 *       — console utilities</li>
 *   <li>{@link com.aspectran.shell.command.builtins.RestartCommand RestartCommand}
 *       and {@link com.aspectran.shell.command.builtins.QuitCommand QuitCommand}
 *       — control shell lifecycle</li>
 *   <li>{@link com.aspectran.shell.command.builtins.VerboseCommand VerboseCommand}
 *       — toggle verbose mode for translet execution</li>
 *   <li>{@link com.aspectran.shell.command.builtins.PBEncryptCommand PBEncryptCommand}
 *       and {@link com.aspectran.shell.command.builtins.PBDecryptCommand PBDecryptCommand}
 *       — password-based string encryption/decryption helpers</li>
 * </ul>
 */
package com.aspectran.shell.command.builtins;
