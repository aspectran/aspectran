/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
 * Support for interactive shell using the feature-rich <a href="https://github.com/jline/jline3">JLine</a>
 * library for providing Aspectran services on the command line.
 * <p>
 * Due to Windows' lack of color ANSI services out-of-the-box, this implementation automatically detects the classpath
 * presence of <a href="http://jansi.fusesource.org/">Jansi</a> and uses it if present. If in addition to Jansi, the
 * ANSI support will include colour if <a href="https://jna.dev.java.net/">JNA</a> library is also available. Neither
 * of these libraries are necessary for *nix machines, which support colour ANSI without any special effort. This
 * implementation has been written to use reflection in order to avoid hard dependencies on Jansi or JNA.
 */
package com.aspectran.shell.jline;