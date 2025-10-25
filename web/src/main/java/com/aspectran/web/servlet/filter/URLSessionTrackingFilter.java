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

import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;

/**
 * A filter that removes the session ID from the request URI if it is present.
 * <p>URL-based session tracking is a fallback mechanism for when cookies are disabled,
 * but it has several drawbacks:
 * <ul>
 *     <li>Security: Session IDs can be easily leaked through logs, browser history, or referrer headers.</li>
 *     <li>SEO: Search engines may index multiple URLs for the same content, diluting page rank.</li>
 *     <li>Caching: It can interfere with the caching of responses by intermediate proxies.</li>
 * </ul>
 * This filter mitigates these issues by stripping the session ID from the URI,
 * relying on cookie-based sessions. It dynamically detects the session ID parameter name
 * (e.g., {@code jsessionid}) from the servlet context's session cookie configuration.
 *
 * <p>Created: 2025. 2. 8.</p>
 */
public class URLSessionTrackingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(URLSessionTrackingFilter.class);

    private static final String DEFAULT_DELIMITER = ";jsessionid=";

    private String delimiter = DEFAULT_DELIMITER;

    @Override
    public void init(@NonNull FilterConfig filterConfig) {
        String name = filterConfig.getServletContext().getSessionCookieConfig().getName();
        if (StringUtils.hasText(name)) {
            delimiter = ";" + name.toLowerCase(Locale.ENGLISH) + "=";
        }
        if (logger.isDebugEnabled()) {
            String myName = ObjectUtils.simpleIdentityToString(this, filterConfig.getFilterName());
            ToStringBuilder tsb = new ToStringBuilder(myName);
            tsb.append("delimiter", delimiter);
            logger.debug(tsb.toString());
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String requestURI = httpRequest.getRequestURI();
            if (requestURI.contains(delimiter)) {
                final String requestURItoUse = StringUtils.divide(httpRequest.getRequestURI(), delimiter)[0];
                HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(httpRequest) {
                    @Override
                    public String getRequestURI() {
                        return requestURItoUse;
                    }
                };
                chain.doFilter(requestWrapper, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

}
