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
 * Provides a service implementation for running Aspectran as an interactive
 * command-line shell.
 * <p>This package adapts the core Aspectran service for a console-based, user-driven
 * environment. The main entry point is the
 * {@link com.aspectran.shell.service.ShellService#translate(com.aspectran.shell.command.TransletCommandLine)}
 * method, which takes a parsed command line and executes the corresponding translet.
 */
package com.aspectran.shell.service;
