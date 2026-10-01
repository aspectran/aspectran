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

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeUtilsTest {

    @Test
    void testIsPrimitiveWrapper() {
        assertTrue(TypeUtils.isPrimitiveWrapper(Boolean.class));
        assertTrue(TypeUtils.isPrimitiveWrapper(Byte.class));
        assertTrue(TypeUtils.isPrimitiveWrapper(Character.class));
        assertTrue(TypeUtils.isPrimitiveWrapper(Short.class));
        assertTrue(TypeUtils.isPrimitiveWrapper(Integer.class));
        assertTrue(TypeUtils.isPrimitiveWrapper(Long.class));
        assertTrue(TypeUtils.isPrimitiveWrapper(Float.class));
        assertTrue(TypeUtils.isPrimitiveWrapper(Double.class));
        assertTrue(TypeUtils.isPrimitiveWrapper(Void.class));

        assertFalse(TypeUtils.isPrimitiveWrapper(int.class));
        assertFalse(TypeUtils.isPrimitiveWrapper(void.class));
        assertFalse(TypeUtils.isPrimitiveWrapper(String.class));
        assertFalse(TypeUtils.isPrimitiveWrapper(null));
    }

    @Test
    void testIsPrimitiveArrayAndWrapperArray() {
        assertTrue(TypeUtils.isPrimitiveArray(int[].class));
        assertTrue(TypeUtils.isPrimitiveArray(boolean[].class));
        assertFalse(TypeUtils.isPrimitiveArray(Integer[].class));
        assertFalse(TypeUtils.isPrimitiveArray(String[].class));

        assertTrue(TypeUtils.isPrimitiveWrapperArray(Integer[].class));
        assertTrue(TypeUtils.isPrimitiveWrapperArray(Boolean[].class));
        assertFalse(TypeUtils.isPrimitiveWrapperArray(int[].class));
        assertFalse(TypeUtils.isPrimitiveWrapperArray(String[].class));
    }

    @Test
    void testIsAssignable() {
        // Standard hierarchy
        assertTrue(TypeUtils.isAssignable(Object.class, String.class));
        assertTrue(TypeUtils.isAssignable(Number.class, Integer.class));
        assertTrue(TypeUtils.isAssignable(CharSequence.class, String.class));

        // Array to Object / Interface
        assertTrue(TypeUtils.isAssignable(Object.class, String[].class));
        assertTrue(TypeUtils.isAssignable(Object.class, int[].class));
        assertTrue(TypeUtils.isAssignable(Serializable.class, String[].class));
        assertTrue(TypeUtils.isAssignable(Cloneable.class, int[].class));

        // Autoboxing / Unboxing
        assertTrue(TypeUtils.isAssignable(int.class, Integer.class));
        assertTrue(TypeUtils.isAssignable(Integer.class, int.class));
        assertTrue(TypeUtils.isAssignable(Integer[].class, int[].class));
        assertTrue(TypeUtils.isAssignable(int[].class, Integer[].class));

        // Null checks
        assertTrue(TypeUtils.isAssignable(String.class, null));
        assertTrue(TypeUtils.isAssignable(Integer.class, null));
        assertFalse(TypeUtils.isAssignable(int.class, null));

        // Incompatible
        assertFalse(TypeUtils.isAssignable(Integer.class, String.class));
        assertFalse(TypeUtils.isAssignable(int[].class, String[].class));
    }

    @Test
    void testIsAssignableArrayOverload() {
        Class<?>[] lhs = new Class<?>[] { Object.class, int.class, String.class };
        Class<?>[] rhs = new Class<?>[] { String[].class, Integer.class, String.class };
        assertTrue(TypeUtils.isAssignable(lhs, rhs));

        Class<?>[] rhsMismatch = new Class<?>[] { String[].class, Integer.class };
        assertFalse(TypeUtils.isAssignable(lhs, rhsMismatch));
        assertTrue(TypeUtils.isAssignable((Class<?>[])null, (Class<?>[])null));
    }

    @Test
    void testIsAssignableValue() {
        // Object to primitive/wrapper array
        assertTrue(TypeUtils.isAssignableValue(Object.class, new int[] { 1, 2 }));
        assertTrue(TypeUtils.isAssignableValue(Object.class, new String[] { "a", "b" }));

        // Autoboxing
        assertTrue(TypeUtils.isAssignableValue(int.class, 10));
        assertTrue(TypeUtils.isAssignableValue(int.class, Integer.valueOf(10)));
        assertTrue(TypeUtils.isAssignableValue(Integer.class, 10));
        assertTrue(TypeUtils.isAssignableValue(int[].class, new Integer[] { 1, 2 }));
        assertTrue(TypeUtils.isAssignableValue(Integer[].class, new int[] { 1, 2 }));

        // Null value
        assertTrue(TypeUtils.isAssignableValue(String.class, null));
        assertFalse(TypeUtils.isAssignableValue(int.class, null));

        // Array values
        Class<?>[] types = new Class<?>[] { Object.class, int.class };
        Object[] values = new Object[] { "test", 100 };
        assertTrue(TypeUtils.isAssignableValue(types, values));
    }

    @Test
    void testWrapperAndPrimitiveConversions() {
        assertEquals(Integer.class, TypeUtils.getPrimitiveWrapper(int.class));
        assertEquals(int.class, TypeUtils.wrapperToPrimitive(Integer.class));
        assertEquals(Void.class, TypeUtils.getPrimitiveWrapper(void.class));
        assertEquals(void.class, TypeUtils.wrapperToPrimitive(Void.class));

        Class<?>[] wrappers = new Class<?>[] { Integer.class, Boolean.class, String.class };
        Class<?>[] primitives = TypeUtils.wrappersToPrimitives(wrappers);
        assertArrayEquals(new Class<?>[] { int.class, boolean.class, null }, primitives);
    }

    @Test
    void testGetPrimitiveDefaultValue() {
        assertEquals(false, TypeUtils.getPrimitiveDefaultValue(boolean.class));
        assertEquals('\u0000', TypeUtils.getPrimitiveDefaultValue(char.class));
        assertEquals((byte)0, TypeUtils.getPrimitiveDefaultValue(byte.class));
        assertEquals((short)0, TypeUtils.getPrimitiveDefaultValue(short.class));
        assertEquals(0, TypeUtils.getPrimitiveDefaultValue(int.class));
        assertEquals(0L, TypeUtils.getPrimitiveDefaultValue(long.class));
        assertEquals(0.0f, TypeUtils.getPrimitiveDefaultValue(float.class));
        assertEquals(0.0d, TypeUtils.getPrimitiveDefaultValue(double.class));
        assertNull(TypeUtils.getPrimitiveDefaultValue(String.class));
    }

}
