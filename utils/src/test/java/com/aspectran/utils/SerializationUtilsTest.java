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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SerializationUtilsTest {

    static class Person implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String name;
        private final int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person)o;
            return age == person.age && Objects.equals(name, person.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }

    @Test
    void testSerializeAndDeserialize() {
        assertNull(SerializationUtils.serialize(null));
        assertNull(SerializationUtils.deserialize(null));

        Person original = new Person("John", 30);
        byte[] bytes = SerializationUtils.serialize(original);
        assertNotNull(bytes);

        Person deserialized = SerializationUtils.deserialize(bytes);
        assertNotNull(deserialized);
        assertNotSame(original, deserialized);
        assertEquals(original, deserialized);
    }

    @Test
    void testClone() {
        assertNull(SerializationUtils.clone(null));

        Person original = new Person("Alice", 25);
        Person cloned = SerializationUtils.clone(original);
        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertEquals(original, cloned);
    }

    @Test
    void testDeserializeWithClassLoader() {
        Person original = new Person("Bob", 40);
        byte[] bytes = SerializationUtils.serialize(original);
        assertNotNull(bytes);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Person deserialized = SerializationUtils.deserialize(bytes, cl);
        assertNotNull(deserialized);
        assertEquals(original, deserialized);
    }

    @Test
    void testSerializeNonSerializable() {
        Object nonSerializable = new Object();
        assertThrows(IllegalArgumentException.class, () -> SerializationUtils.serialize(nonSerializable));
    }

    @Test
    void testDeserializeCorruptBytes() {
        byte[] corrupt = new byte[] { 0, 1, 2, 3 };
        assertThrows(IllegalArgumentException.class, () -> SerializationUtils.deserialize(corrupt));
    }

}
