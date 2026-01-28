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
package com.aspectran.undertow.server;

import io.undertow.UndertowOptions;
import org.xnio.CompressionType;
import org.xnio.OptionMap;
import org.xnio.Options;

/**
 * A bean-style wrapper for configuring Undertow and XNIO options.
 * <p>This class provides convenient setter methods for common Undertow and XNIO options.
 * Internally, it builds an {@link OptionMap} which can then be applied to the server,
 * socket, or worker configurations. This allows for easy, declarative configuration
 * in Aspectran's bean definition files.</p>
 *
 * <p>Created: 2019-08-18</p>
 */
public class TowOptions {

    private final OptionMap.Builder options = OptionMap.builder();

    /**
     * Returns the underlying {@link OptionMap} containing all configured options.
     * @return the option map
     */
    public OptionMap getOptionMap() {
        return options.getMap();
    }

    //---------------------------------------------------------//
    // Undertow Server Options
    //---------------------------------------------------------//

    /**
     * @see UndertowOptions#MAX_HEADER_SIZE
     */
    public void setMaxHeaderSize(int maxHeaderSize) {
        options.set(UndertowOptions.MAX_HEADER_SIZE, maxHeaderSize);
    }

    /**
     * @see UndertowOptions#MAX_ENTITY_SIZE
     */
    public void setMaxEntitySize(long maxEntitySize) {
        options.set(UndertowOptions.MAX_ENTITY_SIZE, maxEntitySize);
    }

    /**
     * @see UndertowOptions#MULTIPART_MAX_ENTITY_SIZE
     */
    public void setMultipartMaxEntitySize(long multipartMaxEntitySize) {
        options.set(UndertowOptions.MULTIPART_MAX_ENTITY_SIZE, multipartMaxEntitySize);
    }

    /**
     * @see UndertowOptions#BUFFER_PIPELINED_DATA
     */
    public void setBufferPipelinedData(boolean bufferPipelinedData) {
        options.set(UndertowOptions.BUFFER_PIPELINED_DATA, bufferPipelinedData);
    }

    /**
     * @see UndertowOptions#IDLE_TIMEOUT
     */
    public void setIdleTimeout(int idleTimeout) {
        options.set(UndertowOptions.IDLE_TIMEOUT, idleTimeout);
    }

    /**
     * @see UndertowOptions#REQUEST_PARSE_TIMEOUT
     */
    public void setRequestParseTimeout(int requestParseTimeout) {
        options.set(UndertowOptions.REQUEST_PARSE_TIMEOUT, requestParseTimeout);
    }

    /**
     * @see UndertowOptions#NO_REQUEST_TIMEOUT
     */
    public void setNoRequestTimeout(int noRequestTimeout) {
        options.set(UndertowOptions.NO_REQUEST_TIMEOUT, noRequestTimeout);
    }

    /**
     * @see UndertowOptions#MAX_PARAMETERS
     */
    public void setMaxParameters(int maxParameters) {
        options.set(UndertowOptions.MAX_PARAMETERS, maxParameters);
    }

    /**
     * @see UndertowOptions#MAX_HEADERS
     */
    public void setMaxHeaders(int maxHeaders) {
        options.set(UndertowOptions.MAX_HEADERS, maxHeaders);
    }

    /**
     * @see UndertowOptions#MAX_COOKIES
     */
    public void setMaxCookies(int maxCookies) {
        options.set(UndertowOptions.MAX_COOKIES, maxCookies);
    }

    /**
     * @see UndertowOptions#DECODE_URL
     */
    public void setDecodeUrl(boolean decodeUrl) {
        options.set(UndertowOptions.DECODE_URL, decodeUrl);
    }

    /**
     * @see UndertowOptions#URL_CHARSET
     */
    public void setUrlCharset(String urlCharset) {
        options.set(UndertowOptions.URL_CHARSET, urlCharset);
    }

    /**
     * @see UndertowOptions#ALWAYS_SET_KEEP_ALIVE
     */
    public void setAlwaysSetKeepAlive(boolean alwaysSetKeepAlive) {
        options.set(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, alwaysSetKeepAlive);
    }

    /**
     * @see UndertowOptions#ALWAYS_SET_DATE
     */
    public void setAlwaysSetDate(boolean alwaysSetDate) {
        options.set(UndertowOptions.ALWAYS_SET_DATE, alwaysSetDate);
    }

