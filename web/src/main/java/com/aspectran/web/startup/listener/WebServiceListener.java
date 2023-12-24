/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.web.service.DefaultWebService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * The Class WebServiceListener.
 */
public class WebServiceListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(WebServiceListener.class);

    private DefaultWebService webService;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        logger.info("Initializing WebServiceListener...");

        try {
            webService = DefaultWebService.create(event.getServletContext());
            webService.start();
        } catch (Exception e) {
            logger.error("WebServiceListener initialization failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (webService != null) {
            logger.info("Do not terminate the server while the all scoped bean destroying");

            webService.stop();
            webService = null;

            logger.info("Successfully destroyed WebServiceListener: " + this);
        }
    }

}
