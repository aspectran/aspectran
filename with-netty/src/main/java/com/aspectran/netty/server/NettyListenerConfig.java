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
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.KeyManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Configuration options for a Netty listener.
 * <p>Supports configuring host, port, socket options, and SSL/TLS settings.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public class NettyListenerConfig {

    private String name;

    private String host = "0.0.0.0";

    private int port = 8080;

    private int actualPort = -1;

    private boolean ssl;

    private SslContext sslContext;

    private String keyStorePath;

    private String keyStorePassword;

    private String keyPassword;

    private String keyStoreType = "PKCS12";

    private int backlog = 1024;

    private boolean tcpNoDelay = true;

    private boolean keepAlive = true;

    private boolean reuseAddress = true;

    public NettyListenerConfig() {
    }

    public NettyListenerConfig(int port) {
        this.port = port;
    }

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

        this.sslContext = SslContextBuilder.forServer(kmf).build();
        return this.sslContext;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder("NettyListenerConfig");
        tsb.append("name", name);
        tsb.append("host", host);
        tsb.append("port", getActualPort());
        tsb.append("ssl", ssl);
        return tsb.toString();
    }

}
