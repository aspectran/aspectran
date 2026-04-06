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
package com.aspectran.core.component.bean.proxy;

import com.aspectran.core.activity.HintParameters;
import com.aspectran.core.component.bean.annotation.Hint;
import com.aspectran.core.component.bean.annotation.Hints;
import com.aspectran.core.context.rule.BeanRule;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The ProxyHintScanner class is responsible for scanning {@link Hint} annotations
 * on bean methods and their corresponding interface methods.
 *
 * <p>Created: 2026. 4. 3.</p>
 */
public abstract class ProxyHintScanner {

    @NonNull
    public static Map<Method, List<HintParameters>> scan(@NonNull BeanRule beanRule) {
        Class<?> beanClass = beanRule.getBeanClass();
        if (beanClass == null) {
            return Collections.emptyMap();
        }

        Map<Method, List<HintParameters>> methodHints = new HashMap<>();
        // Scan the class itself
        for (Method method : beanClass.getMethods()) {
            List<HintParameters> hints = scanMethodHints(beanRule, method);
            if (!hints.isEmpty()) {
                addMethodHints(methodHints, method, hints);
                // If it's an implementation of an interface method, also register for that interface method
                registerInterfaceMethods(methodHints, beanRule, method, hints);
            }
        }
        // Scan all interfaces
        for (Class<?> iface : getAllInterfaces(beanClass)) {
            for (Method method : iface.getMethods()) {
                List<HintParameters> hints = scanMethodHints(beanRule, method);
                if (!hints.isEmpty()) {
                    addMethodHints(methodHints, method, hints);
                }
            }
        }
        return (methodHints.isEmpty() ? Collections.emptyMap() : methodHints);
    }

    /**
     * Scans a specific method for {@link Hint} or {@link Hints} annotations.
     * @param beanRule the bean rule
     * @param method the method to scan
     * @return a list of hints found on the method
     */
    @NonNull
    private static List<HintParameters> scanMethodHints(BeanRule beanRule, @NonNull Method method) {
        Hints hintsAnnotation = method.getAnnotation(Hints.class);
        if (hintsAnnotation != null) {
            Hint[] values = hintsAnnotation.value();
            List<HintParameters> hints = new ArrayList<>(values.length);
            for (Hint hint : values) {
                hints.add(parseHint(beanRule, hint));
            }
            return hints;
        }
        Hint hintAnnotation = method.getAnnotation(Hint.class);
        if (hintAnnotation != null) {
            return Collections.singletonList(parseHint(beanRule, hintAnnotation));
        }
        return Collections.emptyList();
    }

    /**
     * Adds the collected hints for a method to the cache.
     * @param methodHints the map of method hints
     * @param method the method to which the hints apply
     * @param hints the list of hints to add
     */
    private static void addMethodHints(
            @NonNull Map<Method, List<HintParameters>> methodHints,
            @NonNull Method method, @NonNull List<HintParameters> hints) {
        String source = method.toString();
        for (HintParameters hint : hints) {
            hint.setSource(source);
        }
        methodHints.computeIfAbsent(method, k -> new ArrayList<>()).addAll(hints);
    }

    /**
     * Registers implementation-level hints for all corresponding interface methods.
     * @param methodHints the map of method hints
     * @param beanRule the bean rule
     * @param implMethod the implementation method
     * @param hints the hints to register
     */
    private static void registerInterfaceMethods(
            Map<Method, List<HintParameters>> methodHints,
            @NonNull BeanRule beanRule,
            Method implMethod, List<HintParameters> hints) {
        for (Class<?> iface : getAllInterfaces(beanRule.getBeanClass())) {
            try {
                Method ifaceMethod = iface.getMethod(implMethod.getName(), implMethod.getParameterTypes());
                addMethodHints(methodHints, ifaceMethod, hints);
            } catch (NoSuchMethodException e) {
                // ignore
            }
        }
    }

    /**
     * Returns all interfaces implemented by the given class and its superclasses.
     * @param clazz the class to inspect
     * @return a list of all implemented interfaces
     */
    @NonNull
    private static List<Class<?>> getAllInterfaces(Class<?> clazz) {
        List<Class<?>> interfaces = new ArrayList<>();
        while (clazz != null) {
            for (Class<?> iface : clazz.getInterfaces()) {
                if (!interfaces.contains(iface)) {
                    interfaces.add(iface);
                    getAllInterfaces(iface, interfaces);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return interfaces;
    }

    /**
     * Recursively collects all super-interfaces of the given interface.
     * @param iface the interface to inspect
     * @param allInterfaces the list to populate with discovered interfaces
     */
    private static void getAllInterfaces(@NonNull Class<?> iface, List<Class<?>> allInterfaces) {
        for (Class<?> superIface : iface.getInterfaces()) {
            if (!allInterfaces.contains(superIface)) {
                allInterfaces.add(superIface);
                getAllInterfaces(superIface, allInterfaces);
            }
        }
    }

    /**
     * Parses a {@link Hint} annotation's value into a {@link HintParameters} object.
     * @param beanRule the bean rule
     * @param hintAnnotation the hint annotation to parse
     * @return the parsed hint parameters
     * @throws BeanProxyException if parsing the hint value fails
     */
    @NonNull
    private static HintParameters parseHint(BeanRule beanRule, @NonNull Hint hintAnnotation) {
        try {
            return new HintParameters(hintAnnotation.type(), hintAnnotation.value(), hintAnnotation.propagated());
        } catch (Exception e) {
            throw new BeanProxyException(beanRule, "Failed to parse @Hint(type=\"" +
                    hintAnnotation.type() + "\", value=\"" + hintAnnotation.value() + "\")", e);
        }
    }

}
