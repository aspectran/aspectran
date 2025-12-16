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

import com.aspectran.utils.ResourceUtils;
import io.undertow.Undertow;
import org.jspecify.annotations.NonNull;

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
 * A bean-style configuration class for an Undertow HTTPS listener.
 * <p>This class holds the host, port, and all necessary SSL/TLS settings,
 * such as keystore and truststore information, to configure a secure connector.
 * It simplifies the process of setting up SSL by allowing these properties to be
 * defined declaratively in Aspectran's configuration.</p>
 *
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

    /**
     * Returns the port number for the listener.
     * @return the port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port number for the listener.
     * @param port the port number
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns the host name or IP address for the listener.
     * @return the host name
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host name or IP address for the listener.
     * @param host the host name
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the alias of the key to use from the keystore.
     * @return the key alias
     */
    public String getKeyAlias() {
        return keyAlias;
    }

    /**
     * Sets the alias of the key to use from the keystore.
     * @param keyAlias the key alias
     */
    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    /**
     * Returns the type of the keystore (e.g., "JKS", "PKCS12").
     * @return the keystore type
     */
    public String getKeyStoreType() {
        return keyStoreType;
    }

    /**
     * Sets the type of the keystore.
     * @param keyStoreType the keystore type
     */
    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    /**
     * Returns the provider for the keystore.
     * @return the keystore provider
     */
    public String getKeyStoreProvider() {
        return keyStoreProvider;
    }

    /**
     * Sets the provider for the keystore.
     * @param keyStoreProvider the keystore provider
     */
    public void setKeyStoreProvider(String keyStoreProvider) {
        this.keyStoreProvider = keyStoreProvider;
    }

    /**
     * Returns the path to the keystore file.
     * @return the keystore path
     */
    public String getKeyStorePath() {
        return keyStorePath;
    }

    /**
     * Sets the path to the keystore file.
     * @param keyStorePath the keystore path
     */
    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    /**
     * Returns the password for the keystore.
     * @return the keystore password
     */
    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    /**
     * Sets the password for the keystore.
     * @param keyStorePassword the keystore password
     */
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * Returns the type of the truststore.
     * @return the truststore type
     */
    public String getTrustStoreType() {
        return trustStoreType;
    }

    /**
     * Sets the type of the truststore.
     * @param trustStoreType the truststore type
     */
    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    /**
     * Returns the provider for the truststore.
     * @return the truststore provider
     */
    public String getTrustStoreProvider() {
        return trustStoreProvider;
    }

    /**
     * Sets the provider for the truststore.
     * @param trustStoreProvider the truststore provider
     */
    public void setTrustStoreProvider(String trustStoreProvider) {
        this.trustStoreProvider = trustStoreProvider;
    }

    /**
     * Returns the path to the truststore file.
     * @return the truststore path
     */
    public String getTrustStorePath() {
        return trustStorePath;
    }

    /**
     * Sets the path to the truststore file.
     * @param trustStorePath the truststore path
     */
    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    /**
     * Returns the password for the truststore.
     * @return the truststore password
     */
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    /**
     * Sets the password for the truststore.
     * @param trustStorePassword the truststore password
     */
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    /**
     * Creates and returns an Undertow {@link Undertow.ListenerBuilder} based on this configuration.
     * @return a configured listener builder for an HTTPS connector
     * @throws IOException if the SSL context cannot be initialized
     */
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

    /**
     * Creates and initializes the {@link KeyManager}s for the SSL context.
     * @return an array of key managers
     * @throws IOException if the keystore cannot be loaded or initialized
     */
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

    /**
     * Wraps the default key managers to force the use of a specific alias.
     * @param keyAlias the alias to use
     * @param keyManagers the original key managers
     * @return the wrapped key managers
     */
    private KeyManager[] getAliasedX509ExtendedKeyManager(String keyAlias, @NonNull KeyManager[] keyManagers) {
        for (int i = 0; i < keyManagers.length; i++) {
            if (keyManagers[i] instanceof X509ExtendedKeyManager) {
                keyManagers[i] = new AliasedX509ExtendedKeyManager((X509ExtendedKeyManager)keyManagers[i], keyAlias);
            }
        }
        return keyManagers;
    }

    /**
     * Creates and initializes the {@link TrustManager}s for the SSL context.
     * @return an array of trust managers
     * @throws IOException if the truststore cannot be loaded or initialized
     */
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

    @NonNull
    private KeyStore loadKeyStore(String type, String provider, String resource, String password) throws Exception {
        return loadStore(type, provider, resource, password);
    }

    private KeyStore loadTrustStore(String type, String provider, String resource, String password) throws Exception {
        if (resource == null) {
            return null;
        }
        return loadStore(type, provider, resource, password);
    }

    @NonNull
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
     * An X509ExtendedKeyManager that selects a key with a desired alias,
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
