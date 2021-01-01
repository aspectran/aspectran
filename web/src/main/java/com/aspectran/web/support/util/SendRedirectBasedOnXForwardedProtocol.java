package com.aspectran.web.support.util;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.util.StringUtils;
import com.aspectran.web.support.http.HttpHeaders;

import java.io.IOException;

/**
 * Helper that supports sending redirects based on the HTTP X-Forwarded-Proto header.
 *
 * <p>Created: 2021/01/01</p>
 *
 * @since 6.9.10
 */
public class SendRedirectBasedOnXForwardedProtocol {

    private static final String SCHEME_DELIMITER = "://";

    public static void redirect(Translet translet, String location) throws IOException {
        translet.redirect(getLocation(translet, location));
    }

    public static String getLocation(Translet translet, String location) {
        String forwarded = getLocationForwarded(translet, location);
        return (forwarded != null ? forwarded : location);
    }

    public static String getLocationForwarded(Translet translet, String location) {
        String xForwardedProtocol = translet.getRequestAdapter().getHeader(HttpHeaders.X_FORWARDED_PROTO);
        if (StringUtils.hasLength(xForwardedProtocol)) {
            String host = translet.getRequestAdapter().getHeader(HttpHeaders.HOST);
            if (StringUtils.hasLength(host) && !isAbsoluteUrl(location)) {
                return xForwardedProtocol + SCHEME_DELIMITER + host + location;
            }
        }
        return null;
    }

    private static boolean isAbsoluteUrl(String location) {
        return (location != null && location.toLowerCase().startsWith("http") && location.contains(SCHEME_DELIMITER));
    }

}
