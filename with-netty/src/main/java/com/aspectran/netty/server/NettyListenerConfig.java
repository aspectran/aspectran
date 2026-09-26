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
package com.aspectran.netty.server;

import com.aspectran.utils.ResourceUtils;
import com.aspectran.utils.ToStringBuilder;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.KeyManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Configuration options for a Netty listener.
 * <p>Supports configuring host, port, socket options, SSL/TLS, and HTTP/2 settings.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public class NettyListenerConfig {

    private String name;

    private String host = "0.0.0.0";

    private int port = 8080;

    private int actualPort = -1;

    private boolean ssl;

    private boolean http2;

    private SslContext sslContext;

    private String keyStorePath;

    private String keyStorePassword;

    private String keyPassword;

    private String keyStoreType = "PKCS12";

    private int backlog = 1024;

    private boolean tcpNoDelay = true;

    private boolean keepAlive = true;

    private boolean reuseAddress = true;

    private int receiveBufferSize;

    private int sendBufferSize;

    private int maxInitialLineLength = 4096;

    private int maxHeaderSize = 8192;

    private int maxChunkSize = 8192;

    private Long http2HeaderTableSize;

    private Boolean http2EnablePush;

    private Long http2MaxConcurrentStreams;

    private Integer http2InitialWindowSize;

    private Integer http2MaxFrameSize;

    private Long http2MaxHeaderListSize;

    private long http2GracefulShutdownTimeoutMillis;

    /**
     * Constructs a new {@code NettyListenerConfig} with default host and port.
     */
    public NettyListenerConfig() {
    }

    /**
     * Constructs a new {@code NettyListenerConfig} for the specified port.
     * @param port the port number to bind
     */
    public NettyListenerConfig(int port) {
        this.port = port;
    }

    /**
     * Constructs a new {@code NettyListenerConfig} for the specified host and port.
     * @param host the host address or hostname
     * @param port the port number to bind
     */
    public NettyListenerConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getActualPort() {
        return (actualPort != -1 ? actualPort : port);
    }

    public void setActualPort(int actualPort) {
        this.actualPort = actualPort;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public boolean isHttp2() {
        return http2;
    }

    public void setHttp2(boolean http2) {
        this.http2 = http2;
    }

    public SslContext getSslContext() {
        return sslContext;
    }

    public void setSslContext(SslContext sslContext) {
        this.sslContext = sslContext;
        if (sslContext != null) {
            this.ssl = true;
        }
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isReuseAddress() {
        return reuseAddress;
    }

    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public int getMaxInitialLineLength() {
        return maxInitialLineLength;
    }

    public void setMaxInitialLineLength(int maxInitialLineLength) {
        this.maxInitialLineLength = maxInitialLineLength;
    }

    public int getMaxHeaderSize() {
        return maxHeaderSize;
    }

    public void setMaxHeaderSize(int maxHeaderSize) {
        this.maxHeaderSize = maxHeaderSize;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public void setMaxChunkSize(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
    }

    public Long getHttp2HeaderTableSize() {
        return http2HeaderTableSize;
    }

    public void setHttp2HeaderTableSize(long http2HeaderTableSize) {
        this.http2HeaderTableSize = http2HeaderTableSize;
    }

    public Boolean getHttp2EnablePush() {
        return http2EnablePush;
    }

    public void setHttp2EnablePush(boolean http2EnablePush) {
        this.http2EnablePush = http2EnablePush;
    }

    public Long getHttp2MaxConcurrentStreams() {
        return http2MaxConcurrentStreams;
    }

    public void setHttp2MaxConcurrentStreams(long http2MaxConcurrentStreams) {
        this.http2MaxConcurrentStreams = http2MaxConcurrentStreams;
    }

    public Integer getHttp2InitialWindowSize() {
        return http2InitialWindowSize;
    }

    public void setHttp2InitialWindowSize(int http2InitialWindowSize) {
        this.http2InitialWindowSize = http2InitialWindowSize;
    }

    public Integer getHttp2MaxFrameSize() {
        return http2MaxFrameSize;
    }

    public void setHttp2MaxFrameSize(int http2MaxFrameSize) {
        this.http2MaxFrameSize = http2MaxFrameSize;
    }

    public Long getHttp2MaxHeaderListSize() {
        return http2MaxHeaderListSize;
    }

    public void setHttp2MaxHeaderListSize(long http2MaxHeaderListSize) {
        this.http2MaxHeaderListSize = http2MaxHeaderListSize;
    }

    public long getHttp2GracefulShutdownTimeoutMillis() {
        return http2GracefulShutdownTimeoutMillis;
    }

    public void setHttp2GracefulShutdownTimeoutMillis(long http2GracefulShutdownTimeoutMillis) {
        this.http2GracefulShutdownTimeoutMillis = http2GracefulShutdownTimeoutMillis;
    }

    /**
     * Builds and returns an {@link Http2Settings} instance
     * containing all configured HTTP/2 settings, or {@code null} if none are configured.
     * @return the configured HTTP/2 settings, or {@code null}
     */
    public Http2Settings getHttp2Settings() {
        Http2Settings settings = null;
        if (http2HeaderTableSize != null) {
            settings = new Http2Settings();
            settings.headerTableSize(http2HeaderTableSize);
        }
        if (http2EnablePush != null) {
            if (settings == null) {
                settings = new Http2Settings();
            }
            settings.pushEnabled(http2EnablePush);
        }
        if (http2MaxConcurrentStreams != null) {
            if (settings == null) {
                settings = new Http2Settings();
            }
            settings.maxConcurrentStreams(http2MaxConcurrentStreams);
        }
        if (http2InitialWindowSize != null) {
            if (settings == null) {
                settings = new Http2Settings();
            }
            settings.initialWindowSize(http2InitialWindowSize);
        }
        if (http2MaxFrameSize != null) {
            if (settings == null) {
                settings = new Http2Settings();
            }
            settings.maxFrameSize(http2MaxFrameSize);
        }
        if (http2MaxHeaderListSize != null) {
            if (settings == null) {
                settings = new Http2Settings();
            }
            settings.maxHeaderListSize(http2MaxHeaderListSize);
        } else if (maxHeaderSize > 0) {
            if (settings == null) {
                settings = new Http2Settings();
            }
            settings.maxHeaderListSize(maxHeaderSize);
        }
        return settings;
    }

    /**
     * Creates and configures an {@link SslContext} based on the keyStore configuration.
     * @return a configured {@link SslContext}
     * @throws Exception if an error occurs while building the SSL context
     */
    public SslContext buildSslContext() throws Exception {
        if (sslContext != null) {
            return sslContext;
        }
        if (keyStorePath == null) {
            throw new IllegalStateException("keyStorePath must be specified for SSL listener");
        }
        KeyStore keyStore = KeyStore.getInstance(keyStoreType != null ? keyStoreType : KeyStore.getDefaultType());
        char[] storePass = (keyStorePassword != null ? keyStorePassword.toCharArray() : null);
        char[] keyPass = (keyPassword != null ? keyPassword.toCharArray() : storePass);

        try (InputStream in = ResourceUtils.getResourceAsStream(keyStorePath)) {
            keyStore.load(in, storePass);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyPass);

        SslContextBuilder builder = SslContextBuilder.forServer(kmf);
        if (http2) {
            ApplicationProtocolConfig apc = new ApplicationProtocolConfig(
                    ApplicationProtocolConfig.Protocol.ALPN,
                    ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                    ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                    ApplicationProtocolNames.HTTP_2,
                    ApplicationProtocolNames.HTTP_1_1);
            builder.applicationProtocolConfig(apc);
        }

        this.sslContext = builder.build();
        return this.sslContext;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder("NettyListenerConfig");
        tsb.append("name", name);
        tsb.append("host", host);
        tsb.append("port", getActualPort());
        tsb.append("ssl", ssl);
        tsb.append("http2", http2);
        return tsb.toString();
    }

}