    /**
     * @see UndertowOptions#MAX_BUFFERED_REQUEST_SIZE
     */
    public void setMaxBufferedRequestSize(int maxBufferedRequestSize) {
        options.set(UndertowOptions.MAX_BUFFERED_REQUEST_SIZE, maxBufferedRequestSize);
    }

    /**
     * @see UndertowOptions#RECORD_REQUEST_START_TIME
     */
    public void setRecordRequestStartTime(boolean recordRequestStartTime) {
        options.set(UndertowOptions.RECORD_REQUEST_START_TIME, recordRequestStartTime);
    }

    /**
     * @see UndertowOptions#ALLOW_EQUALS_IN_COOKIE_VALUE
     */
    public void setAllowEqualsInCookieValue(boolean allowEqualsInCookieValue) {
        options.set(UndertowOptions.ALLOW_EQUALS_IN_COOKIE_VALUE, allowEqualsInCookieValue);
    }

    /**
     * @see UndertowOptions#ENABLE_RFC6265_COOKIE_VALIDATION
     */
    public void setEnableRFC6265CookieValidation(boolean enableRFC6265CookieValidation) {
        options.set(UndertowOptions.ENABLE_RFC6265_COOKIE_VALIDATION, enableRFC6265CookieValidation);
    }

    /**
     * @see UndertowOptions#ENABLE_HTTP2
     */
    public void setEnableHttp2(boolean enableHttp2) {
        options.set(UndertowOptions.ENABLE_HTTP2, enableHttp2);
    }

    /**
     * @see UndertowOptions#ENABLE_STATISTICS
     */
    public void setEnableStatistics(boolean enableStatistics) {
        options.set(UndertowOptions.ENABLE_STATISTICS, enableStatistics);
    }

    /**
     * @see UndertowOptions#ALLOW_UNKNOWN_PROTOCOLS
     */
    public void setAllowUnknownProtocols(boolean allowUnknownProtocols) {
        options.set(UndertowOptions.ALLOW_UNKNOWN_PROTOCOLS, allowUnknownProtocols);
    }

    /**
     * @see UndertowOptions#HTTP2_SETTINGS_HEADER_TABLE_SIZE
     */
    public void setHttp2SettingsHeaderTableSize(int http2SettingsHeaderTableSize) {
        options.set(UndertowOptions.HTTP2_SETTINGS_HEADER_TABLE_SIZE, http2SettingsHeaderTableSize);
    }

    /**
     * @see UndertowOptions#HTTP2_SETTINGS_ENABLE_PUSH
     */
    public void setHttp2SettingsEnablePush(boolean http2SettingsEnablePush) {
        options.set(UndertowOptions.HTTP2_SETTINGS_ENABLE_PUSH, http2SettingsEnablePush);
    }

    /**
     * @see UndertowOptions#HTTP2_SETTINGS_MAX_CONCURRENT_STREAMS
     */
    public void setHttp2SettingsMaxConcurrentStreams(int http2SettingsMaxConcurrentStreams) {
        options.set(UndertowOptions.HTTP2_SETTINGS_MAX_CONCURRENT_STREAMS, http2SettingsMaxConcurrentStreams);
    }

    /**
     * @see UndertowOptions#HTTP2_SETTINGS_INITIAL_WINDOW_SIZE
     */
    public void setHttp2SettingsInitialWindowSize(int http2SettingsInitialWindowSize) {
        options.set(UndertowOptions.HTTP2_SETTINGS_INITIAL_WINDOW_SIZE, http2SettingsInitialWindowSize);
    }

    /**
     * @see UndertowOptions#HTTP2_SETTINGS_MAX_FRAME_SIZE
     */
    public void setHttp2SettingsMaxFrameSize(int http2SettingsMaxFrameSize) {
        options.set(UndertowOptions.HTTP2_SETTINGS_MAX_FRAME_SIZE, http2SettingsMaxFrameSize);
    }

    /**
     * @see UndertowOptions#HTTP2_PADDING_SIZE
     */
    public void setHttp2PaddingSize(int http2PaddingSize) {
        options.set(UndertowOptions.HTTP2_PADDING_SIZE, http2PaddingSize);
    }

