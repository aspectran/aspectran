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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassUtilsTest {

    public static boolean staticBlockExecuted = false;

    @Test
    void testCreateInstanceDefault() {
        String instance = ClassUtils.createInstance(String.class);
        assertNotNull(instance);
        assertEquals("", instance);
    }

    @Test
    void testCreateInstanceWithArgs() {
        TestBean instance = ClassUtils.createInstance(TestBean.class, "Aspectran", 2026);
        assertNotNull(instance);
        assertEquals("Aspectran", instance.getName());
        assertEquals(2026, instance.getAge());
    }

    @Test
    void testCreateInstanceWithNullArg() {
        TestBean instance = ClassUtils.createInstance(TestBean.class, null, 100);
        assertNotNull(instance);
        assertNull(instance.getName());
        assertEquals(100, instance.getAge());
    }

    @Test
    void testFindConstructor() throws NoSuchMethodException {
        Constructor<TestBean> ctor = ClassUtils.findConstructor(TestBean.class, String.class, int.class);
        assertNotNull(ctor);

        Constructor<TestBean> defaultCtor = ClassUtils.findConstructor(TestBean.class);
        assertNotNull(defaultCtor);
    }

    @Test
    void testClassForNamePrimitives() throws ClassNotFoundException {
        assertEquals(boolean.class, ClassUtils.classForName("boolean"));
        assertEquals(byte.class, ClassUtils.classForName("byte"));
        assertEquals(char.class, ClassUtils.classForName("char"));
        assertEquals(short.class, ClassUtils.classForName("short"));
        assertEquals(int.class, ClassUtils.classForName("int"));
        assertEquals(long.class, ClassUtils.classForName("long"));
        assertEquals(float.class, ClassUtils.classForName("float"));
        assertEquals(double.class, ClassUtils.classForName("double"));
        assertEquals(void.class, ClassUtils.classForName("void"));
    }

    @Test
    void testClassForNameArrays() throws ClassNotFoundException {
        assertEquals(int[].class, ClassUtils.classForName("int[]"));
        assertEquals(String[].class, ClassUtils.classForName("java.lang.String[]"));
        assertEquals(String[][].class, ClassUtils.classForName("java.lang.String[][]"));
    }

    @Test
    void testClassForNameNestedClass() throws ClassNotFoundException {
        assertEquals(Map.Entry.class, ClassUtils.classForName("java.util.Map.Entry"));
    }

    @Test
    void testClassForNameWithoutInitialization() throws ClassNotFoundException {
        staticBlockExecuted = false;
        Class<?> clazz = ClassUtils.classForName("com.aspectran.utils.ClassUtilsTest$UninitializedTarget");
        assertNotNull(clazz);
        assertFalse(staticBlockExecuted);
    }

    @Test
    void testIsPresent() {
        assertTrue(ClassUtils.isPresent("java.lang.String"));
        assertTrue(ClassUtils.isPresent("int"));
        assertTrue(ClassUtils.isPresent("java.lang.String[]"));
        assertFalse(ClassUtils.isPresent("com.nonexistent.NonExistentClass"));
        assertFalse(ClassUtils.isPresent(null));
    }

    @Test
    void testIsVisible() {
        assertTrue(ClassUtils.isVisible(String.class, null));
        assertTrue(ClassUtils.isVisible(String.class, ClassLoader.getSystemClassLoader()));
    }

    @Test
    void testGetUserClass() {
        assertEquals(String.class, ClassUtils.getUserClass(String.class));
        assertEquals(TestBean.class, ClassUtils.getUserClass(TestBean.class));
    }

    @Test
    void testGetShortName() {
        assertEquals("String", ClassUtils.getShortName(String.class));
        assertEquals("String[]", ClassUtils.getShortName(String[].class));
        assertEquals("ClassUtilsTest.TestBean", ClassUtils.getShortName(TestBean.class));
        assertEquals("MyService", ClassUtils.getShortName("com.example.MyService$$EnhancerByCGLIB$$1234"));
    }

    @Test
    void testGetPackageName() {
        assertEquals("java.lang", ClassUtils.getPackageName(String.class));
        assertEquals("com.aspectran.utils", ClassUtils.getPackageName(ClassUtilsTest.class));
        assertEquals("com.aspectran.utils", ClassUtils.getPackageName(TestBean.class));
        assertEquals("com.example", ClassUtils.getPackageName("com.example.MyService$$EnhancerByCGLIB$$1234"));
        assertEquals("", ClassUtils.getPackageName("DefaultPackageClass"));
    }

    @Test
    void testIsAnnotationPresent() {
        assertTrue(ClassUtils.isAnnotationPresent(SubClass.class, SampleAnnotation.class));
        assertFalse(ClassUtils.isAnnotationPresent(String.class, SampleAnnotation.class));
    }

    @Test
    void testIsLambdaClass() {
        Runnable runnable = () -> {};
        assertTrue(ClassUtils.isLambdaClass(runnable.getClass()));
        assertFalse(ClassUtils.isLambdaClass(String.class));
    }

    static class UninitializedTarget {
        static {
            staticBlockExecuted = true;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface SampleAnnotation {
    }

    @SampleAnnotation
    static class SuperClass {
    }

    static class SubClass extends SuperClass {
    }

    public static class TestBean {
        private final String name;
        private final int age;

        public TestBean() {
            this(null, 0);
        }

        public TestBean(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }

}
