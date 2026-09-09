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
package com.aspectran.shell;

import com.aspectran.utils.ResourceUtils;

import java.io.File;
import java.io.IOException;

/**
 * Entry point for the Session Manager Sample Shell Application.
 *
 * <p>This sample demonstrates how to use {@code SessionManager} in an Aspectran
 * shell application. It provides the following translets:
 * <ul>
 *   <li>{@code login}        - Creates a session and stores user login information.</li>
 *   <li>{@code logout}       - Invalidates the session for the given session ID.</li>
 *   <li>{@code session-info} - Displays session details for the given session ID.</li>
 *   <li>{@code session-stats}- Displays overall session statistics.</li>
 * </ul>
 *
 * <p>Usage example (after launching the shell):
 * <pre>
 *   session-sample&gt; login
 *   Enter username: alice
 *   Enter role: ADMIN
 *
 *   session-sample&gt; session-info
 *   Enter sessionId: &lt;session-id-from-login&gt;
 *
 *   session-sample&gt; session-stats
 *
 *   session-sample&gt; logout
 *   Enter sessionId: &lt;session-id-from-login&gt;
 * </pre>
 */
public class SessionSampleShellTest {

    public static void main(String[] args) throws IOException {
        String configFilePath = "config/shell/aspectran-config-session-sample.apon";
        File aspectranConfigFile = ResourceUtils.getResourceAsFile(configFilePath);
        AspectranShell.bootstrap(aspectranConfigFile);
    }

}