    /**
     * @see UndertowOptions#HTTP2_HUFFMAN_CACHE_SIZE
     */
    public void setHttp2HuffmanCacheSize(int http2HuffmanCacheSize) {
        options.set(UndertowOptions.HTTP2_HUFFMAN_CACHE_SIZE, http2HuffmanCacheSize);
    }

    /**
     * @see UndertowOptions#MAX_CONCURRENT_REQUESTS_PER_CONNECTION
     */
    public void setMaxConcurrentRequestsPerConnection(int maxConcurrentRequestsPerConnection) {
        options.set(UndertowOptions.MAX_CONCURRENT_REQUESTS_PER_CONNECTION, maxConcurrentRequestsPerConnection);
    }

    /**
     * @see UndertowOptions#MAX_QUEUED_READ_BUFFERS
     */
    public void setMaxQueuedReadBuffers(int maxQueuedReadBuffers) {
        options.set(UndertowOptions.MAX_QUEUED_READ_BUFFERS, maxQueuedReadBuffers);
    }

    /**
     * @see UndertowOptions#MAX_AJP_PACKET_SIZE
     */
    public void setAjpPacketSize(int ajpPacketSize) {
        options.set(UndertowOptions.MAX_AJP_PACKET_SIZE, ajpPacketSize);
    }

    /**
     * @see UndertowOptions#REQUIRE_HOST_HTTP11
     */
    public void setRequireHostHttp11(boolean requireHostHttp11) {
        options.set(UndertowOptions.REQUIRE_HOST_HTTP11, requireHostHttp11);
    }

    /**
     * @see UndertowOptions#MAX_CACHED_HEADER_SIZE
     */
    public void setMaxCachedHeaderSize(int maxCachedHeaderSize) {
        options.set(UndertowOptions.MAX_CACHED_HEADER_SIZE, maxCachedHeaderSize);
    }

    /**
     * @see UndertowOptions#HTTP_HEADERS_CACHE_SIZE
     */
    public void setHttpHeadersCacheSize(int httpHeadersCacheSize) {
        options.set(UndertowOptions.HTTP_HEADERS_CACHE_SIZE, httpHeadersCacheSize);
    }

    /**
     * @see UndertowOptions#SSL_USER_CIPHER_SUITES_ORDER
     */
    public void setSslUserCipherSuitesOrder(boolean sslUserCipherSuitesOrder) {
        options.set(UndertowOptions.SSL_USER_CIPHER_SUITES_ORDER, sslUserCipherSuitesOrder);
    }

    /**
     * @see UndertowOptions#ALLOW_UNESCAPED_CHARACTERS_IN_URL
     */
    public void setAllowUnescapedCharactersInUrl(boolean allowUnescapedCharactersInUrl) {
        options.set(UndertowOptions.ALLOW_UNESCAPED_CHARACTERS_IN_URL, allowUnescapedCharactersInUrl);
    }

    /**
     * @see UndertowOptions#SHUTDOWN_TIMEOUT
     */
    public void setShutdownTimeout(int shutdownTimeout) {
        options.set(UndertowOptions.SHUTDOWN_TIMEOUT, shutdownTimeout);
    }

    /**
     * @see UndertowOptions#ENDPOINT_IDENTIFICATION_ALGORITHM
     */
    public void setEndpointIdentificationAlgorithm(String endpointIdentificationAlgorithm) {
        options.set(UndertowOptions.ENDPOINT_IDENTIFICATION_ALGORITHM, endpointIdentificationAlgorithm);
    }

    /**
     * @see UndertowOptions#DECODE_SLASH
     */
    public void setDecodeSlash(boolean decodeSlash) {
        options.set(UndertowOptions.DECODE_SLASH, decodeSlash);
    }

    /**
     * @see UndertowOptions#SSL_SNI_HOSTNAME
     */
    public void setSslSniHostname(String sslSniHostname) {
        options.set(UndertowOptions.SSL_SNI_HOSTNAME, sslSniHostname);
    }

    /**
     * @see UndertowOptions#QUEUED_FRAMES_HIGH_WATER_MARK
     */
    public void setQueuedFramesHighWaterMark(int queuedFramesHighWaterMark) {
        options.set(UndertowOptions.QUEUED_FRAMES_HIGH_WATER_MARK, queuedFramesHighWaterMark);
    }

