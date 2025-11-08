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
package com.aspectran.core.context.asel.ognl;

import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class that defines and enforces security restrictions for OGNL expressions.
 * <p>This class maintains allowlists and blocklists for Java packages, classes, and
 * methods to prevent the execution of potentially malicious code through expression
 * evaluation. It is used by {@link OgnlMemberAccess} to determine whether a specific
 * member access is permitted.</p>
 */
public class OgnlRestrictions {

    // NOTE these lists are hard-wired into code, so any change to these sets should be synchronized with changes
    // in the corresponding code for quickly checking the fact that a type name might be in the blocking list.
    private static final Set<String> BLOCKED_ALL_PURPOSES_PACKAGE_NAME_PREFIXES =
            new HashSet<>(Arrays.asList(
                    "java.", "javax.", "jakarta.", "jdk.",
                    "org.ietf.jgss.", "org.omg.", "org.w3c.dom.", "org.xml.sax.",
                    "com.sun.", "sun."));
    private static final Set<String> ALLOWED_ALL_PURPOSES_PACKAGE_NAME_PREFIXES =
            new HashSet<>(Arrays.asList(
                    "java.time."));
    private static final Set<String> BLOCKED_TYPE_REFERENCE_PACKAGE_NAME_PREFIXES =
            new HashSet<>(Arrays.asList(
                    "com.palantir.javapoet.",
                    "net.bytebuddy.", "net.sf.cglib.",
                    "javassist.", "javax0.geci.",
                    "org.apache.bcel.", "org.aspectj.", "org.javassist.", "org.mockito.", "org.objectweb.asm.",
                    "org.objenesis.", "org.springframework.aot.", "org.springframework.asm.",
                    "org.springframework.cglib.", "org.springframework.javapoet.", "org.springframework.objenesis.",
                    "org.springframework.web.", "org.springframework.webflow.", "org.springframework.context.",
                    "org.springframework.beans.", "org.springframework.aspects.", "org.springframework.aop.",
                    "org.springframework.expression.", "org.springframework.util.",
                    "com.aspectran.core.", "com.aspectran.daemon.", "com.aspectran.embed.", "com.aspectran.logging.",
                    "com.aspectran.shell.", "com.aspectran.utils.", "com.aspectran.web.", "com.aspectran.freemarker.",
                    "com.aspectran.jetty.", "com.aspectran.mybatis.", "com.aspectran.pebble.", "com.aspectran.thymeleaf.",
                    "com.aspectran.undertow."));

    private static final Set<String> ALLOWED_JAVA_CLASS_NAMES;
    private static final Set<Class<?>> ALLOWED_JAVA_CLASSES =
            new HashSet<>(Arrays.asList(
                    // java.lang
                    Boolean.class, Byte.class, Character.class, Double.class, Enum.class, Float.class,
                    Integer.class, Long.class, Math.class, Number.class, Short.class, String.class,
                    // java.math
                    BigDecimal.class, BigInteger.class, RoundingMode.class,
                    // java.util
                    ArrayList.class, LinkedList.class, HashMap.class, LinkedHashMap.class, HashSet.class,
                    LinkedHashSet.class, Iterator.class, Enumeration.class, Deque.class, Locale.class, Properties.class,
                    Date.class, Calendar.class, Optional.class, OptionalDouble.class, OptionalInt.class,
                    OptionalLong.class, UUID.class, Currency.class,
                    // java.util.concurrent.atomic
                    AtomicBoolean.class, AtomicInteger.class, AtomicIntegerArray.class, AtomicIntegerFieldUpdater.class,
                    AtomicLong.class, AtomicLongArray.class, AtomicLongFieldUpdater.class,
                    AtomicMarkableReference.class, AtomicReference.class, AtomicReferenceArray.class,
                    AtomicReferenceFieldUpdater.class, AtomicStampedReference.class, DoubleAccumulator.class,
                    DoubleAdder.class, LongAccumulator.class, LongAdder.class,
                    // java.sql
                    java.sql.Date.class, Time.class, Timestamp.class));

