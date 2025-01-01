/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.web.service.DefaultWebService;
import com.aspectran.web.service.DefaultWebServiceBuilder;
import com.aspectran.web.service.WebService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * The Class WebServiceListener.
 */
public class WebServiceListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(WebServiceListener.class);

    private DefaultWebService webService;

    @Override
    public void contextInitialized(@NonNull ServletContextEvent event) {
        try {
            WebService.findWebService(event.getServletContext());
            logger.warn("Root WebService already exists; Remove WebServiceListener as it is unnecessary");
            return;
        } catch (IllegalStateException ignored) {
            // ignore
        }

        logger.info("Creating Root WebService...");

        try {
            webService = DefaultWebServiceBuilder.build(event.getServletContext());
            webService.start();

            logger.info("Initialized " + getMyName());
        } catch (Exception e) {
            logger.error("Failed to create root web service", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (webService != null) {
            webService.stop();
            webService = null;

            logger.info("Destroyed " + getMyName());
        }
    }

    @NonNull
    private String getMyName() {
        return ObjectUtils.simpleIdentityToString(this);
    }

}
