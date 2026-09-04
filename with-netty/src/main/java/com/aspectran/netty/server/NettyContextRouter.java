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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Routes incoming HTTP request paths to the appropriate {@link NettyContext}.
 * <p>Uses longest prefix matching so that more specific context paths (e.g. {@code /api/v1})
 * take precedence over broader paths (e.g. {@code /api} or {@code /}).</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public class NettyContextRouter {

    private final List<NettyContext> contexts = new CopyOnWriteArrayList<>();

    private volatile NettyContext rootContext;

    /**
     * Adds a {@link NettyContext} to the router and re-sorts contexts by prefix length descending.
     * @param context the context to add
     */
    public void addContext(@NonNull NettyContext context) {
        contexts.add(context);
        resort();
    }

    /**
     * Sets the array of {@link NettyContext} instances to route.
     * @param contexts the contexts to route
     */
    public void setContexts(NettyContext... contexts) {
        this.contexts.clear();
        if (contexts != null) {
            Collections.addAll(this.contexts, contexts);
        }
        resort();
    }

    /**
     * Sets the list of {@link NettyContext} instances to route.
     * @param contexts the list of contexts to route
     */
    public void setContexts(List<NettyContext> contexts) {
        this.contexts.clear();
        if (contexts != null) {
            this.contexts.addAll(contexts);
        }
        resort();
    }

    /**
     * Removes a {@link NettyContext} from the router.
     * @param context the context to remove
     * @return true if removed; false otherwise
     */
    public boolean removeContext(NettyContext context) {
        boolean removed = contexts.remove(context);
        if (removed) {
            resort();
        }
        return removed;
    }

    /**
     * Returns an unmodifiable list of registered contexts, sorted in routing precedence order.
     * @return the list of contexts
     */
    public List<NettyContext> getContexts() {
        return Collections.unmodifiableList(contexts);
    }

    /**
     * Returns whether any contexts are registered.
     * @return true if no contexts are registered; false otherwise
     */
    public boolean isEmpty() {
        return contexts.isEmpty();
    }

    /**
     * Returns the number of registered contexts.
     * @return the context count
     */
    public int size() {
        return contexts.size();
    }

    /**
     * Matches the given request URI path to the most specific {@link NettyContext}.
     * @param path the request URI path
     * @return the matched context, or the root context if no specific prefix matches
     */
    @Nullable
    public NettyContext match(@NonNull String path) {
        for (NettyContext context : contexts) {
            String cp = context.getContextPath();
            if (cp.isEmpty()) {
                continue;
            }
            if (path.equals(cp) || path.startsWith(cp + "/") || path.startsWith(cp + "?")) {
                return context;
            }
        }
        return rootContext;
    }

    /**
     * Returns the root context (context path {@code ""} or {@code "/"}), or {@code null} if none is registered.
     * @return the root context
     */
    public NettyContext getRootContext() {
        return rootContext;
    }

    /**
     * Returns the {@link NettyContext} registered with the specified context path.
     * @param contextPath the context path
     * @return the matched context, or {@code null} if not found
     */
    @Nullable
    public NettyContext getContext(String contextPath) {
        if (contextPath == null || contextPath.isEmpty() || "/".equals(contextPath)) {
            return (rootContext != null ? rootContext : (contexts.size() == 1 ? contexts.getFirst() : null));
        }
        String normalized = (contextPath.startsWith("/") ? contextPath : "/" + contextPath);
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        for (NettyContext context : contexts) {
            if (context.getContextPath().equals(normalized)) {
                return context;
            }
        }
        return null;
    }

    private void resort() {
        NettyContext foundRoot = null;
        for (NettyContext context : contexts) {
            if (context.getContextPath().isEmpty()) {
                foundRoot = context;
                break;
            }
        }
        this.rootContext = foundRoot;
        contexts.sort((c1, c2) -> Integer.compare(c2.getContextPath().length(), c1.getContextPath().length()));
    }

}
