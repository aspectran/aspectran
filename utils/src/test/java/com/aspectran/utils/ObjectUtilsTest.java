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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link ObjectUtils}.
 */
class ObjectUtilsTest {

    @Test
    void testIsEmpty() {
        assertTrue(ObjectUtils.isEmpty((Object)null));
        assertTrue(ObjectUtils.isEmpty((Object[])null));
        assertTrue(ObjectUtils.isEmpty(new Object[0]));
        assertFalse(ObjectUtils.isEmpty(new Object[]{"test"}));

        assertTrue(ObjectUtils.isEmpty(Optional.empty()));
        assertFalse(ObjectUtils.isEmpty(Optional.of("value")));

        assertTrue(ObjectUtils.isEmpty(""));
        assertFalse(ObjectUtils.isEmpty("hello"));

        assertTrue(ObjectUtils.isEmpty(new int[0]));
        assertFalse(ObjectUtils.isEmpty(new int[]{1, 2}));

        assertTrue(ObjectUtils.isEmpty(Collections.emptyList()));
        assertFalse(ObjectUtils.isEmpty(List.of("item")));

        assertTrue(ObjectUtils.isEmpty(Collections.emptyMap()));
        assertFalse(ObjectUtils.isEmpty(Map.of("k", "v")));

        assertFalse(ObjectUtils.isEmpty(new Object()));
    }

    @Test
    void testIsArray() {
        assertFalse(ObjectUtils.isArray(null));
        assertFalse(ObjectUtils.isArray("not an array"));
        assertTrue(ObjectUtils.isArray(new int[]{1, 2}));
        assertTrue(ObjectUtils.isArray(new String[]{"a", "b"}));
    }

    @Test
    void testToObjectArray() {
        Object[] empty = ObjectUtils.toObjectArray(null);
        assertEquals(0, empty.length);

        Object[] emptyArray = ObjectUtils.toObjectArray(new int[0]);
        assertEquals(0, emptyArray.length);

        String[] strArray = new String[]{"a", "b"};
        Object[] objArray = ObjectUtils.toObjectArray(strArray);
        assertArrayEquals(strArray, objArray);

        int[] intArray = new int[]{1, 2, 3};
        Object[] converted = ObjectUtils.toObjectArray(intArray);
        assertEquals(3, converted.length);
        assertEquals(1, converted[0]);
        assertEquals(2, converted[1]);
        assertEquals(3, converted[2]);

        assertThrows(IllegalArgumentException.class, () -> ObjectUtils.toObjectArray("not an array"));
    }

    @Test
    void testDefaultIfNull() {
        assertEquals("default", ObjectUtils.defaultIfNull(null, "default"));
        assertEquals("value", ObjectUtils.defaultIfNull("value", "default"));

        assertEquals("fromSupplier", ObjectUtils.defaultIfNull(null, () -> "fromSupplier"));
        assertEquals("value", ObjectUtils.defaultIfNull("value", () -> "fromSupplier"));
    }

    @Test
    void testContainsElement() {
        String[] array = new String[]{"a", "b", "c", null};
        assertTrue(ObjectUtils.containsElement(array, "a"));
        assertTrue(ObjectUtils.containsElement(array, "b"));
        assertTrue(ObjectUtils.containsElement(array, null));
        assertFalse(ObjectUtils.containsElement(array, "d"));
        assertFalse(ObjectUtils.containsElement(null, "a"));
    }

    @Test
    void testAddObjectToArray() {
        String[] original = new String[]{"a", "b"};
        String[] extended = ObjectUtils.addObjectToArray(original, "c");
        assertArrayEquals(new String[]{"a", "b", "c"}, extended);

        String[] fromNull = ObjectUtils.addObjectToArray(null, "first");
        assertArrayEquals(new String[]{"first"}, fromNull);
    }

