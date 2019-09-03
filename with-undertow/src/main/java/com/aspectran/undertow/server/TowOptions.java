/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.undertow.server;

import io.undertow.UndertowOptions;
import org.xnio.OptionMap;

/**
 * <p>Created: 2019-08-18</p>
 */
public class TowOptions {

    private final OptionMap.Builder options = OptionMap.builder();

    public OptionMap getOptionMap() {
        return options.getMap();
    }

    public void setMaxHeaderSize(int maxHeaderSize) {
        options.set(UndertowOptions.MAX_HEADER_SIZE, maxHeaderSize);
    }

    public void setMaxEntitySize(long maxEntitySize) {
        options.set(UndertowOptions.MAX_ENTITY_SIZE, maxEntitySize);
    }

    public void setMultipartMaxEntitySize(long multipartMaxEntitySize) {
        options.set(UndertowOptions.MULTIPART_MAX_ENTITY_SIZE, multipartMaxEntitySize);
    }

    public void setBufferPipelinedData(boolean bufferPipelinedData) {
        options.set(UndertowOptions.BUFFER_PIPELINED_DATA, bufferPipelinedData);
    }

    public void setIdleTimeout(int idleTimeout) {
        options.set(UndertowOptions.IDLE_TIMEOUT, idleTimeout);
    }

    public void setRequestParseTimeout(int requestParseTimeout) {
        options.set(UndertowOptions.REQUEST_PARSE_TIMEOUT, requestParseTimeout);
    }

    public void setNoRequestTimeout(int noRequestTimeout) {
        options.set(UndertowOptions.NO_REQUEST_TIMEOUT, noRequestTimeout);
    }

    public void setMaxParameters(int maxParameters) {
        options.set(UndertowOptions.MAX_PARAMETERS, maxParameters);
    }

    public void setMaxHeaders(int maxHeaders) {
        options.set(UndertowOptions.MAX_HEADERS, maxHeaders);
    }

    public void setMaxCookies(int maxCookies) {
        options.set(UndertowOptions.MAX_COOKIES, maxCookies);
    }

    public void setAllowEncodedSlash(boolean allowEncodedSlash) {
        options.set(UndertowOptions.ALLOW_ENCODED_SLASH, allowEncodedSlash);
    }

    public void setDecodeUrl(boolean decodeUrl) {
        options.set(UndertowOptions.DECODE_URL, decodeUrl);
    }

    public void setUrlCharset(String urlCharset) {
        options.set(UndertowOptions.URL_CHARSET, urlCharset);
    }

    public void setAlwaysSetKeepAlive(boolean alwaysSetKeepAlive) {
        options.set(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, alwaysSetKeepAlive);
    }

    public void setAlwaysSetDate(boolean alwaysSetDate) {
        options.set(UndertowOptions.ALWAYS_SET_DATE, alwaysSetDate);
    }

    public void setMaxBufferedRequestSize(int maxBufferedRequestSize) {
        options.set(UndertowOptions.MAX_BUFFERED_REQUEST_SIZE, maxBufferedRequestSize);
    }

    public void setRecordRequestStartTime(boolean recordRequestStartTime) {
        options.set(UndertowOptions.RECORD_REQUEST_START_TIME, recordRequestStartTime);
    }

    public void setAllowEqualsInCookieValue(boolean allowEqualsInCookieValue) {
        options.set(UndertowOptions.ALLOW_EQUALS_IN_COOKIE_VALUE, allowEqualsInCookieValue);
    }

    public void setEnableRFC6265CookieValidation(boolean enableRFC6265CookieValidation) {
        options.set(UndertowOptions.ENABLE_RFC6265_COOKIE_VALIDATION, enableRFC6265CookieValidation);
    }

    public void setEnableHttp2(boolean enableHttp2) {
        options.set(UndertowOptions.ENABLE_HTTP2, enableHttp2);
    }

    public void setEnableStatistics(boolean enableStatistics) {
        options.set(UndertowOptions.ENABLE_STATISTICS, enableStatistics);
    }

