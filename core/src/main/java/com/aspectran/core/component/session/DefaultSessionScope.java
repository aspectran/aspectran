/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.activity.SessionScopeActivity;
import com.aspectran.core.activity.aspect.SessionScopeAdvisor;
import com.aspectran.core.adapter.AdviceBasicSessionAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.bean.scope.SessionScope;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.io.Serializable;

/**
 * The Class DefaultSessionScope.
 */
public class DefaultSessionScope extends SessionScope implements SessionBindingListener, Serializable {

    private static final long serialVersionUID = 937698459900960084L;

    private static final Log log = LogFactory.getLog(DefaultSessionScope.class);

    private SessionScopeAdvisor advisor;

    /**
     * Instantiates a new HttpSessionScope.
     *
     * @param advisor the session scope advisor
     */
    public DefaultSessionScope(SessionScopeAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public void valueBound(Session session, String name, Object value) {
        if (log.isDebugEnabled()) {
            log.debug("New DefaultSessionScope bound in session " + session);
        }

        if (advisor != null) {
            advisor.executeBeforeAdvice();
        }
    }

    @Override
    public void valueUnbound(Session session, String name, Object value) {
        if (log.isDebugEnabled()) {
            log.debug("DefaultSessionScope removed from session " + session);
        }

        if (advisor != null) {
            SessionAdapter sessionAdapter = new AdviceBasicSessionAdapter(session);
            SessionScopeActivity activity = advisor.getSessionScopeActivity();
            activity.setSessionAdapter(sessionAdapter);
            advisor.executeAfterAdvice();
            advisor = null;
        }

        // destroy the session scoped beans
        destroy();
    }

}
