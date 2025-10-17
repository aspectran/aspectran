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
 * Built-in commands provided by Aspectran Daemon.
 * <p>
 * This package contains the default command implementations available in the
 * daemon under the {@code "builtins"} namespace. These commands
 * extend the daemon {@code Command} SPI and are typically registered by the
 * daemon at startup, offering utilities for inspecting and executing translets,
 * managing scheduled jobs and aspects, printing system information, and controlling
 * daemon lifecycle.
 * </p>
 * <p>Representative commands include:</p>
 * <ul>
 *   <li>{@link com.aspectran.daemon.command.builtins.ComponentCommand ComponentCommand}
 *       — list/describe/enable/disable aspects, translets, and scheduled jobs</li>
 *   <li>{@link com.aspectran.daemon.command.builtins.TransletCommand TransletCommand}
 *       — execute translets</li>
 *   <li>{@link com.aspectran.daemon.command.builtins.InvokeActionCommand InvokeActionCommand}
 *       — executes a method on a specified bean</li>
 *   <li>{@link com.aspectran.daemon.command.builtins.SysInfoCommand SysInfoCommand}
 *       — print JVM/system information</li>
 *   <li>{@link com.aspectran.daemon.command.builtins.RestartCommand RestartCommand}
 *       and {@link com.aspectran.daemon.command.builtins.QuitCommand QuitCommand}
 *       — control daemon lifecycle</li>
 *   <li>{@link com.aspectran.daemon.command.builtins.PollingIntervalCommand PollingIntervalCommand}
 *       — change the polling interval for the file commander</li>
 * </ul>
 */
package com.aspectran.daemon.command.builtins;