    public void setAllowUnknownProtocols(boolean allowUnknownProtocols) {
        options.set(UndertowOptions.ALLOW_UNKNOWN_PROTOCOLS, allowUnknownProtocols);
    }

    public void setHttp2SettingsHeaderTableSize(int http2SettingsHeaderTableSize) {
        options.set(UndertowOptions.HTTP2_SETTINGS_HEADER_TABLE_SIZE, http2SettingsHeaderTableSize);
    }

    public void setHttp2SettingsEnablePush(boolean http2SettingsEnablePush) {
        options.set(UndertowOptions.HTTP2_SETTINGS_ENABLE_PUSH, http2SettingsEnablePush);
    }

    public void setHttp2SettingsMaxConcurrentStreams(int http2SettingsMaxConcurrentStreams) {
        options.set(UndertowOptions.HTTP2_SETTINGS_MAX_CONCURRENT_STREAMS, http2SettingsMaxConcurrentStreams);
    }

    public void setHttp2SettingsInitialWindowSize(int http2SettingsInitialWindowSize) {
        options.set(UndertowOptions.HTTP2_SETTINGS_INITIAL_WINDOW_SIZE, http2SettingsInitialWindowSize);
    }

    public void setHttp2SettingsMaxFrameSize(int http2SettingsMaxFrameSize) {
        options.set(UndertowOptions.HTTP2_SETTINGS_MAX_FRAME_SIZE, http2SettingsMaxFrameSize);
    }

    public void setHttp2PaddingSize(int http2PaddingSize) {
        options.set(UndertowOptions.HTTP2_PADDING_SIZE, http2PaddingSize);
    }

    public void setHttp2HuffmanCacheSize(int http2HuffmanCacheSize) {
        options.set(UndertowOptions.HTTP2_HUFFMAN_CACHE_SIZE, http2HuffmanCacheSize);
    }

    public void setMaxConcurrentRequestsPerConnection(int maxConcurrentRequestsPerConnection) {
        options.set(UndertowOptions.MAX_CONCURRENT_REQUESTS_PER_CONNECTION, maxConcurrentRequestsPerConnection);
    }

    public void setMaxQueuedReadBuffers(int maxQueuedReadBuffers) {
        options.set(UndertowOptions.MAX_QUEUED_READ_BUFFERS, maxQueuedReadBuffers);
    }

    public void setAjpPacketSize(int ajpPacketSize) {
        options.set(UndertowOptions.MAX_AJP_PACKET_SIZE, ajpPacketSize);
    }

    public void setRequireHostHttp11(boolean requireHostHttp11) {
        options.set(UndertowOptions.REQUIRE_HOST_HTTP11, requireHostHttp11);
    }

    public void setMaxCachedHeaderSize(int maxCachedHeaderSize) {
        options.set(UndertowOptions.MAX_CACHED_HEADER_SIZE, maxCachedHeaderSize);
    }

    public void setHttpHeadersCacheSize(int httpHeadersCacheSize) {
        options.set(UndertowOptions.HTTP_HEADERS_CACHE_SIZE, httpHeadersCacheSize);
    }

    public void setSslUserCipherSuitesOrder(boolean sslUserCipherSuitesOrder) {
        options.set(UndertowOptions.SSL_USER_CIPHER_SUITES_ORDER, sslUserCipherSuitesOrder);
    }

    public void setAllowUnescapedCharactersInUrl(boolean allowUnescapedCharactersInUrl) {
        options.set(UndertowOptions.ALLOW_UNESCAPED_CHARACTERS_IN_URL, allowUnescapedCharactersInUrl);
    }

    public void setShutdownTimeout(int shutdownTimeout) {
        options.set(UndertowOptions.SHUTDOWN_TIMEOUT, shutdownTimeout);
    }

    public void setEndpointIdentificationAlgorithm(String endpointIdentificationAlgorithm) {
        options.set(UndertowOptions.ENDPOINT_IDENTIFICATION_ALGORITHM, endpointIdentificationAlgorithm);
    }

}
