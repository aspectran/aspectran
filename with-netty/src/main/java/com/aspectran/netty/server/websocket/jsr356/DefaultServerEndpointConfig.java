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
package com.aspectran.netty.server.websocket.jsr356;

import com.aspectran.utils.Assert;
import jakarta.websocket.Decoder;
import jakarta.websocket.Encoder;
import jakarta.websocket.Extension;
import jakarta.websocket.server.ServerEndpointConfig;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default standalone implementation of {@link ServerEndpointConfig} for Netty,
 * avoiding platform-specific SPI lookup errors.
 *
 * <p>Created: 2026-09-02</p>
 */
public class DefaultServerEndpointConfig implements ServerEndpointConfig {

    private final Class<?> endpointClass;

    private final String path;

    private final List<Class<? extends Encoder>> encoders;

    private final List<Class<? extends Decoder>> decoders;

    private final List<String> subprotocols;

    private final List<Extension> extensions;

    private final Configurator configurator;

    private final Map<String, Object> userProperties = new ConcurrentHashMap<>();

    public DefaultServerEndpointConfig(@NonNull Class<?> endpointClass, @NonNull String path) {
        this(endpointClass, path, null, null, null, null, null);
    }

    public DefaultServerEndpointConfig(
            @NonNull Class<?> endpointClass,
            @NonNull String path,
            @Nullable List<Class<? extends Encoder>> encoders,
            @Nullable List<Class<? extends Decoder>> decoders,
            @Nullable List<String> subprotocols,
            @Nullable List<Extension> extensions,
            @Nullable Configurator configurator) {
        Assert.notNull(endpointClass, "endpointClass must not be null");
        Assert.notNull(path, "path must not be null");
        this.endpointClass = endpointClass;
        this.path = path;
        this.encoders = (encoders != null ? encoders : Collections.emptyList());
        this.decoders = (decoders != null ? decoders : Collections.emptyList());
        this.subprotocols = (subprotocols != null ? subprotocols : Collections.emptyList());
        this.extensions = (extensions != null ? extensions : Collections.emptyList());
        this.configurator = (configurator != null ? configurator : new Configurator() {});
    }

    @Override
    public Class<?> getEndpointClass() {
        return endpointClass;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public List<String> getSubprotocols() {
        return subprotocols;
    }

    @Override
    public List<Extension> getExtensions() {
        return extensions;
    }

    @Override
    public Configurator getConfigurator() {
        return configurator;
    }

    @Override
    public List<Class<? extends Encoder>> getEncoders() {
        return encoders;
    }

    @Override
    public List<Class<? extends Decoder>> getDecoders() {
        return decoders;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

}
