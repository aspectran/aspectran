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

import com.aspectran.netty.server.websocket.NettyWebSocketListener;
import com.aspectran.netty.server.websocket.NettyWebSocketSession;
import com.aspectran.utils.Assert;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Decoder;
import jakarta.websocket.Encoder;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter that bridges Netty's {@link NettyWebSocketListener} to standard
 * JSR-356 {@link Endpoint} classes or {@link ServerEndpoint}-annotated POJO endpoints.
 *
 * <p>Created: 2026-09-02</p>
 */
public class JsrWebSocketEndpointAdapter implements NettyWebSocketListener {

    private static final Logger logger = LoggerFactory.getLogger(JsrWebSocketEndpointAdapter.class);

    private static final String JSR_SESSION_ATTR = JsrWebSocketSession.class.getName();

    private final Object endpointInstance;

    private final Class<?> endpointClass;

    private final ServerEndpointConfig endpointConfig;

    private final Set<Session> openSessions = ConcurrentHashMap.newKeySet();

    private final List<Encoder> encoders = new ArrayList<>();

    private final List<Decoder> decoders = new ArrayList<>();

    private Method onOpenMethod;

    private Method onCloseMethod;

    private Method onErrorMethod;

    private Method onMessageTextMethod;

    private Method onMessageBinaryMethod;

    public JsrWebSocketEndpointAdapter(
            @NonNull Object endpointInstance,
            @Nullable ServerEndpointConfig endpointConfig) {
        Assert.notNull(endpointInstance, "endpointInstance must not be null");
        this.endpointInstance = endpointInstance;
        this.endpointClass = endpointInstance.getClass();
        this.endpointConfig = endpointConfig;
        initEncodersAndDecoders();
        findAnnotatedMethods();
    }

    public Object getEndpointInstance() {
        return endpointInstance;
    }

    public Class<?> getEndpointClass() {
        return endpointClass;
    }

    public Set<Session> getOpenSessions() {
        return Collections.unmodifiableSet(openSessions);
    }

    @Override
    public void onOpen(@NonNull NettyWebSocketSession session) throws Exception {
        JsrWebSocketSession jsrSession = new JsrWebSocketSession(session, openSessions, encoders, decoders);
        openSessions.add(jsrSession);
        session.setAttribute(JSR_SESSION_ATTR, jsrSession);

        if (endpointInstance instanceof Endpoint endpoint) {
            endpoint.onOpen(jsrSession, endpointConfig);
        } else if (onOpenMethod != null) {
            invokeOnOpen(jsrSession);
        }
    }

    @Override
    public void onMessage(@NonNull NettyWebSocketSession session, String text) throws Exception {
        JsrWebSocketSession jsrSession = session.getAttribute(JSR_SESSION_ATTR);
        if (jsrSession != null) {
            if (jsrSession.hasMessageHandlers()) {
                jsrSession.handleTextMessage(text);
            } else if (onMessageTextMethod != null) {
                invokeOnMessageText(jsrSession, text);
            }
        }
    }

    @Override
    public void onMessage(@NonNull NettyWebSocketSession session, byte[] data) throws Exception {
        JsrWebSocketSession jsrSession = session.getAttribute(JSR_SESSION_ATTR);
        if (jsrSession != null) {
            if (jsrSession.hasMessageHandlers()) {
                jsrSession.handleBinaryMessage(data);
            } else if (onMessageBinaryMethod != null) {
                invokeOnMessageBinary(jsrSession, data);
            }
        }
    }

    @Override
    public void onClose(@NonNull NettyWebSocketSession session, int statusCode, @Nullable String reason) throws Exception {
        JsrWebSocketSession jsrSession = session.getAttribute(JSR_SESSION_ATTR);
        if (jsrSession != null) {
            openSessions.remove(jsrSession);
            CloseReason.CloseCode code = CloseReason.CloseCodes.getCloseCode(statusCode);
            CloseReason closeReason = new CloseReason(code != null ? code : CloseReason.CloseCodes.NORMAL_CLOSURE, reason);

            if (endpointInstance instanceof Endpoint endpoint) {
                endpoint.onClose(jsrSession, closeReason);
            } else if (onCloseMethod != null) {
                invokeOnClose(jsrSession, closeReason);
            }
        }
    }

