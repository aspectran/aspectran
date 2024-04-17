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
package com.aspectran.web.servlet.listener;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.web.service.DefaultWebService;
import com.aspectran.web.service.DefaultWebServiceBuilder;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import static com.aspectran.web.service.WebService.ROOT_WEB_SERVICE_ATTR_NAME;

/**
 * The Class WebServiceListener.
 */
public class WebServiceListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(WebServiceListener.class);

    private DefaultWebService webService;

    @Override
    public void contextInitialized(@NonNull ServletContextEvent event) {
        Object attr = event.getServletContext().getAttribute(ROOT_WEB_SERVICE_ATTR_NAME);
        if (attr instanceof DefaultWebService) {
            logger.warn("Root WebService already exists; Remove WebServiceListener as it is unnecessary");
            return;
        }

        logger.info("Creating Root WebService...");

        try {
            webService = DefaultWebServiceBuilder.build(event.getServletContext());
            webService.getServiceLifeCycle().start();
        } catch (Exception e) {
            logger.error("Failed to create root web service", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (webService != null) {
            logger.info("Do not terminate the server while the all scoped bean destroying");

            webService.stop();
            webService = null;

            logger.info("Successfully destroyed WebService: " + this);
        }
    }

}
