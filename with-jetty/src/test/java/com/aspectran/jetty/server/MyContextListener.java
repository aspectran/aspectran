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
package com.aspectran.jetty.server;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Created: 4/23/24</p>
 */
public class MyContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(MyContextListener.class);

    public MyContextListener() {
        logger.info("constructor");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("contextInitialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // To this day, Jetty appears to have never properly handled this event. In other words, this event is not called!
        logger.info("contextDestroyed-1");
        System.out.println("================== contextDestroyed-1 ================== ");
    }

}
