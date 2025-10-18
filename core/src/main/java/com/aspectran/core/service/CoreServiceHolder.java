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
 * A static utility class that holds and manages references to active {@link CoreService} instances.
 * <p>This class provides a global access point to Aspectran services, allowing them to be
 * acquired based on {@link ClassLoader} or specific class types. It is crucial for managing
 * multiple Aspectran contexts within a single JVM, especially in complex deployment scenarios
 * like application servers or OSGi environments.
 *
 * @since 2019-01-10
 */
public abstract class CoreServiceHolder {

    private static final Set<ServiceHoldingListener> serviceHoldingListeners = new CopyOnWriteArraySet<>();

    private static final Set<CoreService> allServices = new CopyOnWriteArraySet<>();

    private static final Map<ClassLoader, CoreService> servicesByLoader = new HashMap<>();

    private static final Map<Class<?>, CoreService> servicesByClass = new HashMap<>();

    private static volatile CoreService currentService;

    /**
     * Adds a {@link ServiceHoldingListener} to be notified when services are held or released.
     * @param listener the listener to add
     */
    public static void addServiceHoldingListener(ServiceHoldingListener listener) {
        Assert.notNull(listener, "listener must not be null");
        serviceHoldingListeners.add(listener);
    }

    /**
     * Removes a {@link ServiceHoldingListener}.
     * @param listener the listener to remove
     */
    public static void removeServiceHoldingListener(ServiceHoldingListener listener) {
        Assert.notNull(listener, "listener must not be null");
        serviceHoldingListeners.remove(listener);
    }

    /**
     * Holds a {@link CoreService} instance, making it available for acquisition.
     * @param service the service to hold
     */
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
            for (ServiceHoldingListener listener : serviceHoldingListeners) {
                listener.afterServiceHolding(service);
            }
        }
    }

    /**
     * Holds a {@link CoreService} instance associated with a specific {@link ClassLoader}.
     * @param classLoader the class loader to associate with the service
     * @param service the service to hold
     */
    public static synchronized void hold(ClassLoader classLoader, CoreService service) {
        Assert.notNull(classLoader, "classLoader must not be null");
        Assert.notNull(service, "service must not be null");
        Assert.state(allServices.contains(service), "Not a registered service: " + service);
        CoreService existing = servicesByLoader.get(classLoader);
        Assert.state(existing != service, "The classloader is already mapped to another service: " + service);
        servicesByLoader.put(classLoader, service);
    }

    /**
     * Holds a {@link CoreService} instance associated with a specific class type.
     * @param clazz the class type to associate with the service
     * @param service the service to hold
     */
    public static synchronized void hold(Class<?> clazz, CoreService service) {
        Assert.notNull(clazz, "clazz must not be null");
        Assert.notNull(service, "service must not be null");
        Assert.state(allServices.contains(service), "Not a registered service: " + service);
        CoreService existing = servicesByClass.get(clazz);
        Assert.state(existing != service, "The class is already mapped to another service: " + service);
        servicesByClass.put(clazz, service);
    }

    /**
     * Releases a {@link CoreService} instance, removing it from the holder.
     * @param service the service to release
     */
    public static synchronized void release(CoreService service) {
        Assert.notNull(service, "service must not be null");
        Assert.state(allServices.contains(service), "Not a registered service: " + service);
        for (ServiceHoldingListener listener : serviceHoldingListeners) {
            listener.beforeServiceRelease(service);
        }
        allServices.remove(service);
        if (allServices.isEmpty()) {
            serviceHoldingListeners.clear();
        }
        if (currentService != null && currentService == service) {
            currentService = null;
        }
        servicesByLoader.entrySet().removeIf(entry -> service.equals(entry.getValue()));
        servicesByClass.entrySet().removeIf(entry -> service.equals(entry.getValue()));
    }

    /**
     * Acquires the most appropriate {@link CoreService} for the current thread's context.
     * @return the acquired {@link CoreService}, or {@code null} if none is found
     */
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

    /**
     * Acquires the {@link CoreService} associated with the given class.
     * @param clazz the class to use for lookup
     * @return the acquired {@link CoreService}, or {@code null} if none is found
     */
    public static CoreService acquire(Class<?> clazz) {
        CoreService service = servicesByClass.get(clazz);
        if (service == null) {
            service = acquire();
        }
        return service;
    }

    /**
     * Finds the {@link ActivityContext} associated with the most appropriate {@link CoreService}.
     * @return the found {@link ActivityContext}, or {@code null} if none is found
     */
    @Nullable
    public static ActivityContext findActivityContext() {
        CoreService service = acquire();
        return (service != null ? service.getActivityContext() : null);
    }

    /**
     * Finds the {@link ActivityContext} associated with the {@link CoreService} linked to the given class.
     * @param clazz the class to use for lookup
     * @return the found {@link ActivityContext}, or {@code null} if none is found
     */
    @Nullable
    public static ActivityContext findActivityContext(Class<?> clazz) {
        CoreService service = acquire(clazz);
        return (service != null ? service.getActivityContext() : null);
    }

    /**
     * Finds the {@link ActivityContext} with the specified context name.
     * @param contextName the name of the context to find
     * @return the found {@link ActivityContext}, or {@code null} if none is found
     */
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
