/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.core.service;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.resource.SiblingClassLoader;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Created: 01/10/2019</p>
 */
public class CoreServiceHolder {

    private static final Map<ClassLoader, CoreService> servicePerThread = new ConcurrentHashMap<>();

    private static final Map<Class<?>, CoreService> servicePerClass = new ConcurrentHashMap<>();

    private static volatile CoreService currentService;

    public static void hold(CoreService service) {
        Assert.notNull(service, "service must not be null");
        Assert.state(service.getActivityContext() != null, "No ActivityContext");
        ClassLoader classLoader = service.getServiceClassLoader();
        if (classLoader != null) {
            if (classLoader == CoreServiceHolder.class.getClassLoader()) {
                currentService = service;
            } else {
                servicePerThread.put(classLoader, service);
            }
            if (service.getAltClassLoader() != null) {
                hold(service, service.getAltClassLoader());
            }
        }
    }

    public static void hold(CoreService service, ClassLoader classLoader) {
        Assert.notNull(service, "service must not be null");
        Assert.notNull(classLoader, "classLoader must not be null");
        Assert.state(currentService == service || servicePerThread.containsValue(service),
            "Unregistered service: " + service);
        servicePerThread.put(classLoader, service);
    }

    public static void hold(CoreService service, Class<?> clazz) {
        Assert.notNull(service, "service must not be null");
        Assert.notNull(clazz, "clazz must not be null");
        Assert.state(currentService == service || servicePerThread.containsValue(service),
            "Unregistered service: " + service);
        servicePerClass.put(clazz, service);
    }

    public static void release(CoreService service) {
        Assert.notNull(service, "service must not be null");
        servicePerThread.entrySet().removeIf(entry -> (service.equals(entry.getValue())));
        servicePerClass.entrySet().removeIf(entry -> (service.equals(entry.getValue())));
        if (currentService != null && currentService == service) {
            currentService = null;
        }
    }

    public static CoreService acquire() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            CoreService service = servicePerThread.get(classLoader);
            if (service == null && !(classLoader instanceof SiblingClassLoader)) {
                service = servicePerThread.get(classLoader.getParent());
            }
            if (service != null) {
                return service;
            }
        }
        return currentService;
    }

    public static CoreService acquire(Class<?> clazz) {
        CoreService service = servicePerClass.get(clazz);
        if (service == null) {
            service = acquire();
        }
        return service;
    }

    @Nullable
    public static ActivityContext getActivityContext() {
        CoreService service = acquire();
        return (service != null ? service.getActivityContext() : null);
    }

    @Nullable
    public static ActivityContext getActivityContext(Class<?> clazz) {
        CoreService service = acquire(clazz);
        return (service != null ? service.getActivityContext() : null);
    }

}
