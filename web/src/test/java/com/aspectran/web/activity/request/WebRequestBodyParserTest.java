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
package com.aspectran.web.activity.request;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.component.bean.NoSuchBeanException;
import com.aspectran.core.component.bean.NoUniqueBeanException;
import com.aspectran.core.context.rule.BeanRule;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for {@link WebRequestBodyParser#parseMultipartFormData(Activity)}.
 */
class WebRequestBodyParserTest {

    private MultipartFormDataParser createMockParser(AtomicBoolean parsedFlag) {
        return (MultipartFormDataParser) Proxy.newProxyInstance(
                MultipartFormDataParser.class.getClassLoader(),
                new Class<?>[] { MultipartFormDataParser.class },
                (proxy, method, args) -> {
                    if ("parse".equals(method.getName())) {
                        parsedFlag.set(true);
                    }
                    return null;
                }
        );
    }

    private Activity createMockActivity(
            String multipartFormDataParser,
            Map<String, String> settings,
            Map<String, MultipartFormDataParser> beans,
            boolean multipleBeansWithoutDefault) {

        Translet translet = (Translet) Proxy.newProxyInstance(
                Translet.class.getClassLoader(),
                new Class<?>[] { Translet.class },
                (proxy, method, args) -> {
                    if ("getMultipartFormDataParser".equals(method.getName())) {
                        return multipartFormDataParser;
                    }
                    if ("getTransletName".equals(method.getName())) {
                        return "/test/translet";
                    }
                    return null;
                }
        );

        return (Activity) Proxy.newProxyInstance(
                Activity.class.getClassLoader(),
                new Class<?>[] { Activity.class },
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("getMultipartFormDataParser".equals(methodName)) {
                        return multipartFormDataParser;
                    }
                    if ("hasTranslet".equals(methodName)) {
                        return true;
                    }
                    if ("getTranslet".equals(methodName)) {
                        return translet;
                    }
                    if ("getSetting".equals(methodName)) {
                        String name = (String) args[0];
                        return settings != null ? settings.get(name) : null;
                    }
                    if ("hasSetting".equals(methodName)) {
                        String name = (String) args[0];
                        return settings != null && settings.containsKey(name);
                    }
                    if ("containsBean".equals(methodName)) {
                        if (args.length == 1 && args[0] instanceof String) {
                            return beans != null && beans.containsKey(args[0]);
                        }
                        if (args.length == 2 && args[1] instanceof String) {
                            return beans != null && beans.containsKey(args[1]);
                        }
                        if (args.length == 1 && args[0] == MultipartFormDataParser.class) {
                            return beans != null && !beans.isEmpty();
                        }
                    }
                    if ("getBean".equals(methodName)) {
                        if (args[0] instanceof String beanName) {
                            if (beans != null && beans.containsKey(beanName)) {
                                return beans.get(beanName);
                            }
                            throw new NoSuchBeanException(beanName);
                        }
                        if (args[0] == MultipartFormDataParser.class) {
                            if (beans == null || beans.isEmpty()) {
                                throw new NoSuchBeanException(MultipartFormDataParser.class);
                            }
                            if (multipleBeansWithoutDefault) {
                                BeanRule r1 = new BeanRule();
                                r1.setId("parserA");
                                BeanRule r2 = new BeanRule();
                                r2.setId("parserB");
                                throw new NoUniqueBeanException(MultipartFormDataParser.class, new BeanRule[] { r1, r2 });
                            }
                            return beans.values().iterator().next();
                        }
                    }
                    if ("getRequestAdapter".equals(methodName)) {
                        return null;
                    }
                    return null;
                }
        );
    }

    @Test
    void testNoMultipartConfiguration() {
        AtomicBoolean parsed = new AtomicBoolean(false);
        Map<String, MultipartFormDataParser> beans = new HashMap<>();
        beans.put("multipartFormDataParser", createMockParser(parsed));

        Activity activity = createMockActivity(null, null, beans, false);
        assertDoesNotThrow(() -> WebRequestBodyParser.parseMultipartFormData(activity));
        assertFalse(parsed.get(), "Parser should not be executed when multipart is not configured");
    }

    @Test
    void testConfiguredWithCustomParser() {
        AtomicBoolean customParsed = new AtomicBoolean(false);
        AtomicBoolean defaultParsed = new AtomicBoolean(false);
        Map<String, MultipartFormDataParser> beans = new HashMap<>();
        beans.put("customParser", createMockParser(customParsed));
        beans.put("multipartFormDataParser", createMockParser(defaultParsed));

        Activity activity = createMockActivity("customParser", null, beans, false);
        assertDoesNotThrow(() -> WebRequestBodyParser.parseMultipartFormData(activity));
        assertTrue(customParsed.get(), "Custom parser should be executed");
        assertFalse(defaultParsed.get(), "Default parser should not be executed");
    }

    @Test
    void testConfiguredWithDefaultParser() {
        AtomicBoolean defaultParsed = new AtomicBoolean(false);
        Map<String, MultipartFormDataParser> beans = new HashMap<>();
        beans.put("singleParser", createMockParser(defaultParsed));

        Activity activity = createMockActivity("", null, beans, false);
        assertDoesNotThrow(() -> WebRequestBodyParser.parseMultipartFormData(activity));
        assertTrue(defaultParsed.get(), "Single bean parser should be executed by type lookup");
    }

    @Test
    void testConfiguredWithConventionBeanName() {
        AtomicBoolean conventionParsed = new AtomicBoolean(false);
        AtomicBoolean otherParsed = new AtomicBoolean(false);
        Map<String, MultipartFormDataParser> beans = new HashMap<>();
        beans.put("multipartFormDataParser", createMockParser(conventionParsed));
        beans.put("otherParser", createMockParser(otherParsed));

        Activity activity = createMockActivity("", null, beans, false);
        assertDoesNotThrow(() -> WebRequestBodyParser.parseMultipartFormData(activity));
        assertTrue(conventionParsed.get(), "Convention bean named 'multipartFormDataParser' should be preferred");
        assertFalse(otherParsed.get(), "Other parser should not be executed");
    }

    @Test
    void testSettingWithCustomParser() {
        AtomicBoolean settingParsed = new AtomicBoolean(false);
        Map<String, MultipartFormDataParser> beans = new HashMap<>();
        beans.put("settingParser", createMockParser(settingParsed));

        Map<String, String> settings = new HashMap<>();
        settings.put("multipartFormDataParser", "settingParser");

        Activity activity = createMockActivity(null, settings, beans, false);
        assertDoesNotThrow(() -> WebRequestBodyParser.parseMultipartFormData(activity));
        assertTrue(settingParsed.get(), "Parser specified via activity setting should be executed");
    }

    @Test
    void testNonExistentParserBeanThrowsException() {
        Map<String, MultipartFormDataParser> beans = new HashMap<>();

        Activity activity = createMockActivity("nonExistent", null, beans, false);
        assertThrows(MultipartRequestParseException.class,
                () -> WebRequestBodyParser.parseMultipartFormData(activity));
    }

    @Test
    void testNoParserBeanThrowsException() {
        Map<String, MultipartFormDataParser> beans = new HashMap<>();

        Activity activity = createMockActivity("", null, beans, false);
        assertThrows(MultipartRequestParseException.class,
                () -> WebRequestBodyParser.parseMultipartFormData(activity));
    }

    @Test
    void testMultipleParserBeansWithoutNameThrowsException() {
        Map<String, MultipartFormDataParser> beans = new HashMap<>();
        beans.put("parserA", createMockParser(new AtomicBoolean()));
        beans.put("parserB", createMockParser(new AtomicBoolean()));

        Activity activity = createMockActivity("", null, beans, true);
        assertThrows(MultipartRequestParseException.class,
                () -> WebRequestBodyParser.parseMultipartFormData(activity));
    }

}
