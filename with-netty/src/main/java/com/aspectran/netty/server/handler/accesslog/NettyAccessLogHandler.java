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
package com.aspectran.netty.server.handler.accesslog;

import com.aspectran.netty.server.handler.logging.ChannelLoggingGroupHelper;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.logging.LoggingGroupHelper;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.util.AttributeKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.aspectran.web.support.http.HttpHeaders.X_FORWARDED_FOR;

/**
 * Netty handler that logs HTTP access events with configurable format strings and logger categories.
 *
 * <p>Created: 2026-09-02</p>
 */
@ChannelHandler.Sharable
public class NettyAccessLogHandler extends ChannelDuplexHandler {

    private static final String DEFAULT_CATEGORY = "com.aspectran.netty.accesslog";

    private static final String DEFAULT_FORMAT = "combined";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.US);

    private static final AttributeKey<AccessLogData> ACCESS_LOG_DATA_KEY =
            AttributeKey.valueOf("netty.access.data");

    private Logger logger;

    private String category = DEFAULT_CATEGORY;

    private String formatString = DEFAULT_FORMAT;

    private List<LogElement> logElements = Collections.emptyList();

    private boolean proxyAddressForwarding;

    /**
     * Constructs a new {@code NettyAccessLogHandler} with default logger category and format.
     */
    public NettyAccessLogHandler() {
        this(DEFAULT_CATEGORY);
    }

    /**
     * Constructs a new {@code NettyAccessLogHandler} with the specified logger category.
     * @param category the logger category name
     */
    public NettyAccessLogHandler(String category) {
        setCategory(category);
        setFormatString(DEFAULT_FORMAT);
    }

    /**
     * Constructs a new {@code NettyAccessLogHandler} with the specified {@link Logger}.
     * @param logger the logger to use for access logging
     */
    public NettyAccessLogHandler(Logger logger) {
        this.logger = (logger != null ? logger : LoggerFactory.getLogger(DEFAULT_CATEGORY));
        this.category = this.logger.getName();
        setFormatString(DEFAULT_FORMAT);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = (StringUtils.hasText(category) ? category.trim() : DEFAULT_CATEGORY);
        this.logger = LoggerFactory.getLogger(this.category);
    }

    public String getFormatString() {
        return formatString;
    }

    public void setFormatString(String formatString) {
        this.formatString = (StringUtils.hasText(formatString) ? formatString.trim() : DEFAULT_FORMAT);
        this.logElements = parseFormatString(this.formatString);
    }

    public boolean isProxyAddressForwarding() {
        return proxyAddressForwarding;
    }

    public void setProxyAddressForwarding(boolean proxyAddressForwarding) {
        this.proxyAddressForwarding = proxyAddressForwarding;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest request) {
            AccessLogData data = new AccessLogData();
            data.startTime = System.currentTimeMillis();

            if (proxyAddressForwarding && request.headers().contains(X_FORWARDED_FOR)) {
                String forwardedFor = request.headers().get(X_FORWARDED_FOR);
                if (StringUtils.hasText(forwardedFor)) {
                    int idx = forwardedFor.indexOf(',');
                    data.clientIp = (idx != -1 ? forwardedFor.substring(0, idx).trim() : forwardedFor.trim());
                }
            }
            if (data.clientIp == null) {
                SocketAddress remoteAddress = ctx.channel().remoteAddress();
                data.clientIp = (remoteAddress instanceof InetSocketAddress inetAddress)
                        ? inetAddress.getAddress().getHostAddress() : "127.0.0.1";
            }

            data.method = request.method().name();
            data.uri = request.uri();
            data.protocol = request.protocolVersion().text();
            data.requestLine = data.method + " " + data.uri + " " + data.protocol;
            data.headers = request.headers();

            ctx.channel().attr(ACCESS_LOG_DATA_KEY).set(data);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpResponse response) {
            AccessLogData data = ctx.channel().attr(ACCESS_LOG_DATA_KEY).getAndSet(null);
            if (data != null && logger != null && logger.isInfoEnabled()) {
                data.duration = System.currentTimeMillis() - data.startTime;
                data.statusCode = response.status().code();
                data.responseHeaders = response.headers();
                data.contentLength = HttpUtil.getContentLength(response, -1L);

                StringBuilder sb = new StringBuilder(128);
                for (LogElement element : logElements) {
                    element.append(sb, data);
                }
                ChannelLoggingGroupHelper.setFrom(ctx.channel());
                try {
                    logger.info(sb.toString());
                } finally {
                    LoggingGroupHelper.clear();
                }
            }
        }
        super.write(ctx, msg, promise);
    }

    private List<LogElement> parseFormatString(String format) {
        if ("common".equalsIgnoreCase(format)) {
            format = "%h - - %t \"%r\" %s %b";
        } else if ("combined".equalsIgnoreCase(format)) {
            format = "%h - - %t \"%r\" %s %b \"%{i,Referer}\" \"%{i,User-Agent}\"";
        }

        List<LogElement> elements = new ArrayList<>();
        int len = format.length();
        int i = 0;
        StringBuilder literal = new StringBuilder();

        while (i < len) {
            char c = format.charAt(i);
            if (c == '%') {
                if (i + 1 >= len) {
                    literal.append('%');
                    break;
                }
                char next = format.charAt(i + 1);
                if (next == '%') {
                    literal.append('%');
                    i += 2;
                    continue;
                }

                if (next == '{') {
                    int close = format.indexOf('}', i + 2);
                    if (close != -1) {
                        String inner = format.substring(i + 2, close);
                        char modifier = (close + 1 < len) ? format.charAt(close + 1) : '\0';
                        LogElement elem = parseBracedToken(inner, modifier);
                        if (elem != null) {
                            if (!literal.isEmpty()) {
                                elements.add(new ConstantElement(literal.toString()));
                                literal.setLength(0);
                            }
                            elements.add(elem);
                            i = close + (elem instanceof SuffixModifierElement ? 2 : 1);
                            continue;
                        }
                    }
                }

                LogElement elem = parseSingleToken(next);
                if (elem != null) {
                    if (!literal.isEmpty()) {
                        elements.add(new ConstantElement(literal.toString()));
                        literal.setLength(0);
                    }
                    elements.add(elem);
                    i += 2;
                    continue;
                }

                literal.append('%');
                i++;
            } else {
                literal.append(c);
                i++;
            }
        }

        if (!literal.isEmpty()) {
            elements.add(new ConstantElement(literal.toString()));
        }

        return Collections.unmodifiableList(elements);
    }

    @Nullable
    private LogElement parseBracedToken(@NonNull String inner, char modifier) {
        if (inner.startsWith("i,")) {
            return new RequestHeaderElement(inner.substring(2).trim());
        }
        if (inner.startsWith("o,")) {
            return new ResponseHeaderElement(inner.substring(2).trim());
        }
        if (inner.startsWith("c,")) {
            return new CookieElement(inner.substring(2).trim());
        }
        if (modifier == 'i') {
            return new SuffixModifierElement(new RequestHeaderElement(inner.trim()));
        }
        if (modifier == 'o') {
            return new SuffixModifierElement(new ResponseHeaderElement(inner.trim()));
        }
        if (modifier == 'c') {
            return new SuffixModifierElement(new CookieElement(inner.trim()));
        }
        if ("q".equalsIgnoreCase(inner)) {
            return new QueryStringElement();
        }
        return null;
    }

    @Nullable
    private LogElement parseSingleToken(char token) {
        return switch (token) {
            case 't' -> new DateTimeElement();
            case 'a', 'h' -> new RemoteIpElement();
            case 'r' -> new RequestLineElement();
            case 'm' -> new MethodElement();
            case 'U' -> new UriPathElement();
            case 'q' -> new QueryStringElement();
            case 's' -> new StatusCodeElement();
            case 'b' -> new ContentLengthElement(true);
            case 'B' -> new ContentLengthElement(false);
            case 'D' -> new DurationElement(true);
            case 'T' -> new DurationElement(false);
            default -> null;
        };
    }

    @FunctionalInterface
    private interface LogElement {
        void append(StringBuilder sb, AccessLogData data);
    }

    private static class ConstantElement implements LogElement {
        private final String text;

        ConstantElement(String text) {
            this.text = text;
        }

        @Override
        public void append(@NonNull StringBuilder sb, AccessLogData data) {
            sb.append(text);
        }
    }

    private static class DateTimeElement implements LogElement {
        @Override
        public void append(@NonNull StringBuilder sb, AccessLogData data) {
            sb.append('[').append(ZonedDateTime.now().format(DATE_FORMATTER)).append(']');
        }
    }

    private static class RemoteIpElement implements LogElement {
        @Override
        public void append(@NonNull StringBuilder sb, @NonNull AccessLogData data) {
            sb.append(data.clientIp != null ? data.clientIp : "-");
        }
    }

    private static class RequestLineElement implements LogElement {
        @Override
        public void append(@NonNull StringBuilder sb, @NonNull AccessLogData data) {
            sb.append(data.requestLine != null ? data.requestLine : "-");
        }
    }

    private static class MethodElement implements LogElement {
        @Override
        public void append(@NonNull StringBuilder sb, @NonNull AccessLogData data) {
            sb.append(data.method != null ? data.method : "-");
        }
    }

    private static class UriPathElement implements LogElement {
        @Override
        public void append(@NonNull StringBuilder sb, @NonNull AccessLogData data) {
            sb.append(data.getPath());
        }
    }

    private static class QueryStringElement implements LogElement {
        @Override
        public void append(@NonNull StringBuilder sb, @NonNull AccessLogData data) {
            sb.append(data.getQueryString());
        }
    }

    private static class StatusCodeElement implements LogElement {
        @Override
        public void append(@NonNull StringBuilder sb, @NonNull AccessLogData data) {
            sb.append(data.statusCode);
        }
    }

    private static class ContentLengthElement implements LogElement {
        private final boolean dashIfEmpty;

        ContentLengthElement(boolean dashIfEmpty) {
            this.dashIfEmpty = dashIfEmpty;
        }

        @Override
        public void append(StringBuilder sb, @NonNull AccessLogData data) {
            if (data.contentLength >= 0) {
                sb.append(data.contentLength);
            } else {
                sb.append(dashIfEmpty ? "-" : "0");
            }
        }
    }

    private static class DurationElement implements LogElement {
        private final boolean millis;

        DurationElement(boolean millis) {
            this.millis = millis;
        }

        @Override
        public void append(StringBuilder sb, AccessLogData data) {
            if (millis) {
                sb.append(data.duration);
            } else {
                sb.append(String.format(Locale.US, "%.3f", data.duration / 1000.0));
            }
        }
    }

    private static class RequestHeaderElement implements LogElement {
        private final String headerName;

        RequestHeaderElement(String headerName) {
            this.headerName = headerName;
        }

        @Override
        public void append(@NonNull StringBuilder sb, @NonNull AccessLogData data) {
            String val = (data.headers != null ? data.headers.get(headerName) : null);
            sb.append(val != null ? val : "-");
        }
    }

    private static class ResponseHeaderElement implements LogElement {
        private final String headerName;

        ResponseHeaderElement(String headerName) {
            this.headerName = headerName;
        }

        @Override
        public void append(@NonNull StringBuilder sb, @NonNull AccessLogData data) {
            String val = (data.responseHeaders != null ? data.responseHeaders.get(headerName) : null);
            sb.append(val != null ? val : "-");
        }
    }

    private static class CookieElement implements LogElement {
        private final String cookieName;

        CookieElement(String cookieName) {
            this.cookieName = cookieName;
        }

        @Override
        public void append(@NonNull StringBuilder sb, @NonNull AccessLogData data) {
            sb.append(data.getCookieValue(cookieName));
        }
    }

    private static class SuffixModifierElement implements LogElement {
        private final LogElement delegate;

        SuffixModifierElement(LogElement delegate) {
            this.delegate = delegate;
        }

        @Override
        public void append(StringBuilder sb, AccessLogData data) {
            delegate.append(sb, data);
        }
    }

    private static class AccessLogData {
        long startTime;
        long duration;
        String clientIp;
        String method;
        String uri;
        String protocol;
        String requestLine;
        HttpHeaders headers;
        int statusCode;
        HttpHeaders responseHeaders;
        long contentLength = -1L;
        private Map<String, String> cookies;

        @NonNull
        String getCookieValue(String name) {
            if (cookies == null) {
                cookies = parseCookies();
            }
            String val = cookies.get(name.toLowerCase(Locale.ROOT));
            return (val != null ? val : "-");
        }

        @NonNull
        private Map<String, String> parseCookies() {
            if (headers == null) {
                return Collections.emptyMap();
            }
            String cookieHeader = headers.get(HttpHeaderNames.COOKIE);
            if (!StringUtils.hasText(cookieHeader)) {
                return Collections.emptyMap();
            }
            Map<String, String> map = new HashMap<>();
            try {
                Set<Cookie> decoded = ServerCookieDecoder.STRICT.decode(cookieHeader);
                for (Cookie c : decoded) {
                    map.put(c.name().toLowerCase(Locale.ROOT), c.value());
                }
            } catch (Exception e) {
                for (String pair : cookieHeader.split(";")) {
                    String[] kv = pair.trim().split("=", 2);
                    if (kv.length == 2) {
                        map.put(kv[0].trim().toLowerCase(Locale.ROOT), kv[1].trim());
                    }
                }
            }
            return map;
        }

        String getPath() {
            if (uri == null) {
                return "-";
            }
            int q = uri.indexOf('?');
            return (q != -1 ? uri.substring(0, q) : uri);
        }

        @NonNull
        String getQueryString() {
            if (uri == null) {
                return "";
            }
            int q = uri.indexOf('?');
            return (q != -1 ? uri.substring(q) : "");
        }
    }

}
