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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReflectionUtilsTest {

    @Test
    void testGetTypeDifferenceWeight() {
        assertEquals(0.0f, ReflectionUtils.getTypeDifferenceWeight(String.class, String.class));
        assertEquals(0.1f, ReflectionUtils.getTypeDifferenceWeight(int.class, Integer.class));
        assertEquals(0.1f, ReflectionUtils.getTypeDifferenceWeight(Integer.class, int.class));

        // Subclass
        assertEquals(1.0f, ReflectionUtils.getTypeDifferenceWeight(Object.class, String.class));

        // Interface
        assertEquals(0.25f, ReflectionUtils.getTypeDifferenceWeight(CharSequence.class, CharSequence.class));

        // Multidimensional array
        assertEquals(0.0f, ReflectionUtils.getTypeDifferenceWeight(String[][].class, String[][].class));
        assertEquals(1.0f, ReflectionUtils.getTypeDifferenceWeight(Object[][].class, String[][].class));

        // Array length mismatch
        Class<?>[] src = new Class<?>[] { String.class };
        Class<?>[] dest = new Class<?>[] { String.class, Integer.class };
        assertEquals(Float.MAX_VALUE, ReflectionUtils.getTypeDifferenceWeight(src, dest));

        // Args weight
        Object[] args = new Object[] { "hello", 123 };
        Class<?>[] params = new Class<?>[] { String.class, int.class };
        assertTrue(ReflectionUtils.getTypeDifferenceWeight(params, args) < 1.0f);
    }

    @Test
    void testToPrimitiveArray() {
        assertNull(ReflectionUtils.toPrimitiveArray(null));
        assertNull(ReflectionUtils.toPrimitiveArray("not an array"));
        assertNull(ReflectionUtils.toPrimitiveArray(new String[] { "a", "b" }));

        // Boolean
        Boolean[] bools = new Boolean[] { true, false, null };
        boolean[] primitiveBools = (boolean[])ReflectionUtils.toPrimitiveArray(bools);
        assertNotNull(primitiveBools);
        assertArrayEquals(new boolean[] { true, false, false }, primitiveBools);

        // Integer
        Integer[] ints = new Integer[] { 1, 2, 3 };
        int[] primitiveInts = (int[])ReflectionUtils.toPrimitiveArray(ints);
        assertNotNull(primitiveInts);
        assertArrayEquals(new int[] { 1, 2, 3 }, primitiveInts);

        // Double
        Double[] doubles = new Double[] { 1.1, 2.2 };
        double[] primitiveDoubles = (double[])ReflectionUtils.toPrimitiveArray(doubles);
        assertNotNull(primitiveDoubles);
        assertArrayEquals(new double[] { 1.1, 2.2 }, primitiveDoubles);

        // Byte, Char, Short, Long, Float
        Byte[] bytes = new Byte[] { (byte)1, (byte)2 };
        assertArrayEquals(new byte[] { 1, 2 }, (byte[])ReflectionUtils.toPrimitiveArray(bytes));

        Character[] chars = new Character[] { 'a', 'b' };
        assertArrayEquals(new char[] { 'a', 'b' }, (char[])ReflectionUtils.toPrimitiveArray(chars));

        Short[] shorts = new Short[] { (short)10, (short)20 };
        assertArrayEquals(new short[] { 10, 20 }, (short[])ReflectionUtils.toPrimitiveArray(shorts));

        Long[] longs = new Long[] { 100L, 200L };
        assertArrayEquals(new long[] { 100L, 200L }, (long[])ReflectionUtils.toPrimitiveArray(longs));

        Float[] floats = new Float[] { 1.5f, 2.5f };
        assertArrayEquals(new float[] { 1.5f, 2.5f }, (float[])ReflectionUtils.toPrimitiveArray(floats));
    }

    @Test
    void testToComponentTypeArray() {
        assertNull(ReflectionUtils.toComponentTypeArray(null, String.class));
        assertNull(ReflectionUtils.toComponentTypeArray("not an array", String.class));

        Object[] src = new Object[] { "first", "second" };
        CharSequence[] converted = (CharSequence[])ReflectionUtils.toComponentTypeArray(src, CharSequence.class);
        assertNotNull(converted);
        assertEquals(2, converted.length);
        assertEquals("first", converted[0]);
        assertEquals("second", converted[1]);
    }

    static class SampleBean {
        private String name = "initial";

        public String greet(String prefix) {
            return prefix + " " + name;
        }

        public static int add(int a, int b) {
            return a + b;
        }
    }

    @Test
    void testFieldAccess() throws Exception {
        SampleBean bean = new SampleBean();
        Field field = SampleBean.class.getDeclaredField("name");
        field.setAccessible(true);

        assertEquals("initial", ReflectionUtils.getField(field, bean));

        ReflectionUtils.setField(field, bean, "updated");
        assertEquals("updated", ReflectionUtils.getField(field, bean));
    }

    @Test
    void testInvokeMethod() throws Exception {
        SampleBean bean = new SampleBean();
        Method greetMethod = SampleBean.class.getMethod("greet", String.class);
        Object result = ReflectionUtils.invokeMethod(greetMethod, bean, "Hello");
        assertEquals("Hello initial", result);

        Method addMethod = SampleBean.class.getMethod("add", int.class, int.class);
        Object addResult = ReflectionUtils.invokeMethod(addMethod, null, 10, 20);
        assertEquals(30, addResult);
    }

}