    /**
     * @see UndertowOptions#QUEUED_FRAMES_LOW_WATER_MARK
     */
    public void setQueuedFramesLowWaterMark(int queuedFramesLowWaterMark) {
        options.set(UndertowOptions.QUEUED_FRAMES_LOW_WATER_MARK, queuedFramesLowWaterMark);
    }

    /**
     * @see UndertowOptions#AJP_ALLOWED_REQUEST_ATTRIBUTES_PATTERN
     */
    public void setAjpAllowedRequestAttributesPattern(String ajpAllowedRequestAttributesPattern) {
        options.set(UndertowOptions.AJP_ALLOWED_REQUEST_ATTRIBUTES_PATTERN, ajpAllowedRequestAttributesPattern);
    }

    /**
     * @see UndertowOptions#TRACK_ACTIVE_REQUESTS
     */
    public void setTrackActiveRequests(boolean trackActiveRequests) {
        options.set(UndertowOptions.TRACK_ACTIVE_REQUESTS, trackActiveRequests);
    }

    /**
     * @see UndertowOptions#RST_FRAMES_TIME_WINDOW
     */
    public void setRstFramesTimeWindow(int rstFramesTimeWindow) {
        options.set(UndertowOptions.RST_FRAMES_TIME_WINDOW, rstFramesTimeWindow);
    }

    /**
     * @see UndertowOptions#MAX_RST_FRAMES_PER_WINDOW
     */
    public void setMaxRstFramesPerWindow(int maxRstFramesPerWindow) {
        options.set(UndertowOptions.MAX_RST_FRAMES_PER_WINDOW, maxRstFramesPerWindow);
    }

    //---------------------------------------------------------//
    // XNIO Socket Options
    //---------------------------------------------------------//

    /**
     * @see Options#ALLOW_BLOCKING
     */
    public void setAllowBlocking(boolean allowBlocking) {
        options.set(Options.ALLOW_BLOCKING, allowBlocking);
    }

    /**
     * @see Options#MULTICAST
     */
    public void setMulticast(boolean multicast) {
        options.set(Options.MULTICAST, multicast);
    }

    /**
     * @see Options#BROADCAST
     */
    public void setBroadcast(boolean broadcast) {
        options.set(Options.BROADCAST, broadcast);
    }

    /**
     * @see Options#CLOSE_ABORT
     */
    public void setCloseAbort(boolean closeAbort) {
        options.set(Options.CLOSE_ABORT, closeAbort);
    }

    /**
     * @see Options#RECEIVE_BUFFER
     */
    public void setReceiveBuffer(int receiveBuffer) {
        options.set(Options.RECEIVE_BUFFER, receiveBuffer);
    }

    /**
     * @see Options#REUSE_ADDRESSES
     */
    public void setReuseAddresses(boolean reuseAddresses) {
        options.set(Options.REUSE_ADDRESSES, reuseAddresses);
    }

    /**
     * @see Options#SEND_BUFFER
     */
    public void setSendBuffer(int sendBuffer) {
        options.set(Options.SEND_BUFFER, sendBuffer);
    }

    /**
     * @see Options#TCP_NODELAY
     */
    public void setTcpNodelay(boolean tcpNodelay) {
        options.set(Options.TCP_NODELAY, tcpNodelay);
    }

    /**
     * @see Options#MULTICAST_TTL
     */
    public void setMulticastTtl(int multicastTtl) {
        options.set(Options.MULTICAST_TTL, multicastTtl);
    }

    /**
     * @see Options#IP_TRAFFIC_CLASS
     */
    public void setIpTrafficClass(int ipTrafficClass) {
        options.set(Options.IP_TRAFFIC_CLASS, ipTrafficClass);
    }

    /**
     * @see Options#TCP_OOB_INLINE
     */
    public void setTcpOobInline(boolean tcpOobInline) {
        options.set(Options.TCP_OOB_INLINE, tcpOobInline);
    }

    /**
     * @see Options#KEEP_ALIVE
     */
    public void setKeepAlive(boolean keepAlive) {
        options.set(Options.KEEP_ALIVE, keepAlive);
    }

    /**
     * @see Options#BACKLOG
     */
    public void setBacklog(int backlog) {
        options.set(Options.BACKLOG, backlog);
    }

    /**
     * @see Options#READ_TIMEOUT
     */
    public void setReadTimeout(int readTimeout) {
        options.set(Options.READ_TIMEOUT, readTimeout);
    }