    private static final Set<String> ALLOWED_JAVA_SUPERS_NAMES;
    private static final Set<Class<?>> ALLOWED_JAVA_SUPERS =
            new HashSet<>(Arrays.asList(
                    // java.util
                    Collection.class, Iterable.class, Iterator.class, List.class, Map.class, Map.Entry.class, Set.class,
                    Calendar.class, Stream.class));

    private static final Set<String> BLOCKED_MEMBER_CALL_JAVA_SUPERS_NAMES =
            new HashSet<>(Arrays.asList(
                    // java.lang
                    "java.lang.ClassLoader",
                    // org.thymeleaf
                    "org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator",
                    "org.thymeleaf.standard.expression.IStandardExpressionParser",
                    "org.thymeleaf.standard.expression.IStandardConversionService",
                    "org.thymeleaf.spring5.context.IThymeleafRequestContext",
                    "org.thymeleaf.spring5.expression.IThymeleafEvaluationContext",
                    "org.thymeleaf.spring6.context.IThymeleafRequestContext",
                    "org.thymeleaf.spring6.expression.IThymeleafEvaluationContext",
                    // org.springframework
                    "org.springframework.web.servlet.support.RequestContext",
                    "org.springframework.web.reactive.result.view.RequestContext",
                    "org.springframework.core.io.ResourceLoader",
                    // com.aspectran
                    "com.aspectran.core.activity.Activity",
                    "com.aspectran.core.activity.Translet",
                    "com.aspectran.core.service.Service"
            ));
    private static final Set<Class<?>> BLOCKED_MEMBER_CALL_JAVA_SUPERS;

    private static final Set<String> ALLOWED_CLASS_METHODS =
            new HashSet<>(Arrays.asList(
                    "getName", "isAssignableFrom", "isInstance",
                    "isInterface", "isPrimitive", "isRecord", "isAnnotation", "isArray", "isEnum"));
    private static final Set<String> BLOCKED_CLASS_METHODS =
            Arrays.stream(Class.class.getDeclaredMethods()).map(Method::getName).collect(Collectors.toSet());

