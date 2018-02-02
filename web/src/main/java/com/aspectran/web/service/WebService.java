/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.web.service;

import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreService;
import com.aspectran.web.startup.servlet.WebActivityServlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The Interface WebService.
 *
 * <p>Created: 2017. 10. 28.</p>
 */
public interface WebService extends CoreService {

    /**
     * ServletContext attribute name used to obtain the root WebService object.
     */
    String ROOT_WEB_SERVICE_ATTRIBUTE = WebService.class.getName() + ".ROOT";

    /**
     * The prefix of the ServletContext property name used to get the standalone WebService object.
     */
    String STANDALONE_WEB_SERVICE_ATTRIBUTE_PREFIX = WebService.class.getName() + ".STANDALONE:";

    /**
     * Executes a Web Activity.
     *
     * @param request current HTTP servlet request
     * @param response current HTTP servlet response
     * @throws IOException If an error occurs during Activity execution
     */
    void execute(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * Returns a new instance of WebService.
     *
     * @param servletContext the servlet context
     * @return the instance of WebService
     * @throws AspectranServiceException the aspectran service exception
     */
    static WebService create(ServletContext servletContext) throws AspectranServiceException {
        return AspectranWebService.create(servletContext);
    }

    /**
     * Returns a new instance of WebService.
     *
     * @param servletContext the servlet context
     * @param rootService the root service
     * @return the instance of WebService
     * @throws AspectranServiceException the aspectran service exception
     */
    static WebService create(ServletContext servletContext, CoreService rootService)
            throws AspectranServiceException {
        return AspectranWebService.create(servletContext, rootService);
    }

    /**
     * Returns a new instance of WebService.
     *
     * @param servlet the web activity servlet
     * @return the instance of WebService
     * @throws AspectranServiceException the aspectran service exception
     */
    static WebService create(WebActivityServlet servlet) throws AspectranServiceException {
        return AspectranWebService.create(servlet);
    }

    /**
     * Returns a new instance of WebService.
     *
     * @param servlet the servlet
     * @param rootService the root service
     * @return the instance of WebService
     * @throws AspectranServiceException the aspectran service exception
     */
    static WebService create(WebActivityServlet servlet, WebService rootService)
            throws AspectranServiceException {
        return AspectranWebService.create(servlet, rootService);
    }

}