    /**
     * @see Options#WRITE_TIMEOUT
     */
    public void setWriteTimeout(int writeTimeout) {
        options.set(Options.WRITE_TIMEOUT, writeTimeout);
    }

    /**
     * @see Options#MAX_INBOUND_MESSAGE_SIZE
     */
    public void setMaxInboundMessageSize(int maxInboundMessageSize) {
        options.set(Options.MAX_INBOUND_MESSAGE_SIZE, maxInboundMessageSize);
    }

    /**
     * @see Options#MAX_OUTBOUND_MESSAGE_SIZE
     */
    public void setMaxOutboundMessageSize(int maxOutboundMessageSize) {
        options.set(Options.MAX_OUTBOUND_MESSAGE_SIZE, maxOutboundMessageSize);
    }

    /**
     * @see Options#SSL_ENABLED
     */
    public void setSslEnabled(boolean sslEnabled) {
        options.set(Options.SSL_ENABLED, sslEnabled);
    }

    /**
     * @see Options#SSL_PROVIDER
     */
    public void setSslProvider(String sslProvider) {
        options.set(Options.SSL_PROVIDER, sslProvider);
    }

    /**
     * @see Options#SSL_PROTOCOL
     */
    public void setProtocol(String protocol) {
        options.set(Options.SSL_PROTOCOL, protocol);
    }

    /**
     * @see Options#SSL_ENABLE_SESSION_CREATION
     */
    public void setSslEnableSessionCreation(boolean sslEnableSessionCreation) {
        options.set(Options.SSL_ENABLE_SESSION_CREATION, sslEnableSessionCreation);
    }

    /**
     * @see Options#SSL_USE_CLIENT_MODE
     */
    public void setSslUseClientMode(boolean sslUseClientMode) {
        options.set(Options.SSL_USE_CLIENT_MODE, sslUseClientMode);
    }

    /**
     * @see Options#SSL_CLIENT_SESSION_CACHE_SIZE
     */
    public void setSslClientSessionCacheSize(int sslClientSessionCacheSize) {
        options.set(Options.SSL_CLIENT_SESSION_CACHE_SIZE, sslClientSessionCacheSize);
    }

    /**
     * @see Options#SSL_CLIENT_SESSION_TIMEOUT
     */
    public void setSslClientSessionTimeout(int sslClientSessionTimeout) {
        options.set(Options.SSL_CLIENT_SESSION_TIMEOUT, sslClientSessionTimeout);
    }

    /**
     * @see Options#SSL_SERVER_SESSION_CACHE_SIZE
     */
    public void setSslServerSessionCacheSize(int sslServerSessionCacheSize) {
        options.set(Options.SSL_SERVER_SESSION_CACHE_SIZE, sslServerSessionCacheSize);
    }

    /**
     * @see Options#SSL_SERVER_SESSION_TIMEOUT
     */
    public void setSslServerSessionTimeout(int sslServerSessionTimeout) {
        options.set(Options.SSL_SERVER_SESSION_TIMEOUT, sslServerSessionTimeout);
    }

    /**
     * @see Options#SSL_PACKET_BUFFER_SIZE
     */
    public void setSslPacketBufferSize(int sslPacketBufferSize) {
        options.set(Options.SSL_PACKET_BUFFER_SIZE, sslPacketBufferSize);
    }

    /**
     * @see Options#SSL_PACKET_BUFFER_REGION_SIZE
     */
    public void setSslPacketRegionSize(int sslPacketRegionSize) {
        options.set(Options.SSL_PACKET_BUFFER_REGION_SIZE, sslPacketRegionSize);
    }

    /**
     * @see Options#SSL_APPLICATION_BUFFER_REGION_SIZE
     */
    public void setSslApplicationBufferRegionSize(int sslApplicationBufferRegionSize) {
        options.set(Options.SSL_APPLICATION_BUFFER_REGION_SIZE, sslApplicationBufferRegionSize);
    }

    /**
     * @see Options#SSL_STARTTLS
     */
    public void setSslStartTls(boolean sslStartTls) {
        options.set(Options.SSL_STARTTLS, sslStartTls);
    }

    /**
     * @see Options#SSL_PEER_HOST_NAME
     */
    public void setSslPeerHostName(String sslPeerHostName) {
        options.set(Options.SSL_PEER_HOST_NAME, sslPeerHostName);
    }