    @Override
    public void onError(@NonNull NettyWebSocketSession session, @NonNull Throwable cause) {
        JsrWebSocketSession jsrSession = session.getAttribute(JSR_SESSION_ATTR);
        if (jsrSession != null) {
            if (endpointInstance instanceof Endpoint endpoint) {
                endpoint.onError(jsrSession, cause);
            } else if (onErrorMethod != null) {
                invokeOnError(jsrSession, cause);
            } else {
                logger.warn("Unhandled WebSocket error in session: {}", session.getId(), cause);
            }
        }
    }

    private void invokeOnOpen(JsrWebSocketSession session) {
        try {
            Class<?>[] paramTypes = onOpenMethod.getParameterTypes();
            Annotation[][] paramAnnos = onOpenMethod.getParameterAnnotations();
            Object[] args = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                if (paramTypes[i].isAssignableFrom(Session.class)) {
                    args[i] = session;
                } else if (paramTypes[i].isAssignableFrom(EndpointConfig.class)) {
                    args[i] = endpointConfig;
                } else {
                    args[i] = resolvePathParam(paramAnnos[i], paramTypes[i], session);
                }
            }
            onOpenMethod.invoke(endpointInstance, args);
        } catch (Exception e) {
            logger.error("Failed to invoke @OnOpen on {}", endpointClass.getName(), unwrapInvocationTarget(e));
        }
    }

    private void invokeOnClose(JsrWebSocketSession session, CloseReason closeReason) {
        try {
            Class<?>[] paramTypes = onCloseMethod.getParameterTypes();
            Annotation[][] paramAnnos = onCloseMethod.getParameterAnnotations();
            Object[] args = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                if (paramTypes[i].isAssignableFrom(Session.class)) {
                    args[i] = session;
                } else if (paramTypes[i].isAssignableFrom(CloseReason.class)) {
                    args[i] = closeReason;
                } else {
                    args[i] = resolvePathParam(paramAnnos[i], paramTypes[i], session);
                }
            }
            onCloseMethod.invoke(endpointInstance, args);
        } catch (Exception e) {
            logger.error("Failed to invoke @OnClose on {}", endpointClass.getName(), unwrapInvocationTarget(e));
        }
    }

    private void invokeOnError(JsrWebSocketSession session, Throwable cause) {
        try {
            Class<?>[] paramTypes = onErrorMethod.getParameterTypes();
            Annotation[][] paramAnnos = onErrorMethod.getParameterAnnotations();
            Object[] args = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                if (paramTypes[i].isAssignableFrom(Session.class)) {
                    args[i] = session;
                } else if (Throwable.class.isAssignableFrom(paramTypes[i])) {
                    args[i] = cause;
                } else {
                    args[i] = resolvePathParam(paramAnnos[i], paramTypes[i], session);
                }
            }
            onErrorMethod.invoke(endpointInstance, args);
        } catch (Exception e) {
            logger.error("Failed to invoke @OnError on {}", endpointClass.getName(), unwrapInvocationTarget(e));
        }
    }

    private void invokeOnMessageText(JsrWebSocketSession session, String text) {
        try {
            Class<?>[] paramTypes = onMessageTextMethod.getParameterTypes();
            Annotation[][] paramAnnos = onMessageTextMethod.getParameterAnnotations();
            Object[] args = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                if (paramTypes[i].isAssignableFrom(Session.class)) {
                    args[i] = session;
                } else if (paramTypes[i].isAssignableFrom(String.class)) {
                    Object pathVal = resolvePathParam(paramAnnos[i], paramTypes[i], session);
                    args[i] = (pathVal != null ? pathVal : text);
                } else {
                    args[i] = resolvePathParam(paramAnnos[i], paramTypes[i], session);
                }
            }
            Object result = onMessageTextMethod.invoke(endpointInstance, args);
            if (result != null) {
                session.getAsyncRemote().sendObject(result);
            }
        } catch (Exception e) {
            logger.error("Failed to invoke @OnMessage on {}", endpointClass.getName(), unwrapInvocationTarget(e));
        }
    }

    private void invokeOnMessageBinary(JsrWebSocketSession session, byte[] data) {
        try {
            Class<?>[] paramTypes = onMessageBinaryMethod.getParameterTypes();
            Annotation[][] paramAnnos = onMessageBinaryMethod.getParameterAnnotations();
            Object[] args = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                if (paramTypes[i].isAssignableFrom(Session.class)) {
                    args[i] = session;
                } else if (paramTypes[i] == byte[].class) {
                    args[i] = data;
                } else if (paramTypes[i].isAssignableFrom(ByteBuffer.class)) {
                    args[i] = ByteBuffer.wrap(data);
                } else {
                    args[i] = resolvePathParam(paramAnnos[i], paramTypes[i], session);
                }
            }
            Object result = onMessageBinaryMethod.invoke(endpointInstance, args);
            if (result != null) {
                session.getAsyncRemote().sendObject(result);
            }
        } catch (Exception e) {
            logger.error("Failed to invoke @OnMessage(binary) on {}", endpointClass.getName(), unwrapInvocationTarget(e));
        }
    }

    @Nullable
    private Object resolvePathParam(Annotation @NonNull [] annotations, Class<?> paramType, JsrWebSocketSession session) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof PathParam pathParam) {
                String raw = session.getPathParameters().get(pathParam.value());
                if (raw == null) {
                    return null;
                }
                if (paramType == String.class) {
                    return raw;
                } else if (paramType == int.class || paramType == Integer.class) {
                    return Integer.parseInt(raw);
                } else if (paramType == long.class || paramType == Long.class) {
                    return Long.parseLong(raw);
                } else if (paramType == boolean.class || paramType == Boolean.class) {
                    return Boolean.parseBoolean(raw);
                } else if (paramType == double.class || paramType == Double.class) {
                    return Double.parseDouble(raw);
                } else if (paramType == float.class || paramType == Float.class) {
                    return Float.parseFloat(raw);
                } else if (paramType == short.class || paramType == Short.class) {
                    return Short.parseShort(raw);
                } else if (paramType == byte.class || paramType == Byte.class) {
                    return Byte.parseByte(raw);
                }
                return raw;
            }
        }
        return null;
    }

    private void initEncodersAndDecoders() {
        ServerEndpoint annotation = endpointClass.getAnnotation(ServerEndpoint.class);
        if (annotation != null) {
            for (Class<? extends Encoder> encClass : annotation.encoders()) {
                try {
                    Encoder encoder = encClass.getDeclaredConstructor().newInstance();
                    if (endpointConfig != null) {
                        encoder.init(endpointConfig);
                    }
                    encoders.add(encoder);
                } catch (Exception e) {
                    logger.warn("Failed to instantiate encoder: {}", encClass.getName(), e);
                }
            }
            for (Class<? extends Decoder> decClass : annotation.decoders()) {
                try {
                    Decoder decoder = decClass.getDeclaredConstructor().newInstance();
                    if (endpointConfig != null) {
                        decoder.init(endpointConfig);
                    }
                    decoders.add(decoder);
                } catch (Exception e) {
                    logger.warn("Failed to instantiate decoder: {}", decClass.getName(), e);
                }
            }
        }
    }

    private void findAnnotatedMethods() {
        Class<?> current = endpointClass;
        while (current != null && current != Object.class && current != Endpoint.class) {
            for (Method method : current.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                if (method.isAnnotationPresent(OnOpen.class) && onOpenMethod == null) {
                    onOpenMethod = method;
                } else if (method.isAnnotationPresent(OnClose.class) && onCloseMethod == null) {
                    onCloseMethod = method;
                } else if (method.isAnnotationPresent(OnError.class) && onErrorMethod == null) {
                    onErrorMethod = method;
                } else if (method.isAnnotationPresent(OnMessage.class)) {
                    Class<?>[] types = method.getParameterTypes();
                    for (Class<?> type : types) {
                        if (type == String.class) {
                            if (onMessageTextMethod == null) {
                                onMessageTextMethod = method;
                            }
                        } else if (type == byte[].class || type == ByteBuffer.class) {
                            if (onMessageBinaryMethod == null) {
                                onMessageBinaryMethod = method;
                            }
                        }
                    }
                }
            }
            current = current.getSuperclass();
        }
    }

    private static Throwable unwrapInvocationTarget(Throwable t) {
        if (t instanceof InvocationTargetException ite && ite.getTargetException() != null) {
            return ite.getTargetException();
        }
        return t;
    }

}
