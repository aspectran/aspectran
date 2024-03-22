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
package com.aspectran.web.servlet.filter;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.utils.wildcard.WildcardPattern;
import com.aspectran.web.service.DefaultServletHttpRequestHandler;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    public void init(@NonNull FilterConfig filterConfig) {
        this.filterConfig = filterConfig;

        String bypassesParam = filterConfig.getInitParameter("bypasses");
        if (bypassesParam != null) {
            String[] bypasses = StringUtils.tokenize(bypassesParam, BYPASS_PATTERN_DELIMITERS);
            if (bypasses.length > 0) {
                List<WildcardPattern> bypassPatterns = new ArrayList<>(bypasses.length);
                for (String path : bypasses) {
                    bypassPatterns.add(WildcardPattern.compile(path.trim(), ActivityContext.NAME_SEPARATOR_CHAR));
                }

                DefaultServletHttpRequestHandler defaultHandler = new DefaultServletHttpRequestHandler(filterConfig.getServletContext());
                defaultHandler.lookupDefaultServletName();

                if (logger.isDebugEnabled()) {
                    for (WildcardPattern pattern : bypassPatterns) {
                        logger.debug(pattern + " is bypassed by " + getMyName() + " to servlet '" +
                                defaultHandler.getDefaultServletName() + "'");
                    }
                }

                this.bypassPatterns = bypassPatterns;
                this.defaultServletHttpRequestHandler = defaultHandler;
            }
        }

        logger.info("Initialized " + getMyName());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            if (bypassPatterns != null) {
                for (WildcardPattern pattern : bypassPatterns) {
                    if (pattern.matches(httpRequest.getRequestURI())) {
                        if (defaultServletHttpRequestHandler.handleRequest(httpRequest, httpResponse)) {
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

    @NonNull
    private String getMyName() {
        return ObjectUtils.simpleIdentityToString(this, filterConfig.getFilterName());
    }

}
