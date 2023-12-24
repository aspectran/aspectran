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

import com.aspectran.utils.ResourceUtils;
import io.undertow.Undertow;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * <p>Created: 2019-08-21</p>
 */
public class HttpsListenerConfig {

    private int port;

    private String host;

    private String keyAlias;

    private String keyStoreType;

    private String keyStoreProvider;

    private String keyStorePath;

    private String keyStorePassword;

    private String trustStoreType;

    private String trustStoreProvider;

    private String trustStorePath;

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

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
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

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
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
            KeyStore keyStore = loadKeyStore(keyStoreType, keyStoreProvider, keyStorePath, keyStorePassword);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
            if (keyAlias != null) {
                return getAliasedX509ExtendedKeyManager(keyAlias, keyManagerFactory.getKeyManagers());
            } else {
                return keyManagerFactory.getKeyManagers();
            }
        } catch (Exception e) {
            throw new IOException("Unable to initialise KeyManager[]", e);
        }
    }

    private KeyManager[] getAliasedX509ExtendedKeyManager(String keyAlias, KeyManager[] keyManagers) {
        for (int i = 0; i < keyManagers.length; i++) {
            if (keyManagers[i] instanceof X509ExtendedKeyManager) {
                keyManagers[i] = new AliasedX509ExtendedKeyManager((X509ExtendedKeyManager)keyManagers[i], keyAlias);
            }
        }
        return keyManagers;
    }

    private TrustManager[] getTrustManagers() throws IOException {
        try {
            KeyStore store = loadTrustStore(trustStoreType, trustStoreProvider, trustStorePath, trustStorePassword);
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
        KeyStore store = (provider != null ? KeyStore.getInstance(type, provider) : KeyStore.getInstance(type));
        try {
            URL url = ResourceUtils.getURL(resource);
            store.load(url.openStream(), (password != null ? password.toCharArray() : null));
            return store;
        } catch (Exception e) {
            throw new IOException("Could not load key store '" + resource + "'", e);
        }
    }

    /**
     * An X509ExtendedKeyManager that select a key with desired alias,
     * delegating other processing to a nested X509ExtendedKeyManager.
     */
    private static class AliasedX509ExtendedKeyManager extends X509ExtendedKeyManager {

        private final X509ExtendedKeyManager keyManager;

        private final String alias;

        AliasedX509ExtendedKeyManager(X509ExtendedKeyManager keyManager, String alias) {
            this.keyManager = keyManager;
            this.alias = alias;
        }

        @Override
        public String chooseEngineClientAlias(String[] keyTypes, Principal[] principals, SSLEngine sslEngine) {
            return this.keyManager.chooseEngineClientAlias(keyTypes, principals, sslEngine);
        }

        @Override
        public String chooseEngineServerAlias(String keyType, Principal[] principals, SSLEngine sslEngine) {
            if (this.alias == null) {
                return this.keyManager.chooseEngineServerAlias(keyType, principals, sslEngine);
            }
            return this.alias;
        }

        @Override
        public String chooseClientAlias(String[] keyTypes, Principal[] issuers, Socket socket) {
            return this.keyManager.chooseClientAlias(keyTypes, issuers, socket);
        }

        @Override
        public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
            return this.keyManager.chooseServerAlias(keyType, issuers, socket);
        }

        @Override
        public X509Certificate[] getCertificateChain(String alias) {
            return this.keyManager.getCertificateChain(alias);
        }

        @Override
        public String[] getClientAliases(String keyType, Principal[] issuers) {
            return this.keyManager.getClientAliases(keyType, issuers);
        }

        @Override
        public PrivateKey getPrivateKey(String alias) {
            return this.keyManager.getPrivateKey(alias);
        }

        @Override
        public String[] getServerAliases(String keyType, Principal[] issuers) {
            return this.keyManager.getServerAliases(keyType, issuers);
        }

    }

}
