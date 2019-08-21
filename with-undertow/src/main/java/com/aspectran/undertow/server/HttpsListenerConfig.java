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

import com.aspectran.core.util.ResourceUtils;
import io.undertow.Undertow;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;

/**
 * <p>Created: 2019-08-21</p>
 */
public class HttpsListenerConfig {

    private int port;

    private String host;

    private String keyStoreType;

    private String keyStoreProvider;

    private String keyStoreName;

    private String keyStorePassword;

    private String trustStoreType;

    private String trustStoreProvider;

    private String trustStoreName;

    private String trustStorePassword;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getKeyStoreProvider() {
        return keyStoreProvider;
    }

    public void setKeyStoreProvider(String keyStoreProvider) {
        this.keyStoreProvider = keyStoreProvider;
    }

    public String getKeyStoreName() {
        return keyStoreName;
    }

    public void setKeyStoreName(String keyStoreName) {
        this.keyStoreName = keyStoreName;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    public String getTrustStoreProvider() {
        return trustStoreProvider;
    }

    public void setTrustStoreProvider(String trustStoreProvider) {
        this.trustStoreProvider = trustStoreProvider;
    }

    public String getTrustStoreName() {
        return trustStoreName;
    }

    public void setTrustStoreName(String trustStoreName) {
        this.trustStoreName = trustStoreName;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    Undertow.ListenerBuilder getListenerBuilder() throws IOException {
        KeyManager[] keyManagers = getKeyManagers();
        TrustManager[] trustManagers = getTrustManagers();

        Undertow.ListenerBuilder listenerBuilder = new Undertow.ListenerBuilder();
        listenerBuilder.setType(Undertow.ListenerType.HTTPS);
        listenerBuilder.setPort(port);
        listenerBuilder.setHost(host);
        listenerBuilder.setKeyManagers(keyManagers);
        listenerBuilder.setTrustManagers(trustManagers);
        return listenerBuilder;
    }

    private KeyManager[] getKeyManagers() throws IOException {
        try {
            KeyStore keyStore = loadKeyStore(keyStoreType, keyStoreProvider, keyStoreName, keyStorePassword);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
            return keyManagerFactory.getKeyManagers();
        } catch (Exception e) {
            throw new IOException("Unable to initialise KeyManager[]", e);
        }
    }

    private TrustManager[] getTrustManagers() throws IOException {
        try {
            KeyStore store = loadTrustStore(trustStoreType, trustStoreProvider, trustStoreName, trustStorePassword);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(store);
            return trustManagerFactory.getTrustManagers();
        } catch (Exception e) {
            throw new IOException("Unable to initialise TrustManager[]", e);
        }
    }

    private KeyStore loadKeyStore(String type, String provider, String resource, String password) throws Exception {
        return loadStore(type, provider, resource, password);
    }

    private KeyStore loadTrustStore(String type, String provider, String resource, String password) throws Exception {
        if (resource == null) {
            return null;
        }
        return loadStore(type, provider, resource, password);
    }

    private KeyStore loadStore(String type, String provider, String resource, String password) throws Exception {
        type = (type != null ? type : "JKS");
        KeyStore store = (provider != null) ? KeyStore.getInstance(type, provider) : KeyStore.getInstance(type);
        try {
            URL url = ResourceUtils.getURL(resource);
            store.load(url.openStream(), (password != null ? password.toCharArray() : null));
            return store;
        } catch (Exception e) {
            throw new IOException("Could not load key store '" + resource + "'", e);
        }
    }

}
