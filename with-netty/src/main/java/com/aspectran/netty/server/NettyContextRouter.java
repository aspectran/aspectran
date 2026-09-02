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

    public void addContext(@NonNull NettyContext context) {
        contexts.add(context);
        resort();
    }

    public void setContexts(NettyContext... contexts) {
        this.contexts.clear();
        if (contexts != null) {
            Collections.addAll(this.contexts, contexts);
        }
        resort();
    }

    public void setContexts(List<NettyContext> contexts) {
        this.contexts.clear();
        if (contexts != null) {
            this.contexts.addAll(contexts);
        }
        resort();
    }

    public boolean removeContext(NettyContext context) {
        boolean removed = contexts.remove(context);
        if (removed) {
            resort();
        }
        return removed;
    }

    public List<NettyContext> getContexts() {
        return Collections.unmodifiableList(contexts);
    }

    public boolean isEmpty() {
        return contexts.isEmpty();
    }

    public int size() {
        return contexts.size();
    }

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

    public NettyContext getRootContext() {
        return rootContext;
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
