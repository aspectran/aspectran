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
package com.aspectran.web.startup.servlet;

import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.web.service.DefaultWebService;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.aspectran.web.service.WebService.ROOT_WEB_SERVICE_ATTR_NAME;

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
            Object obj = servletContext.getAttribute(ROOT_WEB_SERVICE_ATTR_NAME);
            DefaultWebService rootWebService = null;
            if (obj != null) {
                if (!(obj instanceof DefaultWebService)) {
                    throw new IllegalStateException("Context attribute [" + obj + "] is not of type [" +
                            DefaultWebService.class.getName() + "]");
                }
                rootWebService = (DefaultWebService)obj;
                webService = DefaultWebService.create(this, rootWebService);
            } else {
                webService = DefaultWebService.create(this);
            }
            standalone = (rootWebService != webService);
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

    @NonNull
    private String getMyName() {
        return ObjectUtils.simpleIdentityToString(this, getServletName());
    }

}
