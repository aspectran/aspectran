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
package com.aspectran.web.servlet;

import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.web.service.DefaultWebService;
import com.aspectran.web.service.DefaultWebServiceBuilder;
import com.aspectran.web.service.WebService;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serial;

/**
 * The Class WebActivityServlet.
 */
public class WebActivityServlet extends HttpServlet implements Servlet {

    @Serial
    private static final long serialVersionUID = 6659683668233267847L;

    private static final Logger logger = LoggerFactory.getLogger(WebActivityServlet.class);

    private static final String METHOD_HEAD = "HEAD";

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
            DefaultWebService rootWebService = null;
            try {
                rootWebService = WebService.findWebService(getServletContext());
            } catch (IllegalStateException e) {
                // ignored
            }

            DefaultWebService newWebService;
            if (rootWebService != null) {
                newWebService = DefaultWebServiceBuilder.build(this, rootWebService);
                if (newWebService == null) {
                    this.webService = rootWebService;
                    this.standalone = false;
                } else {
                    this.standalone = true;
                }
            } else {
                newWebService = DefaultWebServiceBuilder.build(this);
                this.standalone = true;
            }
            if (newWebService != null) {
                if (newWebService.isOrphan()) {
                    newWebService.start();
                }
                this.webService = newWebService;
            }
        } catch (Exception e) {
            logger.error("Unable to initialize {}", getMyName(), e);
            throw new UnavailableException(e.getMessage());
        }

        logger.info("Initialized {}", getMyName());
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (webService.isLegacyHeadHandling() && METHOD_HEAD.equals(req.getMethod())) {
            doHead(req, res);
        } else {
            webService.service(req, res);
        }
    }

    /**
     * If the WebConfig parameter {@code legacyHeadHandling} is set to "true",
     * this {@code doHead} method is called to remove the response body.
     * As a side note, the legacy head handling mode is being deprecated
     * because it may not be accurate in producing the same head as returned
     * by the GET method.
     */
    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse res) throws IOException {
        NoBodyResponse noBodyResponse = new NoBodyResponse(res);
        webService.service(req, noBodyResponse);
        noBodyResponse.setContentLength();
    }

    @Override
    public void destroy() {
        super.destroy();

        if (standalone) {
            if (webService.isActive()) {
                webService.stop();
            }
            webService.withdraw();
        }

        logger.info("Destroyed {}", getMyName());
    }

    @NonNull
    private String getMyName() {
        return ObjectUtils.simpleIdentityToString(this, getServletName());
    }

}
