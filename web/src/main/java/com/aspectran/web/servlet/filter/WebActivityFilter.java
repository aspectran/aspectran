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
package com.aspectran.web.servlet.filter;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.utils.wildcard.WildcardPattern;
import com.aspectran.utils.wildcard.WildcardPatterns;
import com.aspectran.web.service.DefaultServletHttpRequestHandler;
import com.aspectran.web.service.WebService;
import com.aspectran.web.support.util.WebUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * The Class WebActivityFilter.
 */
public class WebActivityFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(WebActivityFilter.class);

    private static final String BYPASS_PATTERN_DELIMITERS = ",;\t\r\n\f";

    private FilterConfig filterConfig;

    private WildcardPatterns bypassPatterns;

    private DefaultServletHttpRequestHandler defaultServletHttpRequestHandler;

    @Override
    public void init(@NonNull FilterConfig filterConfig) {
        this.filterConfig = filterConfig;

        String bypassesParam = filterConfig.getInitParameter("bypasses");
        if (bypassesParam != null) {
            String[] bypasses = StringUtils.tokenize(bypassesParam, BYPASS_PATTERN_DELIMITERS, true);
            if (bypasses.length > 0) {
                this.bypassPatterns = WildcardPatterns.of(bypasses, ActivityContext.NAME_SEPARATOR_CHAR);

                ServletContext servletContext = filterConfig.getServletContext();
                WebService webService = WebService.findWebService(servletContext);
                DefaultServletHttpRequestHandler defaultHandler = new DefaultServletHttpRequestHandler(servletContext, webService);
                defaultHandler.lookupDefaultServletName();
                this.defaultServletHttpRequestHandler = defaultHandler;

                if (logger.isDebugEnabled()) {
                    for (WildcardPattern pattern : bypassPatterns.getPatterns()) {
                        logger.debug(pattern + " is bypassed by " + getMyName() + " to servlet '" +
                                defaultHandler.getDefaultServletName() + "'");
                    }
                }
            }
        }

        logger.info("Initialized " + getMyName());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (bypassPatterns != null && request instanceof HttpServletRequest httpRequest &&
                response instanceof HttpServletResponse httpResponse) {
            String requestName = WebUtils.getRelativePath(httpRequest.getContextPath(), httpRequest.getRequestURI());
            if (bypassPatterns.matches(requestName)) {
                if (defaultServletHttpRequestHandler.handleRequest(httpRequest, httpResponse)) {
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("Destroyed " + getMyName());
    }

    @NonNull
    private String getMyName() {
        return ObjectUtils.simpleIdentityToString(this, filterConfig.getFilterName());
    }

}
