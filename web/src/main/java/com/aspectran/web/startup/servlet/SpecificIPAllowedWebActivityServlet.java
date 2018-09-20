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
package com.aspectran.web.startup.servlet;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * The Class SpecificIPAllowedWebActivityServlet.
 */
public class SpecificIPAllowedWebActivityServlet extends WebActivityServlet {

    /** @serial */
    private static final long serialVersionUID = -2369788867122156319L;

    private final Log log = LogFactory.getLog(SpecificIPAllowedWebActivityServlet.class);

    private boolean debugEnabled = log.isDebugEnabled();

    private static final String DELIMITERS = " ,;\t\n\r\f";

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
    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String remoteAddr = req.getRemoteAddr();
        if (!isAllowedAddress(remoteAddr)) {
            if (debugEnabled) {
                log.debug("Access Denied: " + remoteAddr);
            }
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        super.service(req, res);
    }

    /**
     * Returns whether IP address is valid.
     *
     * @param ipAddress the IP address
     * @return true if IP address is a valid; false otherwise
     */
    private boolean isAllowedAddress(String ipAddress) {
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