    /**
     * @see Options#SSL_PEER_PORT
     */
    public void setSslPeerPort(int sslPeerPort) {
        options.set(Options.SSL_PEER_PORT, sslPeerPort);
    }

    /**
     * @see Options#SSL_NON_BLOCKING_KEY_MANAGER
     */
    public void setSslNonBlockingKeyManager(boolean sslNonBlockingKeyManager) {
        options.set(Options.SSL_NON_BLOCKING_KEY_MANAGER, sslNonBlockingKeyManager);
    }

    /**
     * @see Options#SSL_NON_BLOCKING_TRUST_MANAGER
     */
    public void setSslNonBlockingTrustManager(boolean sslNonBlockingTrustManager) {
        options.set(Options.SSL_NON_BLOCKING_TRUST_MANAGER, sslNonBlockingTrustManager);
    }

    /**
     * @see Options#USE_DIRECT_BUFFERS
     */
    public void setUseDirectBuffers(boolean useDirectBuffers) {
        options.set(Options.USE_DIRECT_BUFFERS, useDirectBuffers);
    }

    /**
     * @see Options#SECURE
     */
    public void setSecure(boolean secure) {
        options.set(Options.SECURE, secure);
    }

    /**
     * @see Options#SASL_POLICY_FORWARD_SECRECY
     */
    public void setSaslPolicyForwardSecrecy(boolean saslPolicyForwardSecrecy) {
        options.set(Options.SASL_POLICY_FORWARD_SECRECY, saslPolicyForwardSecrecy);
    }

    /**
     * @see Options#SASL_POLICY_NOACTIVE
     */
    public void setSaslPolicyNoactive(boolean saslPolicyNoactive) {
        options.set(Options.SASL_POLICY_NOACTIVE, saslPolicyNoactive);
    }

    /**
     * @see Options#SASL_POLICY_NOANONYMOUS
     */
    public void setSaslPolicyNoanonymous(boolean saslPolicyNoanonymous) {
        options.set(Options.SASL_POLICY_NOANONYMOUS, saslPolicyNoanonymous);
    }

    /**
     * @see Options#SASL_POLICY_NODICTIONARY
     */
    public void setSaslPolicyNodictionary(boolean saslPolicyNodictionary) {
        options.set(Options.SASL_POLICY_NODICTIONARY, saslPolicyNodictionary);
    }

    /**
     * @see Options#SASL_POLICY_NOPLAINTEXT
     */
    public void setSaslPolicyNoplaintext(boolean saslPolicyNoplaintext) {
        options.set(Options.SASL_POLICY_NOPLAINTEXT, saslPolicyNoplaintext);
    }

    /**
     * @see Options#SASL_POLICY_PASS_CREDENTIALS
     */
    public void setSaslPolicyPassCredentials(boolean saslPolicyPassCredentials) {
        options.set(Options.SASL_POLICY_PASS_CREDENTIALS, saslPolicyPassCredentials);
    }

    /**
     * @see Options#SASL_SERVER_AUTH
     */
    public void setSaslServerAuth(boolean saslServerAuth) {
        options.set(Options.SASL_SERVER_AUTH, saslServerAuth);
    }

    /**
     * @see Options#SASL_REUSE
     */
    public void setSaslReuse(boolean saslReuse) {
        options.set(Options.SASL_REUSE, saslReuse);
    }

    /**
     * @see Options#FILE_APPEND
     */
    public void setFileAppend(boolean fileAppend) {
        options.set(Options.FILE_APPEND, fileAppend);
    }

    /**
     * @see Options#FILE_CREATE
     */
    public void setFileCreate(boolean fileCreate) {
        options.set(Options.FILE_CREATE, fileCreate);
    }

    /**
     * @see Options#SSL_APPLICATION_BUFFER_SIZE
     */
    public void setSslApplicationBufferSize(int sslApplicationBufferSize) {
        options.set(Options.SSL_APPLICATION_BUFFER_SIZE, sslApplicationBufferSize);
    }

    //---------------------------------------------------------//
    // XNIO Worker Options
    //---------------------------------------------------------//

    /**
     * @see Options#STACK_SIZE
     */
    public void setStackSize(long stackSize) {
        options.set(Options.STACK_SIZE, stackSize);
    }

    /**
     * @see Options#WORKER_NAME
     */
    public void setWorkerName(String workerName) {
        options.set(Options.WORKER_NAME, workerName);
    }

