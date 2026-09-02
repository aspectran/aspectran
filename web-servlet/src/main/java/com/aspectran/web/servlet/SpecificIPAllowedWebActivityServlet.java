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

import com.aspectran.utils.net.IpAddressUtils;
import com.aspectran.web.servlet.support.util.ServletWebUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serial;

/**
 * A servlet that allows access only from specific IP addresses.
 * This servlet extends {@link WebActivityServlet} and adds a security check
 * based on the remote client's IP address.
 * <p>The allowed IP addresses are configured via the 'allowedAddresses' init-param
 * in the servlet configuration. The addresses can be a comma-separated list of
 * individual IP addresses, octet-level wildcards (e.g. {@code 192.168.1.*}), or
 * IP address ranges in CIDR notation (e.g., {@code 192.168.0.0/24}).</p>
 * <p>
 * <strong>Note:</strong> For more advanced and robust security, using a dedicated
 * firewall or a reverse proxy for access control is recommended.</p>
 */
public class SpecificIPAllowedWebActivityServlet extends WebActivityServlet {

    @Serial
    private static final long serialVersionUID = -2369788867122156319L;

    private static final Logger logger = LoggerFactory.getLogger(SpecificIPAllowedWebActivityServlet.class);

    private String allowedAddresses;

    /**
     * Instantiates a new {@code SpecificIPAllowedWebActivityServlet}.
     */
    public SpecificIPAllowedWebActivityServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
        allowedAddresses = getServletConfig().getInitParameter("allowedAddresses");
        super.init();
    }

    @Override
    public void service(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res) throws IOException {
        String remoteAddr = ServletWebUtils.getRemoteAddr(req);
        if (allowedAddresses == null || !IpAddressUtils.isAllowedIp(remoteAddr, allowedAddresses)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Access Denied: {}", remoteAddr);
            }
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        super.service(req, res);
    }

}
