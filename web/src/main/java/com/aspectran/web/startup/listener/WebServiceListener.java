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
package com.aspectran.web.startup.listener;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.web.service.DefaultWebService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * The Class WebServiceListener.
 */
public class WebServiceListener implements ServletContextListener {

    private static final Log log = LogFactory.getLog(WebServiceListener.class);

    private DefaultWebService webService;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        log.info("Initializing WebServiceListener...");

        try {
            webService = DefaultWebService.create(event.getServletContext());
            webService.start();
        } catch (Exception e) {
            log.error("WebServiceListener initialization failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (webService != null) {
            log.info("Do not terminate the server while the all scoped bean destroying");

            webService.stop();
            webService = null;

            log.info("Successfully destroyed WebServiceListener: " + this);
        }
    }

}