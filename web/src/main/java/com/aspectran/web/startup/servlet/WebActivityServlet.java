/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
package com.aspectran.web.startup.servlet;

import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.web.service.DefaultWebService;
import com.aspectran.web.service.WebService;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * The Class WebActivityServlet.
 */
public class WebActivityServlet extends HttpServlet implements Servlet {

    private static final long serialVersionUID = 6659683668233267847L;

    private static final Logger logger = LoggerFactory.getLogger(WebActivityServlet.class);

    private DefaultWebService webService;

    private boolean standalone;

    /**
     * Instantiates a new WebActivityServlet.
     */
    public WebActivityServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
        try {
            ServletContext servletContext = getServletContext();
            Object attr = servletContext.getAttribute(WebService.ROOT_WEB_SERVICE_ATTR_NAME);
            DefaultWebService rootService = null;
            if (attr != null) {
                if (!(attr instanceof DefaultWebService)) {
                    throw new IllegalStateException("Context attribute [" + attr + "] is not of type [" +
                            DefaultWebService.class.getName() + "]");
                }
                rootService = (DefaultWebService)attr;
                webService = DefaultWebService.create(this, rootService);
            } else {
                webService = DefaultWebService.create(this);
            }
            standalone = (rootService != webService);
            if (standalone) {
                webService.start();
                logger.info(webService.getServiceName() + " is running in standalone mode inside " + getMyName());
            }
        } catch (Exception e) {
            logger.error("Unable to initialize WebActivityServlet", e);
            throw new UnavailableException(e.getMessage());
        }

        logger.info("Initialized " + getMyName());
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
        webService.service(req, res);
    }

    @Override
    public void destroy() {
        super.destroy();

        if (standalone) {
            logger.info("Do not terminate the application server while destroying all scoped beans");
            webService.stop();
        }

        logger.info("Destroyed " + getMyName());
    }

    private String getMyName() {
        return getClass().getSimpleName() + '@' +
                Integer.toString(hashCode(), 16) +
                " [" + getServletName() + "]";
    }

}
