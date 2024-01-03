/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.core.activity;

import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.adapter.DefaultSessionAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionAgent;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.core.util.Aspectran;
import com.aspectran.utils.ResourceUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 1/3/24</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InstantActivityTest {

    private ActivityContextBuilder builder;

    private ActivityContext context;

    private SessionAdapter sessionAdapter;

    @BeforeAll
    void ready() throws Exception {
        File baseDir = ResourceUtils.getResourceAsFile(".");

        builder = new HybridActivityContextBuilder();
        builder.setBasePath(baseDir.getCanonicalPath());
        context = builder.build("/config/activity/instant-activity-config.xml");

        SessionManager sessionManager = createSessionManager();
        SessionAgent sessionAgent = new SessionAgent(sessionManager.getSessionHandler());
        sessionAdapter = new DefaultSessionAdapter(sessionAgent);
    }

    @AfterAll
    void finish() {
        if (builder != null) {
            builder.destroy();
        }
    }

    private SessionManager createSessionManager() throws Exception {
        SessionManagerConfig sessionManagerConfig = new SessionManagerConfig();
        sessionManagerConfig.setWorkerName("t0");
        DefaultSessionManager sessionManager = new DefaultSessionManager();
        sessionManager.setApplicationAdapter(context.getApplicationAdapter());
        sessionManager.setSessionManagerConfig(sessionManagerConfig);
        sessionManager.initialize();
        return sessionManager;
    }

    @Test
    void transletEcho11() throws TransletNotFoundException, ActivityPrepareException, ActivityPerformException, IOException {
        assertEquals("Hello", instantActivity1("/echo"));
    }

    @Test
    void transletInclude11() throws TransletNotFoundException, ActivityPrepareException, ActivityPerformException, IOException {
        assertEquals("Hello World!", instantActivity1("/include11"));
    }

    @Test
    void transletInclude12() throws TransletNotFoundException, ActivityPrepareException, ActivityPerformException, IOException {
        assertEquals("Hello World", instantActivity1("/include12"));
    }

    @Test
    void transletInclude13() throws TransletNotFoundException, ActivityPrepareException, ActivityPerformException, IOException {
        assertEquals("Hello World", instantActivity1("/include13"));
    }

    private String instantActivity1(String transletName) throws TransletNotFoundException, ActivityPrepareException, ActivityPerformException, IOException {
        ParameterMap parameterMap = new ParameterMap();
        parameterMap.setParameter("msg", "Hello");
        InstantActivity activity = new InstantActivity(context);
        activity.setSessionAdapter(sessionAdapter);
        activity.setParameterMap(parameterMap);
        activity.prepare(transletName);
        activity.perform(() -> {
            activity.getSessionAdapter().setAttribute("aspectran", Aspectran.POWERED_BY);
            return activity.getSessionAdapter().getAttribute("aspectran");
        });
        Writer writer = activity.getResponseAdapter().getWriter();
        return writer.toString();
    }

    @Test
    void transletInclude21() throws TransletNotFoundException, ActivityPrepareException, ActivityPerformException, IOException {
        assertEquals("Hello World!", instantActivity2("/include21"));
    }

    @Test
    void transletInclude22() throws TransletNotFoundException, ActivityPrepareException, ActivityPerformException, IOException {
        assertEquals("Hello World", instantActivity2("/include22"));
    }

    @Test
    void transletInclude23() throws TransletNotFoundException, ActivityPrepareException, ActivityPerformException, IOException {
        assertEquals("Hello World", instantActivity2("/include23"));
    }

    private String instantActivity2(String transletName) throws TransletNotFoundException, ActivityPrepareException, ActivityPerformException, IOException {
        InstantActivity activity = new InstantActivity(context);
        activity.setSessionAdapter(sessionAdapter);
        activity.prepare(transletName);
        activity.perform(() -> {
            activity.getSessionAdapter().setAttribute("aspectran", Aspectran.POWERED_BY);
            return activity.getSessionAdapter().getAttribute("aspectran");
        });
        Writer writer = activity.getResponseAdapter().getWriter();
        return writer.toString();
    }

}
