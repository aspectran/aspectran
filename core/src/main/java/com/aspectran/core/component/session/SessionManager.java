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
package com.aspectran.core.component.session;

import com.aspectran.core.component.Component;
import com.aspectran.core.context.config.SessionConfig;

/**
 * The session manager initializes and destroys the session handler and session cache.
 */
public interface SessionManager extends Component {

    String getGroupName();

    void setGroupName(String groupName);

    SessionConfig getSessionConfig();

    void setSessionConfig(SessionConfig sessionConfig);

    SessionDataStore getSessionDataStore();

    void setSessionDataStore(SessionDataStore sessionDataStore);

    SessionHandler getSessionHandler();

    SessionAgent newSessionAgent();

}
