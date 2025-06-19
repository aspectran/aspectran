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

import com.aspectran.utils.annotation.jsr305.NonNull;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serial;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * The Class SpecificIPAllowedWebActivityServlet.
 */
public class SpecificIPAllowedWebActivityServlet extends WebActivityServlet {

    @Serial
    private static final long serialVersionUID = -2369788867122156319L;

    private static final Logger logger = LoggerFactory.getLogger(SpecificIPAllowedWebActivityServlet.class);

    private static final String DELIMITERS = " ,;\t\r\n\f";

    private Set<String> allowedAddresses;

    /**
     * Instantiates a new SpecificIPAllowedWebActivityServlet.
     */
    public SpecificIPAllowedWebActivityServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
        String addresses = getServletConfig().getInitParameter("allowedAddresses");
        if (addresses != null) {
            allowedAddresses = new HashSet<>();
            StringTokenizer st = new StringTokenizer(addresses, DELIMITERS);
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                allowedAddresses.add(token);
            }
        }

        super.init();
    }

    @Override
    public void service(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res) throws IOException {
        String remoteAddr = req.getRemoteAddr();
        if (!isAllowedAddress(remoteAddr)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Access Denied: {}", remoteAddr);
            }
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        super.service(req, res);
    }

    /**
     * Returns whether IP address is valid.
     * @param ipAddress the IP address
     * @return true if IP address is a valid; false otherwise
     */
    private boolean isAllowedAddress(@NonNull String ipAddress) {
        if (allowedAddresses == null) {
            return false;
        }

        // IPv4
        int offset = ipAddress.lastIndexOf('.');
        if (offset == -1) {
            // IPv6
            offset = ipAddress.lastIndexOf(':');
            if (offset == -1) {
                return false;
            }
        }

        String ipAddressClass = ipAddress.substring(0, offset + 1) + '*';
        return (allowedAddresses.contains(ipAddressClass) || allowedAddresses.contains(ipAddress));
    }

}
