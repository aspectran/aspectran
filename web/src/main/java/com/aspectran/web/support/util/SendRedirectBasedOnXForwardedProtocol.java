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
package com.aspectran.web.support.util;

import com.aspectran.core.activity.Translet;
import com.aspectran.utils.StringUtils;
import com.aspectran.web.support.http.HttpHeaders;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Helper that supports sending redirects based on the HTTP X-Forwarded-Proto header.
 *
 * <p>Created: 2021/01/01</p>
 *
 * @since 6.10.0
 */
public class SendRedirectBasedOnXForwardedProtocol {

    /**
     * The setting name for determining whether to rewrite the protocol of a redirect URL to HTTPS.
     * This is relevant when a web application server uses HTTP, but a front-end proxy server (like Nginx)
     * receives HTTPS requests. If set to {@code true}, the redirect URL's protocol will be changed to HTTPS.
     * This setting is typically injected into the current {@code Activity} via an Aspect configuration,
     * for example:
     * <pre>
     *     &lt;aspect id="webTransletSettings"&gt;
     *         &lt;joinpoint&gt;
     *             pointcut: {
     *                 +: /**
     *             }
     *         &lt;/joinpoint&gt;
     *         &lt;settings&gt;
     *             &lt;setting name="characterEncoding" value="utf-8"/&gt;
     *             &lt;setting name="viewDispatcher" value="jspViewDispatcher"/&gt;
     *             &lt;setting name="proxyProtocolAware" value="true"/&gt;
     *         &lt;/settings&gt;
     *     &lt;/aspect&gt;
     * </pre>
     */
    public static final String PROXY_PROTOCOL_AWARE_SETTING_NAME = "proxyProtocolAware";

    private static final String SCHEME_DELIMITER = "://";

    /**
     * This class cannot be instantiated.
     */
    private SendRedirectBasedOnXForwardedProtocol() {
    }

    public static void redirect(@NonNull Translet translet, String location) throws IOException {
        translet.redirect(getLocation(translet, location));
    }

    public static String getLocation(Translet translet, String location) {
        String forwarded = getLocationForwarded(translet, location);
        return (forwarded != null ? forwarded : location);
    }

    @Nullable
    public static String getLocationForwarded(@NonNull Translet translet, String location) {
        String xForwardedProtocol = translet.getRequestAdapter().getHeader(HttpHeaders.X_FORWARDED_PROTO);
        if (StringUtils.hasLength(xForwardedProtocol)) {
            String host = translet.getRequestAdapter().getHeader(HttpHeaders.HOST);
            if (StringUtils.hasLength(host) && !isAbsoluteUrl(location)) {
                return (xForwardedProtocol + SCHEME_DELIMITER + host + location);
            }
        }
        return null;
    }

    private static boolean isAbsoluteUrl(String location) {
        return (location != null && location.toLowerCase().startsWith("http") && location.contains(SCHEME_DELIMITER));
    }

}
