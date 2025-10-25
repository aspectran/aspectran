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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * A servlet that allows access only from specific IP addresses.
 * This servlet extends {@link WebActivityServlet} and adds a security check
 * based on the remote client's IP address.
 * <p>The allowed IP addresses are configured via the 'allowedAddresses' init-param
 * in the servlet configuration. The addresses can be a comma-separated list of
 * individual IP addresses or IP address ranges in CIDR notation (e.g., {@code 192.168.0.0/24}).
 * </p>
 * <p>
 * <strong>Note:</strong> For more advanced and robust security, using a dedicated
 * firewall or a reverse proxy for access control is recommended.
 * </p>
 */
public class SpecificIPAllowedWebActivityServlet extends WebActivityServlet {

    @Serial
    private static final long serialVersionUID = -2369788867122156319L;

    private static final Logger logger = LoggerFactory.getLogger(SpecificIPAllowedWebActivityServlet.class);

    private static final String DELIMITERS = " ,;\t\r\n\f";

    private List<Subnet> allowedSubnets;

    private Set<InetAddress> allowedAddresses;

    /**
     * Instantiates a new {@code SpecificIPAllowedWebActivityServlet}.
     */
    public SpecificIPAllowedWebActivityServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
        String addressesParam = getServletConfig().getInitParameter("allowedAddresses");
        if (addressesParam != null) {
            allowedSubnets = new ArrayList<>();
            allowedAddresses = new HashSet<>();
            StringTokenizer st = new StringTokenizer(addressesParam, DELIMITERS);
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                try {
                    if (token.contains("/")) {
                        allowedSubnets.add(new Subnet(token));
                    } else {
                        allowedAddresses.add(InetAddress.getByName(token));
                    }
                } catch (Exception e) {
                    logger.error("Invalid IP address or CIDR subnet '{}' in 'allowedAddresses' init-param", token, e);
                }
            }
        }

        super.init();
    }

    @Override
    public void service(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res) throws IOException {
        if (!isAllowedAddress(req.getRemoteAddr())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Access Denied: {}", req.getRemoteAddr());
            }
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        super.service(req, res);
    }

    /**
     * Returns whether the specified IP address is allowed.
     * @param remoteAddr the IP address to check
     * @return {@code true} if the IP address is allowed; {@code false} otherwise
     */
    private boolean isAllowedAddress(String remoteAddr) {
        if ((allowedSubnets == null || allowedSubnets.isEmpty()) &&
                (allowedAddresses == null || allowedAddresses.isEmpty())) {
            return false; // Fail-closed if nothing is configured
        }

        try {
            InetAddress remoteAddress = InetAddress.getByName(remoteAddr);
            if (allowedAddresses != null && allowedAddresses.contains(remoteAddress)) {
                return true;
            }
            if (allowedSubnets != null) {
                for (Subnet subnet : allowedSubnets) {
                    if (subnet.matches(remoteAddress)) {
                        return true;
                    }
                }
            }
        } catch (UnknownHostException e) {
            logger.warn("Could not parse remote address '{}'", remoteAddr, e);
            return false;
        }
        return false;
    }

    /**
     * Represents an IP subnet in CIDR notation.
     */
    private static class Subnet {

        private final byte[] networkAddress;

        private final int prefixLength;

        Subnet(@NonNull String cidr) throws UnknownHostException {
            String[] parts = cidr.split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid CIDR format: " + cidr);
            }
            networkAddress = InetAddress.getByName(parts[0]).getAddress();
            prefixLength = Integer.parseInt(parts[1]);
            if (prefixLength < 0 || prefixLength > networkAddress.length * 8) {
                throw new IllegalArgumentException("Invalid prefix length: " + prefixLength);
            }
        }

        boolean matches(@NonNull InetAddress remoteAddress) {
            byte[] remoteIpBytes = remoteAddress.getAddress();
            if (networkAddress.length != remoteIpBytes.length) {
                return false;
            }

            int fullBytes = prefixLength / 8;
            int remainingBits = prefixLength % 8;

            for (int i = 0; i < fullBytes; i++) {
                if (networkAddress[i] != remoteIpBytes[i]) {
                    return false;
                }
            }

            if (remainingBits > 0) {
                int mask = 0xFF << (8 - remainingBits);
                return (networkAddress[fullBytes] & mask) == (remoteIpBytes[fullBytes] & mask);
            }

            return true;
        }

    }

}
