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
package com.aspectran.web.startup.servlet;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.web.service.AspectranWebService;
import com.aspectran.web.service.WebService;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The Class WebActivityServlet.
 */
public class WebActivityServlet extends HttpServlet implements Servlet {

    /** @serial */
    private static final long serialVersionUID = 6659683668233267847L;

    private static final Log log = LogFactory.getLog(WebActivityServlet.class);

    private AspectranWebService webService;

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
            Object object = servletContext.getAttribute(WebService.ROOT_WEB_SERVICE_ATTRIBUTE);
            AspectranWebService rootService = null;
            if (object != null) {
                if (!(object instanceof AspectranWebService)) {
                    throw new IllegalStateException("Context attribute [" + object + "] is not of type [" +
                            AspectranWebService.class.getName() + "]");
                }
                rootService = (AspectranWebService)object;
                webService = AspectranWebService.create(this, rootService);
            } else {
                webService = AspectranWebService.create(this);
            }
            standalone = (rootService != webService);
            if (standalone) {
                webService.start();
                log.info(webService.getServiceName() + " is running in standalone mode inside " + getMyName());
            }
        } catch (Exception e) {
            log.error("Unable to initialize WebActivityServlet", e);
            throw new UnavailableException(e.getMessage());
        }

        log.info("Initialized " + getMyName());
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
        webService.execute(req, res);
    }

    @Override
    public void destroy() {
        super.destroy();

        if (standalone) {
            log.info("Do not terminate the application server while destroying all scoped beans");
            webService.stop();
        }

        log.info("Destroyed " + getMyName());
    }

    private String getMyName() {
        return getClass().getSimpleName() + '@' +
                Integer.toString(hashCode(), 16) +
                " [" + getServletName() + "]";
    }

}
