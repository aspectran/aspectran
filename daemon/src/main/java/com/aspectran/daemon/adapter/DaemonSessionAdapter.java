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
package com.aspectran.daemon.adapter;

import com.aspectran.core.adapter.DefaultSessionAdapter;
import com.aspectran.core.component.session.SessionAgent;

/**
 * The session adapter for the daemon environment.
 * <p>This class extends {@link DefaultSessionAdapter} to provide session management
 * capabilities within a daemon context. It wraps a {@link SessionAgent} that handles
 * the underlying session state, making it available to daemon activities.
 * </p>
 *
 * @author Juho Jeong
 * @since 2017. 12. 12.
 */
public class DaemonSessionAdapter extends DefaultSessionAdapter {

    /**
     * Creates a new {@code DaemonSessionAdapter} that delegates to the given agent.
     * @param agent the session agent that manages the session state
     */
    public DaemonSessionAdapter(SessionAgent agent) {
        super(agent);
    }

}
