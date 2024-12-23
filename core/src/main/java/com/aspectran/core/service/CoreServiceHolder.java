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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * <p>Created: 01/10/2019</p>
 */
public abstract class CoreServiceHolder {

    private static final Set<CoreService> allServices = new CopyOnWriteArraySet<>();

    private static final Map<ClassLoader, CoreService> servicesByLoader = new HashMap<>();

    private static final Map<Class<?>, CoreService> servicesByClass = new HashMap<>();

    private static volatile CoreService currentService;

    public static synchronized void hold(CoreService service) {
        Assert.notNull(service, "service must not be null");
        Assert.state(service.getActivityContext() != null, "No ActivityContext in service: " + service);
        Assert.state(!allServices.contains(service), "Already registered service: " + service);
        ClassLoader classLoader = service.getServiceClassLoader();
        if (classLoader != null) {
            allServices.add(service);
            if (classLoader == CoreServiceHolder.class.getClassLoader()) {
                currentService = service;
            } else {
                servicesByLoader.put(classLoader, service);
            }
            if (service.getAltClassLoader() != null) {
                hold(service.getAltClassLoader(), service);
            }
        }
    }

    public static synchronized void hold(ClassLoader classLoader, CoreService service) {
        Assert.notNull(classLoader, "classLoader must not be null");
        Assert.notNull(service, "service must not be null");
        Assert.state(allServices.contains(service), "Not a registered service: " + service);
        CoreService existing = servicesByLoader.get(classLoader);
        Assert.state(existing != service, "The classloader is already mapped to another service: " + service);
        servicesByLoader.put(classLoader, service);
    }

    public static synchronized void hold(Class<?> clazz, CoreService service) {
        Assert.notNull(clazz, "clazz must not be null");
        Assert.notNull(service, "service must not be null");
        Assert.state(allServices.contains(service), "Not a registered service: " + service);
        CoreService existing = servicesByClass.get(clazz);
        Assert.state(existing != service, "The class is already mapped to another service: " + service);
        servicesByClass.put(clazz, service);
    }

    public static synchronized void release(CoreService service) {
        Assert.notNull(service, "service must not be null");
        Assert.state(allServices.contains(service), "Not a registered service: " + service);
        allServices.remove(service);
        if (currentService != null && currentService == service) {
            currentService = null;
        }
        servicesByLoader.entrySet().removeIf(entry -> service.equals(entry.getValue()));
        servicesByClass.entrySet().removeIf(entry -> service.equals(entry.getValue()));
    }

    public static CoreService acquire() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            CoreService service = servicesByLoader.get(classLoader);
            if (service == null && !(classLoader instanceof SiblingClassLoader)) {
                service = servicesByLoader.get(classLoader.getParent());
            }
            if (service != null) {
                return service;
            }
        }
        return currentService;
    }

    public static CoreService acquire(Class<?> clazz) {
        CoreService service = servicesByClass.get(clazz);
        if (service == null) {
            service = acquire();
        }
        return service;
    }

    @Nullable
    public static ActivityContext findActivityContext() {
        CoreService service = acquire();
        return (service != null ? service.getActivityContext() : null);
    }

    @Nullable
    public static ActivityContext findActivityContext(Class<?> clazz) {
        CoreService service = acquire(clazz);
        return (service != null ? service.getActivityContext() : null);
    }

    @Nullable
    public static ActivityContext findActivityContext(String contextName) {
        Assert.notNull(contextName, "contextName must not be null");
        for (CoreService service : allServices) {
            ActivityContext activityContext = service.getActivityContext();
            if (activityContext != null && contextName.equals(activityContext.getName())) {
                return activityContext;
            }
        }
        return null;
    }

}