    /**
     * @see Options#THREAD_PRIORITY
     */
    public void setThreadPriority(int threadPriority) {
        options.set(Options.THREAD_PRIORITY, threadPriority);
    }

    /**
     * @see Options#THREAD_DAEMON
     */
    public void setThreadDaemon(boolean threadDaemon) {
        options.set(Options.THREAD_DAEMON, threadDaemon);
    }

    /**
     * @see Options#WORKER_IO_THREADS
     */
    public void setWorkerIoThreads(int workerIoThreads) {
        options.set(Options.WORKER_IO_THREADS, workerIoThreads);
    }

    /**
     * @see Options#WORKER_ESTABLISH_WRITING
     */
    public void setWorkerEstablishWriting(boolean workerEstablishWriting) {
        options.set(Options.WORKER_ESTABLISH_WRITING, workerEstablishWriting);
    }

    /**
     * @see Options#WORKER_TASK_CORE_THREADS
     */
    public void setWorkerTaskCoreThreads(int workerTaskCoreThreads) {
        options.set(Options.WORKER_TASK_CORE_THREADS, workerTaskCoreThreads);
    }

    /**
     * @see Options#WORKER_TASK_MAX_THREADS
     */
    public void setWorkerTaskMaxThreads(int workerTaskMaxThreads) {
        options.set(Options.WORKER_TASK_MAX_THREADS, workerTaskMaxThreads);
    }

    /**
     * @see Options#WORKER_TASK_KEEPALIVE
     */
    public void setWorkerTaskKeepalive(int workerTaskKeepalive) {
        options.set(Options.WORKER_TASK_KEEPALIVE, workerTaskKeepalive);
    }

    /**
     * @see Options#WORKER_TASK_LIMIT
     */
    public void setWorkerTaskLimit(int workerTaskLimit) {
        options.set(Options.WORKER_TASK_LIMIT, workerTaskLimit);
    }

    /**
     * @see Options#CORK
     */
    public void setCork(boolean cork) {
        options.set(Options.CORK, cork);
    }

    /**
     * @see Options#CONNECTION_HIGH_WATER
     */
    public void setConnectionHighWater(int connectionHighWater) {
        options.set(Options.CONNECTION_HIGH_WATER, connectionHighWater);
    }

    /**
     * @see Options#CONNECTION_LOW_WATER
     */
    public void setConnectionLowWater(int connectionLowWater) {
        options.set(Options.CONNECTION_LOW_WATER, connectionLowWater);
    }

    /**
     * @see Options#COMPRESSION_LEVEL
     */
    public void setCompressionLevel(int compressionLevel) {
        options.set(Options.COMPRESSION_LEVEL, compressionLevel);
    }

    /**
     * @see Options#COMPRESSION_TYPE
     */
    public void setCompressionType(CompressionType compressionType) {
        options.set(Options.COMPRESSION_TYPE, compressionType);
    }

    /**
     * @see Options#BALANCING_TOKENS
     */
    public void setBalancingTokens(int balancingTokens) {
        options.set(Options.BALANCING_TOKENS, balancingTokens);
    }

    /**
     * @see Options#BALANCING_CONNECTIONS
     */
    public void setBalancingConnections(int balancingConnections) {
        options.set(Options.BALANCING_CONNECTIONS, balancingConnections);
    }

    /**
     * @see Options#WATCHER_POLL_INTERVAL
     */
    public void setWatcherPollInterval(int watcherPollInterval) {
        options.set(Options.WATCHER_POLL_INTERVAL, watcherPollInterval);
    }

    /**
     * @see Options#WORKER_READ_THREADS
     */
    public void setWorkerReadThreads(int workerReadThreads) {
        options.set(Options.WORKER_READ_THREADS, workerReadThreads);
    }

    /**
     * @see Options#WORKER_WRITE_THREADS
     */
    public void setWorkerWriteThreads(int workerWriteThreads) {
        options.set(Options.WORKER_WRITE_THREADS, workerWriteThreads);
    }

    /**
     * @see Options#SPLIT_READ_WRITE_THREADS
     */
    public void setSplitReadWriteThreads(boolean splitReadWriteThreads) {
        options.set(Options.SPLIT_READ_WRITE_THREADS, splitReadWriteThreads);
    }

}
