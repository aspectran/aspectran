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
package com.aspectran.core.context.rule.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for rule types in {@code com.aspectran.core.context.rule.type}.
 */
class RuleTypeTest {

    @Test
    void testMethodType() {
        assertSame(MethodType.GET, MethodType.resolve("GET"));
        assertSame(MethodType.GET, MethodType.resolve("get"));
        assertNull(MethodType.resolve(null));
        assertNull(MethodType.resolve("UNKNOWN"));

        assertSame(MethodType.POST, MethodType.resolve("POST", MethodType.GET));
        assertSame(MethodType.GET, MethodType.resolve("UNKNOWN", MethodType.GET));
        assertSame(MethodType.GET, MethodType.resolve(null, MethodType.GET));

        assertNull(MethodType.parse(null));
        assertNull(MethodType.parse(""));
        assertNull(MethodType.parse("UNKNOWN"));

        MethodType[] parsed = MethodType.parse("GET, POST, GET, PUT");
        assertNotNullArray(parsed);
        assertArrayEquals(new MethodType[] {MethodType.GET, MethodType.POST, MethodType.PUT}, parsed);

        assertEquals("GET,POST,PUT", MethodType.stringify(parsed));
        assertNull(MethodType.stringify(null));
        assertNull(MethodType.stringify(new MethodType[0]));

        assertTrue(MethodType.GET.containsTo(parsed));
        assertFalse(MethodType.DELETE.containsTo(parsed));
        assertFalse(MethodType.GET.containsTo(null));

        assertTrue(MethodType.GET.matches("GET"));
        assertFalse(MethodType.GET.matches("POST"));
    }

    @Test
    void testTriStateType() {
        assertSame(TriStateType.TRUE, TriStateType.of(true));
        assertSame(TriStateType.FALSE, TriStateType.of(false));

        assertSame(TriStateType.TRUE, TriStateType.of(Boolean.TRUE));
        assertSame(TriStateType.FALSE, TriStateType.of(Boolean.FALSE));
        assertSame(TriStateType.UNSET, TriStateType.of((Boolean) null));

        assertEquals(Boolean.TRUE, TriStateType.TRUE.toBoolean());
        assertEquals(Boolean.FALSE, TriStateType.FALSE.toBoolean());
        assertNull(TriStateType.UNSET.toBoolean());

        assertTrue(TriStateType.TRUE.booleanValue(false));
        assertFalse(TriStateType.FALSE.booleanValue(true));
        assertTrue(TriStateType.UNSET.booleanValue(true));
        assertFalse(TriStateType.UNSET.booleanValue(false));
    }

    @Test
    void testFormatType() {
        assertSame(FormatType.XML, FormatType.resolve("xml"));
        assertNull(FormatType.resolve((String) null));

        assertSame(FormatType.TEXT, FormatType.resolve(ContentType.TEXT_PLAIN));
        assertSame(FormatType.APON, FormatType.resolve(ContentType.APPLICATION_APON));
        assertSame(FormatType.JSON, FormatType.resolve(ContentType.APPLICATION_JSON));
        assertSame(FormatType.XML, FormatType.resolve(ContentType.APPLICATION_XML));
        assertNull(FormatType.resolve(ContentType.TEXT_HTML));
        assertNull(FormatType.resolve((ContentType) null));
    }

    @Test
    void testResolves() {
        assertSame(ActionType.HEADER, ActionType.resolve("header"));
        assertNull(ActionType.resolve(null));

        assertSame(AdviceType.BEFORE, AdviceType.resolve("before"));
        assertNull(AdviceType.resolve(null));

        assertSame(AppendableFileFormatType.XML, AppendableFileFormatType.resolve("xml"));
        assertNull(AppendableFileFormatType.resolve(null));

        assertSame(AppenderType.FILE, AppenderType.resolve("file"));
        assertNull(AppenderType.resolve(null));

        assertSame(AutoReloadType.HARD, AutoReloadType.resolve("hard"));
        assertNull(AutoReloadType.resolve(null));

        assertSame(AutowireTargetType.FIELD, AutowireTargetType.resolve("field"));
        assertNull(AutowireTargetType.resolve(null));

        assertSame(BeanRefererType.ASPECT_RULE, BeanRefererType.resolve("aspectRule"));
        assertNull(BeanRefererType.resolve(null));

        assertSame(ContentType.TEXT_HTML, ContentType.resolve("text/html"));
        assertNull(ContentType.resolve(null));

        assertSame(DefaultSettingType.TRANSLET_NAME_PREFIX, DefaultSettingType.resolve("transletNamePrefix"));
        assertNull(DefaultSettingType.resolve(null));

        assertSame(ItemType.SINGLE, ItemType.resolve("single"));
        assertNull(ItemType.resolve(null));

        assertSame(ItemValueType.STRING, ItemValueType.resolve("string"));
        assertNull(ItemValueType.resolve(null));

        assertSame(JoinpointTargetType.ACTIVITY, JoinpointTargetType.resolve("activity"));
        assertNull(JoinpointTargetType.resolve(null));

        assertSame(MisfirePolicy.SMART_POLICY, MisfirePolicy.resolve("smartPolicy"));
        assertSame(MisfirePolicy.SMART_POLICY, MisfirePolicy.resolve("SMARTPOLICY"));
        assertNull(MisfirePolicy.resolve(null));

        assertSame(PointcutType.WILDCARD, PointcutType.resolve("wildcard"));
        assertNull(PointcutType.resolve(null));

        assertSame(ResponseType.TRANSFORM, ResponseType.resolve("transform"));
        assertNull(ResponseType.resolve(null));

        assertSame(ScopeType.SINGLETON, ScopeType.resolve("singleton"));
        assertNull(ScopeType.resolve(null));

        assertSame(TextStyleType.APON, TextStyleType.resolve("apon"));
        assertNull(TextStyleType.resolve(null));

        assertSame(TokenDirectiveType.FIELD, TokenDirectiveType.resolve("field"));
        assertNull(TokenDirectiveType.resolve(null));

        assertSame(TokenType.BEAN, TokenType.resolve("bean"));
        assertNull(TokenType.resolve(null));

        assertSame(TriggerType.CRON, TriggerType.resolve("cron"));
        assertNull(TriggerType.resolve(null));
    }

    private static void assertNotNullArray(Object[] array) {
        if (array == null) {
            throw new AssertionError("Expected non-null array");
        }
    }

}
