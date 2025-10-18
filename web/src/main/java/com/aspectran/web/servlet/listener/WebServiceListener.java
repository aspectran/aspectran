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
package com.aspectran.web.servlet.listener;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.web.service.DefaultWebService;
import com.aspectran.web.service.DefaultWebServiceBuilder;
import com.aspectran.web.service.WebService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ServletContextListener} that creates and destroys a {@link WebService}
 * for the root web application context.
 */
public class WebServiceListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(WebServiceListener.class);

    private DefaultWebService webService;

    @Override
    public void contextInitialized(@NonNull ServletContextEvent event) {
        try {
            WebService.findWebService(event.getServletContext());
            logger.warn("A Root WebService already exists. The WebServiceListener is not necessary and will be ignored.");
            return;
        } catch (IllegalStateException ignored) {
            // ignore
        }

        logger.info("Initializing Root WebService...");

        try {
            webService = DefaultWebServiceBuilder.build(event.getServletContext());
            webService.start();

            logger.info("Root WebService has been initialized");
        } catch (Exception e) {
            logger.error("Failed to create root web service", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (webService != null) {
            webService.stop();
            webService = null;

            logger.info("Root WebService has been destroyed");
        }
    }

}
