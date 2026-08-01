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
package com.aspectran.utils.net;

import com.aspectran.utils.StringUtils;
import org.jspecify.annotations.NonNull;

import java.net.InetAddress;

/**
 * Utility class for IP address pattern matching, octet-level wildcards, and CIDR checks.
 *
 * @since 9.6.5
 */
public abstract class IpAddressUtils {

    /**
     * Checks if the given client IP matches the allowed IPs pattern list.
     * Supports exact IP, octet-level wildcards (e.g. 192.168.1.*), and CIDR notation (e.g. 10.0.0.0/8).
     * @param clientIp the client IP address to check
     * @param allowedIps comma/space-separated list of allowed IP patterns
     * @return true if allowed, false otherwise
     */
    public static boolean isAllowedIp(String clientIp, String allowedIps) {
        if (!StringUtils.hasText(allowedIps)) {
            return true;
        }
        if (!StringUtils.hasText(clientIp)) {
            return false;
        }
        String[] patterns = StringUtils.tokenize(allowedIps, ", \t\n\r\f;");
        for (String pattern : patterns) {
            pattern = pattern.trim();
            if (pattern.isEmpty() || "*".equals(pattern)) {
                return true;
            }
            if (matchIpPattern(clientIp, pattern)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchIpPattern(String clientIp, @NonNull String pattern) {
        if (pattern.equalsIgnoreCase(clientIp)) {
            return true;
        }
        // Handle CIDR notation (e.g. 192.168.1.0/24)
        if (pattern.contains("/")) {
            return matchCidr(clientIp, pattern);
        }
        // Handle IPv4 octet-level matching (e.g. 192.168.1.*)
        if (clientIp.contains(".") && pattern.contains(".")) {
            String[] ipParts = clientIp.split("\\.");
            String[] patternParts = pattern.split("\\.");
            if (ipParts.length == 4 && patternParts.length == 4) {
                for (int i = 0; i < 4; i++) {
                    String p = patternParts[i].trim();
                    if (!"*".equals(p) && !p.equalsIgnoreCase(ipParts[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        // Handle IPv6 octet-level matching (e.g. fe80::*)
        if (clientIp.contains(":") && pattern.contains(":")) {
            String[] ipParts = clientIp.split(":");
            String[] patternParts = pattern.split(":");
            if (patternParts.length <= ipParts.length) {
                for (int i = 0; i < patternParts.length; i++) {
                    String p = patternParts[i].trim();
                    if (!"*".equals(p) && !p.equalsIgnoreCase(ipParts[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static boolean matchCidr(String clientIp, String cidrPattern) {
        try {
            String[] parts = cidrPattern.split("/");
            if (parts.length != 2) {
                return false;
            }
            String baseIp = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            byte[] ipBytes = InetAddress.getByName(clientIp).getAddress();
            byte[] baseBytes = InetAddress.getByName(baseIp).getAddress();
            if (ipBytes.length != baseBytes.length) {
                return false;
            }

            int bytesToCheck = prefixLength / 8;
            for (int i = 0; i < bytesToCheck; i++) {
                if (ipBytes[i] != baseBytes[i]) {
                    return false;
                }
            }
            int remainderBits = prefixLength % 8;
            if (remainderBits > 0 && bytesToCheck < ipBytes.length) {
                int mask = 0xFF << (8 - remainderBits);
                if ((ipBytes[bytesToCheck] & mask) != (baseBytes[bytesToCheck] & mask)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
