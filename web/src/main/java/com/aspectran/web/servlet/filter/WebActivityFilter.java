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
package com.aspectran.web.servlet.filter;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.wildcard.WildcardPattern;
import com.aspectran.utils.wildcard.WildcardPatterns;
import com.aspectran.web.service.DefaultServletHttpRequestHandler;
import com.aspectran.web.service.DefaultWebService;
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
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A servlet filter that bypasses certain requests to the default servlet,
 * preventing them from being processed by Aspectran's main servlet.
 * <p>This is useful for serving static resources (e.g., images, CSS, JavaScript)
 * directly by the container's default servlet, improving performance by avoiding
 * unnecessary processing. The filter is configured via an init-parameter named
 * "bypasses", which accepts a comma-separated list of wildcard patterns for the
 * request paths to bypass.
 * </p>
 */
public class WebActivityFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(WebActivityFilter.class);

    private static final String BYPASS_PATTERN_DELIMITERS = ",;\t\r\n\f";

    /** The patterns for requests to bypass. */
    private WildcardPatterns bypassPatterns;

    /** The handler for default servlet requests. */
    private DefaultServletHttpRequestHandler defaultServletHttpRequestHandler;

    @Override
    public void init(@NonNull FilterConfig filterConfig) {
        String bypassesParam = filterConfig.getInitParameter("bypasses");
        if (bypassesParam != null) {
            String[] bypasses = StringUtils.tokenize(bypassesParam, BYPASS_PATTERN_DELIMITERS, true);
            if (bypasses.length > 0) {
                this.bypassPatterns = WildcardPatterns.of(bypasses, ActivityContext.NAME_SEPARATOR_CHAR);

                ServletContext servletContext = filterConfig.getServletContext();
                DefaultWebService webService = WebService.findWebService(servletContext);
                this.defaultServletHttpRequestHandler = webService.getDefaultServletHttpRequestHandler();

                if (logger.isDebugEnabled()) {
                    for (WildcardPattern pattern : bypassPatterns.getPatterns()) {
                        logger.debug("{} is bypassed by {} to servlet '{}'",
                                pattern,
                                ObjectUtils.simpleIdentityToString(this, filterConfig.getFilterName()),
                                this.defaultServletHttpRequestHandler.getDefaultServletName());
                    }
                }
            }
        }
    }

    /**
     * Checks if the request path matches a bypass pattern.
     * If it matches, the request is handled by the default servlet.
     * Otherwise, the request is passed to the next filter in the chain.
     */
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

}
