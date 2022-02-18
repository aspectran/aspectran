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
package com.aspectran.web.startup.filter;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.core.util.wildcard.WildcardPattern;
import com.aspectran.web.service.DefaultServletHttpRequestHandler;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class WebActivityFilter.
 */
public class WebActivityFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(WebActivityFilter.class);

    private static final String BYPASS_PATTERN_DELIMITERS = ",;\t\r\n\f";

    private FilterConfig filterConfig;

    private List<WildcardPattern> bypassPatterns;

    private DefaultServletHttpRequestHandler defaultServletHttpRequestHandler;

    @Override
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;

        String bypassesParam = filterConfig.getInitParameter("bypasses");
        if (bypassesParam != null) {
            String[] bypasses = StringUtils.tokenize(bypassesParam, BYPASS_PATTERN_DELIMITERS);
            if (bypasses.length > 0) {
                List<WildcardPattern> bypassPatterns = new ArrayList<>(bypasses.length);
                for (String path : bypasses) {
                    bypassPatterns.add(WildcardPattern.compile(path.trim(), ActivityContext.NAME_SEPARATOR_CHAR));
                }
                this.bypassPatterns = bypassPatterns;
                this.defaultServletHttpRequestHandler = new DefaultServletHttpRequestHandler(filterConfig.getServletContext());

                if (logger.isDebugEnabled()) {
                    for (WildcardPattern pattern : bypassPatterns) {
                        logger.debug("URI [" + pattern + "] is bypassed by " + getMyName() + " to servlet [" +
                                this.defaultServletHttpRequestHandler.getDefaultServletName() + "]");
                    }
                }
            }
        }

        logger.info("Initialized " + getMyName());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest)request;
            if (bypassPatterns != null) {
                for (WildcardPattern pattern : bypassPatterns) {
                    if (pattern.matches(httpRequest.getRequestURI())) {
                        if (defaultServletHttpRequestHandler.handleRequest(httpRequest, (HttpServletResponse)response)) {
                            return;
                        }
                    }
                }
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("Destroyed " + getMyName());
    }

    private String getMyName() {
        return getClass().getSimpleName() + '@' +
                Integer.toString(hashCode(), 16) +
                "(" + filterConfig.getFilterName() + ")";
    }

}
