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
package com.aspectran.embed.adapter;

import com.aspectran.core.adapter.DefaultSessionAdapter;
import com.aspectran.core.component.session.SessionAgent;
import com.aspectran.embed.service.EmbeddedAspectran;

/**
 * The session adapter for an embedded Aspectran environment.
 * <p>This adapter extends {@link DefaultSessionAdapter} to provide session management
 * capabilities when Aspectran is running in an embedded mode. It wraps a
 * {@link SessionAgent} that handles the underlying session state.
 * </p>
 *
 * @author Juho Jeong
 * @since 2016. 11. 26.
 * @see EmbeddedAspectran
 */
public class AspectranSessionAdapter extends DefaultSessionAdapter {

    /**
     * Creates a new {@code AspectranSessionAdapter} that delegates to the given agent.
     * @param agent the session agent that manages the session state
     */
    public AspectranSessionAdapter(SessionAgent agent) {
        super(agent);
    }

}