    static {
        ALLOWED_JAVA_CLASS_NAMES = ALLOWED_JAVA_CLASSES.stream().map(Class::getName).collect(Collectors.toSet());
        ALLOWED_JAVA_SUPERS_NAMES = ALLOWED_JAVA_SUPERS.stream().map(Class::getName).collect(Collectors.toSet());
        BLOCKED_MEMBER_CALL_JAVA_SUPERS = BLOCKED_MEMBER_CALL_JAVA_SUPERS_NAMES.stream().
            map(className -> {
                try {
                    return Optional.of(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    return Optional.ofNullable((Class<?>)null);
                }
            }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
    }

    public static boolean isMemberAllowed(Object target, String memberName) {
        Assert.notNull(memberName, "Member name cannot be null");
        if (target == null) {
            return true;
        }

        String normalizedMemberName = normalize(memberName);

        // Calling Object#getClass() or Object#toString() will always be allowed
        if ("getClass".equals(normalizedMemberName) || "toString".equals(normalizedMemberName)) {
            return true;
        }

        // If the target itself is a class, that means we are calling a static method on it. And therefore we
        // will need to determine whether the class itself is blocked.
        if (target instanceof Class<?> clazz) {
            String targetTypeName = clazz.getName();
            // If target is a blocked class, we will only allow calling "getName"
            return ALLOWED_CLASS_METHODS.contains(normalizedMemberName) ||
                    (!BLOCKED_CLASS_METHODS.contains(normalizedMemberName) && isTypeAllowed(targetTypeName));
        }

        return isMemberAllowedForInstanceOfType(target.getClass(), normalizedMemberName);
    }

    private static boolean isTypeAllowed(String typeName) {
        Assert.notNull(typeName, "Type name cannot be null");
        String normalizedTypeName = normalize(typeName);
        if (!isTypeBlockedForTypeReference(normalizedTypeName)) {
            return true;
        }
        // We know the package is blocked, but certain classes and interfaces in blocked packages are allowed
        return ALLOWED_JAVA_CLASS_NAMES.contains(normalizedTypeName) ||
                ALLOWED_JAVA_SUPERS_NAMES.contains(normalizedTypeName);
    }

    private static boolean isMemberAllowedForInstanceOfType(Class<?> type, String memberName) {
        Assert.notNull(type, "Type cannot be null");
        String typeName = type.getName();
        if (!isTypeBlockedForAllPurposes(typeName) && !isTypeBlockedForMemberCalls(type)) {
            return true;
        }

        // We know the package is blocked, so whether we can actually call methods or see fields of it depends
        // on other checks like whether the class (inside the blocked package) is allowed, or whether the method
        // is declared in an allowed package or interface. Also, enums, annotations and proxies are always allowed.

        // Enums and annotations in blocked packages are OK
        if (type.isEnum() || type.isAnnotation()) {
            return true;
        }

        // We will allow methods to be called on JDK-proxied classes. These proxied
        // classes are typically created under "jdk.proxyX" packages so calling methods
        // on them would be forbidden by default if we didn't allow this explicitly.
        if (Proxy.isProxyClass(type)) {
            return true;
        }

        if (ALLOWED_JAVA_CLASSES.contains(type)) {
            return true;
        }

        // Otherwise, we will restrict calls to methods declared in one of the allowed interfaces or superclasses
        return ALLOWED_JAVA_SUPERS.stream()
            .filter(i -> i.isAssignableFrom(type))
            .anyMatch(i -> Arrays.stream(i.getDeclaredMethods())
                .anyMatch(m -> memberName.equals(m.getName())));

    }

    private static boolean isTypeBlockedForMemberCalls(Class<?> type) {
        return BLOCKED_MEMBER_CALL_JAVA_SUPERS.stream().anyMatch(i -> i.isAssignableFrom(type));
    }

    private static boolean isTypeBlockedForTypeReference(String typeName) {
        if (isTypeBlockedForAllPurposes(typeName)) {
            return true;
        }
        char c0 = typeName.charAt(0);
        if (c0 != 'c' && c0 != 'n' && c0 != 'j' && c0 != 'o') { // All blocked packages start with: c, n, j, o
            return false;
        }
        if (c0 == 'c') { // Shortcut for the lot of allowed "com." packages out there.
            return typeName.startsWith("com.palantir.javapoet.");
        }
        return BLOCKED_TYPE_REFERENCE_PACKAGE_NAME_PREFIXES.stream().anyMatch(typeName::startsWith);
    }

    private static boolean isTypeBlockedForAllPurposes(@NonNull String typeName) {
        char c0 = typeName.charAt(0);
        if (c0 != 'c' && c0 != 'j' && c0 != 'o' && c0 != 's') { // All blocked packages start with: c, j, o, s
            return false;
        }
        if (c0 == 'c') { // Shortcut for the lot of allowed "com." packages out there.
            return typeName.startsWith("com.sun.");
        }
        if (isJavaPackage(typeName)) {
            return !typeName.startsWith("java.time.");
        }
        return BLOCKED_ALL_PURPOSES_PACKAGE_NAME_PREFIXES.stream().anyMatch(typeName::startsWith);
    }

    private static boolean isJavaPackage(@NonNull String typeName) {
        return (typeName.charAt(0) == 'j' && typeName.charAt(4) == '.' && typeName.charAt(1) == 'a'
                && typeName.charAt(2) == 'v' && typeName.charAt(3) == 'a');
    }

    private static String normalize(String expression) {
        if (expression == null) {
            return null;
        }
        StringBuilder sb = null;
        int expLen = expression.length();
        char c;
        for (int i = 0; i < expLen; i++) {
            c = expression.charAt(i);
            if (c != '\n' && (c < '\u0020' || (c >= '\u007F' && c <= '\u009F'))) {
                if (sb == null) {
                    sb = new StringBuilder(expLen);
                    sb.append(expression, 0, i);
                }
            } else if (sb != null) {
                sb.append(c);
            }
        }
        return (sb == null ? expression : sb.toString());
    }

}