    @Test
    void testNullSafeEquals() {
        assertTrue(ObjectUtils.nullSafeEquals(null, null));
        assertFalse(ObjectUtils.nullSafeEquals("a", null));
        assertFalse(ObjectUtils.nullSafeEquals(null, "b"));
        assertTrue(ObjectUtils.nullSafeEquals("test", "test"));

        // Arrays comparison
        assertTrue(ObjectUtils.nullSafeEquals(new int[]{1, 2}, new int[]{1, 2}));
        assertFalse(ObjectUtils.nullSafeEquals(new int[]{1, 2}, new int[]{1, 3}));
        assertTrue(ObjectUtils.nullSafeEquals(new boolean[]{true, false}, new boolean[]{true, false}));
        assertTrue(ObjectUtils.nullSafeEquals(new byte[]{1, 2}, new byte[]{1, 2}));
        assertTrue(ObjectUtils.nullSafeEquals(new char[]{'a', 'b'}, new char[]{'a', 'b'}));
        assertTrue(ObjectUtils.nullSafeEquals(new double[]{1.0, 2.0}, new double[]{1.0, 2.0}));
        assertTrue(ObjectUtils.nullSafeEquals(new float[]{1.0f, 2.0f}, new float[]{1.0f, 2.0f}));
        assertTrue(ObjectUtils.nullSafeEquals(new long[]{1L, 2L}, new long[]{1L, 2L}));
        assertTrue(ObjectUtils.nullSafeEquals(new short[]{1, 2}, new short[]{1, 2}));

        // Multidimensional array comparison (deepEquals)
        Object[] nested1 = new Object[]{new String[]{"a", "b"}};
        Object[] nested2 = new Object[]{new String[]{"a", "b"}};
        assertTrue(ObjectUtils.nullSafeEquals(nested1, nested2));
    }

    @Test
    void testNullSafeHashCodeAndHash() {
        assertEquals(0, ObjectUtils.nullSafeHashCode(null));
        assertTrue(ObjectUtils.nullSafeHashCode("test") != 0);
        assertTrue(ObjectUtils.nullSafeHashCode(new int[]{1, 2, 3}) != 0);
        assertTrue(ObjectUtils.nullSafeHashCode(new Object[]{new String[]{"a"}}) != 0);

        assertEquals(0, ObjectUtils.nullSafeHash((Object[]) null));
        assertTrue(ObjectUtils.nullSafeHash("a", "b", new int[]{1}) != 0);
    }

    @Test
    void testNullSafeToString() {
        assertEquals("null", ObjectUtils.nullSafeToString(null));
        assertEquals("hello", ObjectUtils.nullSafeToString("hello"));
        assertEquals("[1, 2, 3]", ObjectUtils.nullSafeToString(new int[]{1, 2, 3}));
        assertEquals("[true, false]", ObjectUtils.nullSafeToString(new boolean[]{true, false}));
        assertEquals("[1, 2]", ObjectUtils.nullSafeToString(new byte[]{1, 2}));
        assertEquals("[a, b]", ObjectUtils.nullSafeToString(new char[]{'a', 'b'}));
        assertEquals("[1.5, 2.5]", ObjectUtils.nullSafeToString(new double[]{1.5, 2.5}));
        assertEquals("[1.0, 2.0]", ObjectUtils.nullSafeToString(new float[]{1.0f, 2.0f}));
        assertEquals("[100, 200]", ObjectUtils.nullSafeToString(new long[]{100L, 200L}));
        assertEquals("[10, 20]", ObjectUtils.nullSafeToString(new short[]{10, 20}));
        assertEquals("[[a, b], [c, d]]", ObjectUtils.nullSafeToString(new String[][]{{"a", "b"}, {"c", "d"}}));
    }

    @Test
    void testIdentityToString() {
        assertEquals("", ObjectUtils.identityToString(null));
        assertEquals("", ObjectUtils.simpleIdentityToString(null));

        Object obj = new Object();
        String identity = ObjectUtils.identityToString(obj);
        assertTrue(identity.startsWith("java.lang.Object@"));

        String simpleIdentity = ObjectUtils.simpleIdentityToString(obj);
        assertTrue(simpleIdentity.startsWith("Object@"));

        String withName = ObjectUtils.simpleIdentityToString(obj, "myBean");
        assertTrue(withName.startsWith("Object@"));
        assertTrue(withName.endsWith("(myBean)"));

        assertNotNull(ObjectUtils.getIdentityHexString(obj));
    }

}
