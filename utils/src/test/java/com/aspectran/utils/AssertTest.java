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
package com.aspectran.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssertTest {

    @Test
    void testState() {
        assertDoesNotThrow(() -> Assert.state(true, "state must be true"));
        assertDoesNotThrow(() -> Assert.state(true, () -> "state must be true"));
        assertDoesNotThrow(() -> Assert.state(true));

        IllegalStateException ex1 = assertThrows(IllegalStateException.class,
                () -> Assert.state(false, "error state"));
        assertEquals("error state", ex1.getMessage());

        IllegalStateException ex2 = assertThrows(IllegalStateException.class,
                () -> Assert.state(false, () -> "lazy error state"));
        assertEquals("lazy error state", ex2.getMessage());

        IllegalStateException ex3 = assertThrows(IllegalStateException.class,
                () -> Assert.state(false));
        assertTrue(ex3.getMessage().contains("Assertion failed"));
    }

    @Test
    void testIsTrue() {
        assertDoesNotThrow(() -> Assert.isTrue(true, "must be true"));
        assertDoesNotThrow(() -> Assert.isTrue(true, () -> "must be true"));
        assertDoesNotThrow(() -> Assert.isTrue(true));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Assert.isTrue(false, "argument false"));
        assertEquals("argument false", ex.getMessage());

        assertThrows(IllegalArgumentException.class,
                () -> Assert.isTrue(false));
    }

    @Test
    void testIsNullAndNotNull() {
        assertDoesNotThrow(() -> Assert.isNull(null, "must be null"));
        assertDoesNotThrow(() -> Assert.isNull(null));
        assertThrows(IllegalArgumentException.class, () -> Assert.isNull("not null", "error"));
        assertThrows(IllegalArgumentException.class, () -> Assert.isNull("not null"));

        assertDoesNotThrow(() -> Assert.notNull("value", "must not be null"));
        assertDoesNotThrow(() -> Assert.notNull("value", () -> "must not be null"));
        assertDoesNotThrow(() -> Assert.notNull("value"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Assert.notNull(null, "null arg"));
        assertEquals("null arg", ex.getMessage());
        assertThrows(IllegalArgumentException.class, () -> Assert.notNull(null));
    }

    @Test
    void testHasLengthAndHasText() {
        assertDoesNotThrow(() -> Assert.hasLength("abc", "must have length"));
        assertDoesNotThrow(() -> Assert.hasLength(new StringBuilder("abc")));
        assertThrows(IllegalArgumentException.class, () -> Assert.hasLength("", "error"));
        assertThrows(IllegalArgumentException.class, () -> Assert.hasLength(null));

        assertDoesNotThrow(() -> Assert.hasText("  abc  ", "must have text"));
        assertDoesNotThrow(() -> Assert.hasText(new StringBuilder("abc")));
        assertThrows(IllegalArgumentException.class, () -> Assert.hasText("   ", "error"));
        assertThrows(IllegalArgumentException.class, () -> Assert.hasText(null));
    }

    @Test
    void testDoesNotContain() {
        assertDoesNotThrow(() -> Assert.doesNotContain("hello world", "foo", "must not contain"));
        assertDoesNotThrow(() -> Assert.doesNotContain("hello world", "foo"));
        assertDoesNotThrow(() -> Assert.doesNotContain(null, "foo"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Assert.doesNotContain("hello world", "world", "contains world"));
        assertEquals("contains world", ex.getMessage());

        assertThrows(IllegalArgumentException.class,
                () -> Assert.doesNotContain("hello world", "world"));
    }

    @Test
    void testNotEmptyArray() {
        String[] arr = new String[]{"a", "b"};
        assertDoesNotThrow(() -> Assert.notEmpty(arr, "must not be empty"));
        assertDoesNotThrow(() -> Assert.notEmpty(arr));

        assertThrows(IllegalArgumentException.class, () -> Assert.notEmpty((Object[]) null, "error"));
        assertThrows(IllegalArgumentException.class, () -> Assert.notEmpty(new Object[0]));
    }

    @Test
    void testNoNullElementsArray() {
        String[] valid = new String[]{"a", "b"};
        assertDoesNotThrow(() -> Assert.noNullElements(valid, "no nulls"));
        assertDoesNotThrow(() -> Assert.noNullElements(valid));
        assertDoesNotThrow(() -> Assert.noNullElements((Object[]) null));

        String[] invalid = new String[]{"a", null, "b"};
        assertThrows(IllegalArgumentException.class, () -> Assert.noNullElements(invalid, "has null"));
        assertThrows(IllegalArgumentException.class, () -> Assert.noNullElements(invalid));
    }

    @Test
    void testNotEmptyCollection() {
        List<String> list = List.of("a", "b");
        assertDoesNotThrow(() -> Assert.notEmpty(list, "must not be empty"));
        assertDoesNotThrow(() -> Assert.notEmpty(list));

        assertThrows(IllegalArgumentException.class, () -> Assert.notEmpty((List<?>) null, "error"));
        assertThrows(IllegalArgumentException.class, () -> Assert.notEmpty(Collections.emptyList()));
    }

    @Test
    void testNoNullElementsCollection() {
        List<String> valid = List.of("a", "b");
        assertDoesNotThrow(() -> Assert.noNullElements(valid, "no nulls"));
        assertDoesNotThrow(() -> Assert.noNullElements(valid));
        assertDoesNotThrow(() -> Assert.noNullElements((List<?>) null));

        List<String> invalid = Arrays.asList("a", null, "b");
        assertThrows(IllegalArgumentException.class, () -> Assert.noNullElements(invalid, "has null"));
        assertThrows(IllegalArgumentException.class, () -> Assert.noNullElements(invalid));
    }

    @Test
    void testNotEmptyMap() {
        Map<String, String> map = Map.of("key", "val");
        assertDoesNotThrow(() -> Assert.notEmpty(map, "must not be empty"));
        assertDoesNotThrow(() -> Assert.notEmpty(map));

        assertThrows(IllegalArgumentException.class, () -> Assert.notEmpty((Map<?, ?>) null, "error"));
        assertThrows(IllegalArgumentException.class, () -> Assert.notEmpty(Collections.emptyMap()));
    }

    @Test
    void testIsInstanceOf() {
        assertDoesNotThrow(() -> Assert.isInstanceOf(String.class, "hello", "must be string"));
        assertDoesNotThrow(() -> Assert.isInstanceOf(Number.class, 123));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Assert.isInstanceOf(String.class, 123, "Type mismatch"));
        assertTrue(ex.getMessage().contains("Type mismatch"));

        assertThrows(IllegalArgumentException.class,
                () -> Assert.isInstanceOf(String.class, null));
    }

    @Test
    void testIsAssignable() {
        assertDoesNotThrow(() -> Assert.isAssignable(Number.class, Integer.class, "must be assignable"));
        assertDoesNotThrow(() -> Assert.isAssignable(CharSequence.class, String.class));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Assert.isAssignable(Integer.class, String.class, "not assignable"));
        assertTrue(ex.getMessage().contains("not assignable"));

        assertThrows(IllegalArgumentException.class,
                () -> Assert.isAssignable(Number.class, null));
    }

}
