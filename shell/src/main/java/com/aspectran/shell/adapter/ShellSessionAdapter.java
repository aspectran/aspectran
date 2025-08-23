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
package com.aspectran.shell.adapter;

import com.aspectran.core.adapter.DefaultSessionAdapter;
import com.aspectran.core.component.session.SessionAgent;

/**
 * The session adapter for the interactive shell environment.
 * <p>This adapter provides session capabilities for shell activities, allowing state
 * to be maintained across multiple command executions. It extends
 * {@link DefaultSessionAdapter} and wraps a {@link SessionAgent} that manages the
 * underlying session state.
 * </p>
 *
 * @author Juho Jeong
 * @since 2017. 3. 4.
 */
public class ShellSessionAdapter extends DefaultSessionAdapter {

    /**
     * Creates a new {@code ShellSessionAdapter} that delegates to the given agent.
     * @param agent the session agent that manages the session state
     */
    public ShellSessionAdapter(SessionAgent agent) {
        super(agent);
    }

}
