/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

    public void setAllowBlocking(boolean allowBlocking) {
        options.set(Options.ALLOW_BLOCKING, allowBlocking);
    }

    public void setMulticast(boolean multicast) {
        options.set(Options.MULTICAST, multicast);
    }

    public void setBroadcast(boolean broadcast) {
        options.set(Options.BROADCAST, broadcast);
    }

    public void setCloseAbort(boolean closeAbort) {
        options.set(Options.CLOSE_ABORT, closeAbort);
    }

    public void setReceiveBuffer(int receiveBuffer) {
        options.set(Options.RECEIVE_BUFFER, receiveBuffer);
    }

    public void setReuseAddresses(boolean reuseAddresses) {
        options.set(Options.REUSE_ADDRESSES, reuseAddresses);
    }

    public void setSendBuffer(int sendBuffer) {
        options.set(Options.SEND_BUFFER, sendBuffer);
    }

    public void setTcpNodelay(boolean tcpNodelay) {
        options.set(Options.TCP_NODELAY, tcpNodelay);
    }

    public void setMulticastTtl(int multicastTtl) {
        options.set(Options.MULTICAST_TTL, multicastTtl);
    }

    public void setIpTrafficClass(int ipTrafficClass) {
        options.set(Options.IP_TRAFFIC_CLASS, ipTrafficClass);
    }

    public void setTcpOobInline(boolean tcpOobInline) {
        options.set(Options.TCP_OOB_INLINE, tcpOobInline);
    }

    public void setKeepAlive(boolean keepAlive) {
        options.set(Options.KEEP_ALIVE, keepAlive);
    }

    public void setBacklog(int backlog) {
        options.set(Options.BACKLOG, backlog);
    }

    public void setReadTimeout(int readTimeout) {
        options.set(Options.READ_TIMEOUT, readTimeout);
    }

    public void setWriteTimeout(int writeTimeout) {
        options.set(Options.WRITE_TIMEOUT, writeTimeout);
    }

    public void setMaxInboundMessageSize(int maxInboundMessageSize) {
        options.set(Options.MAX_INBOUND_MESSAGE_SIZE, maxInboundMessageSize);
    }

    public void setMaxOutboundMessageSize(int maxOutboundMessageSize) {
        options.set(Options.MAX_OUTBOUND_MESSAGE_SIZE, maxOutboundMessageSize);
    }

    public void setSslEnabled(boolean sslEnabled) {
        options.set(Options.SSL_ENABLED, sslEnabled);
    }

    public void setSslProvider(String sslProvider) {
        options.set(Options.SSL_PROVIDER, sslProvider);
    }

    public void setProtocol(String protocol) {
        options.set(Options.SSL_PROTOCOL, protocol);
    }

    public void setSslEnableSessionCreation(boolean sslEnableSessionCreation) {
        options.set(Options.SSL_ENABLE_SESSION_CREATION, sslEnableSessionCreation);
    }

    public void setSslUseClientMode(boolean sslUseClientMode) {
        options.set(Options.SSL_USE_CLIENT_MODE, sslUseClientMode);
    }

    public void setSslClientSessionCacheSize(int sslClientSessionCacheSize) {
        options.set(Options.SSL_CLIENT_SESSION_CACHE_SIZE, sslClientSessionCacheSize);
    }

    public void setSslClientSessionTimeout(int sslClientSessionTimeout) {
        options.set(Options.SSL_CLIENT_SESSION_TIMEOUT, sslClientSessionTimeout);
    }

    public void setSslServerSessionCacheSize(int sslServerSessionCacheSize) {
        options.set(Options.SSL_SERVER_SESSION_CACHE_SIZE, sslServerSessionCacheSize);
    }

    public void setSslServerSessionTimeout(int sslServerSessionTimeout) {
        options.set(Options.SSL_SERVER_SESSION_TIMEOUT, sslServerSessionTimeout);
    }

    public void setSslPacketBufferSize(int sslPacketBufferSize) {
        options.set(Options.SSL_PACKET_BUFFER_SIZE, sslPacketBufferSize);
    }

    public void setSslPacketRegionSize(int sslPacketRegionSize) {
        options.set(Options.SSL_PACKET_BUFFER_REGION_SIZE, sslPacketRegionSize);
    }

    public void setSslApplicationBufferRegionSize(int sslApplicationBufferRegionSize) {
        options.set(Options.SSL_APPLICATION_BUFFER_REGION_SIZE, sslApplicationBufferRegionSize);
    }

    public void setSslStartTls(boolean sslStartTls) {
        options.set(Options.SSL_STARTTLS, sslStartTls);
    }

    public void setSslPeerHostName(String sslPeerHostName) {
        options.set(Options.SSL_PEER_HOST_NAME, sslPeerHostName);
    }

    public void setSslPeerPort(int sslPeerPort) {
        options.set(Options.SSL_PEER_PORT, sslPeerPort);
    }

    public void setSslNonBlockingKeyManager(boolean sslNonBlockingKeyManager) {
        options.set(Options.SSL_NON_BLOCKING_KEY_MANAGER, sslNonBlockingKeyManager);
    }

    public void setSslNonBlockingTrustManager(boolean sslNonBlockingTrustManager) {
        options.set(Options.SSL_NON_BLOCKING_TRUST_MANAGER, sslNonBlockingTrustManager);
    }

    public void setUseDirectBuffers(boolean useDirectBuffers) {
        options.set(Options.USE_DIRECT_BUFFERS, useDirectBuffers);
    }

    public void setSecure(boolean secure) {
        options.set(Options.SECURE, secure);
    }

    public void setSaslPolicyForwardSecrecy(boolean saslPolicyForwardSecrecy) {
        options.set(Options.SASL_POLICY_FORWARD_SECRECY, saslPolicyForwardSecrecy);
    }

    public void setSaslPolicyNoactive(boolean saslPolicyNoactive) {
        options.set(Options.SASL_POLICY_NOACTIVE, saslPolicyNoactive);
    }

    public void setSaslPolicyNoanonymous(boolean saslPolicyNoanonymous) {
        options.set(Options.SASL_POLICY_NOANONYMOUS, saslPolicyNoanonymous);
    }

    public void setSaslPolicyNodictionary(boolean saslPolicyNodictionary) {
        options.set(Options.SASL_POLICY_NODICTIONARY, saslPolicyNodictionary);
    }

    public void setSaslPolicyNoplaintext(boolean saslPolicyNoplaintext) {
        options.set(Options.SASL_POLICY_NOPLAINTEXT, saslPolicyNoplaintext);
    }

    public void setSaslPolicyPassCredentials(boolean saslPolicyPassCredentials) {
        options.set(Options.SASL_POLICY_PASS_CREDENTIALS, saslPolicyPassCredentials);
    }

    public void setSaslServerAuth(boolean saslServerAuth) {
        options.set(Options.SASL_SERVER_AUTH, saslServerAuth);
    }

    public void setSaslReuse(boolean saslReuse) {
        options.set(Options.SASL_REUSE, saslReuse);
    }

    public void setFileAppend(boolean fileAppend) {
        options.set(Options.FILE_APPEND, fileAppend);
    }

    public void setFileCreate(boolean fileCreate) {
        options.set(Options.FILE_CREATE, fileCreate);
    }

    public void setStackSize(long stackSize) {
        options.set(Options.STACK_SIZE, stackSize);
    }

    public void setWorkerName(String workerName) {
        options.set(Options.WORKER_NAME, workerName);
    }

    public void setThreadPriority(int threadPriority) {
        options.set(Options.THREAD_PRIORITY, threadPriority);
    }

    public void setThreadDaemon(boolean threadDaemon) {
        options.set(Options.THREAD_DAEMON, threadDaemon);
    }

    public void setWorkerIoThreads(int workerIoThreads) {
        options.set(Options.WORKER_IO_THREADS, workerIoThreads);
    }

    public void setWorkerEstablishWriting(boolean workerEstablishWriting) {
        options.set(Options.WORKER_ESTABLISH_WRITING, workerEstablishWriting);
    }

    public void setWorkerTaskCoreThreads(int workerTaskCoreThreads) {
        options.set(Options.WORKER_TASK_CORE_THREADS, workerTaskCoreThreads);
    }

    public void setWorkerTaskMaxThreads(int workerTaskMaxThreads) {
        options.set(Options.WORKER_TASK_MAX_THREADS, workerTaskMaxThreads);
    }

    public void setWorkerTaskKeepalive(int workerTaskKeepalive) {
        options.set(Options.WORKER_TASK_KEEPALIVE, workerTaskKeepalive);
    }

    public void setWorkerTaskLimit(int workerTaskLimit) {
        options.set(Options.WORKER_TASK_LIMIT, workerTaskLimit);
    }

    public void setCork(boolean cork) {
        options.set(Options.CORK, cork);
    }

    public void setConnectionHighWater(int connectionHighWater) {
        options.set(Options.CONNECTION_HIGH_WATER, connectionHighWater);
    }

    public void setConnectionLowWater(int connectionLowWater) {
        options.set(Options.CONNECTION_LOW_WATER, connectionLowWater);
    }

    public void setCompressionLevel(int compressionLevel) {
        options.set(Options.COMPRESSION_LEVEL, compressionLevel);
    }

    public void setCompressionType(CompressionType compressionType) {
        options.set(Options.COMPRESSION_TYPE, compressionType);
    }

    public void setBalancingTokens(int balancingTokens) {
        options.set(Options.BALANCING_TOKENS, balancingTokens);
    }

    public void setBalancingConnections(int balancingConnections) {
        options.set(Options.BALANCING_CONNECTIONS, balancingConnections);
    }

    public void setWatcherPollInterval(int watcherPollInterval) {
        options.set(Options.WATCHER_POLL_INTERVAL, watcherPollInterval);
    }

}
