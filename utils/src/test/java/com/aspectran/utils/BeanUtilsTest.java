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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for {@link BeanUtils}, {@link BeanDescriptor}, and {@link BeanTypeUtils}.
 */
class BeanUtilsTest {

    public static class Address {
        private String city;
        private String zipcode;

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getZipcode() {
            return zipcode;
        }

        public void setZipcode(String zipcode) {
            this.zipcode = zipcode;
        }
    }

    public static class Person {
        private String name;
        private int age;
        private Address address;
        private List<String> hobbies;
        private int[] scores;
        private boolean active;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public List<String> getHobbies() {
            return hobbies;
        }

        public void setHobbies(List<String> hobbies) {
            this.hobbies = hobbies;
        }

        public int[] getScores() {
            return scores;
        }

        public void setScores(int[] scores) {
            this.scores = scores;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    public static class DeliveryOrder {
        private final Address address = new Address();

        // Intermediate property has only getter, NO setter
        public Address getAddress() {
            return address;
        }
    }

    public record PersonRecord(String name, int age, Address address) {}

    @Test
    void testSimpleProperty() throws Exception {
        Person person = new Person();
        BeanUtils.setProperty(person, "name", "John");
        BeanUtils.setProperty(person, "age", 30);
        BeanUtils.setProperty(person, "active", true);

        assertEquals("John", BeanUtils.getProperty(person, "name"));
        assertEquals(30, BeanUtils.getProperty(person, "age"));
        assertEquals(true, BeanUtils.getProperty(person, "active"));
    }

    @Test
    void testNestedProperty() throws Exception {
        Person person = new Person();
        Address address = new Address();
        address.setCity("Seoul");
        person.setAddress(address);

        assertEquals("Seoul", BeanUtils.getProperty(person, "address.city"));

        BeanUtils.setProperty(person, "address.city", "Busan");
        assertEquals("Busan", person.getAddress().getCity());

        // Auto-instantiation of intermediate nested null property
        Person person2 = new Person();
        BeanUtils.setProperty(person2, "address.city", "Incheon");
        assertEquals("Incheon", person2.getAddress().getCity());
    }

    @Test
    void testIndexedPropertyOnList() throws Exception {
        Person person = new Person();
        List<String> hobbies = new ArrayList<>();
        hobbies.add("reading");
        hobbies.add("gaming");
        person.setHobbies(hobbies);

        assertEquals("reading", BeanUtils.getProperty(person, "hobbies[0]"));
        assertEquals("gaming", BeanUtils.getProperty(person, "hobbies[1]"));

        BeanUtils.setProperty(person, "hobbies[1]", "coding");
        assertEquals("coding", person.getHobbies().get(1));
    }

    @Test
    void testIndexedPropertyOnPrimitiveArray() throws Exception {
        Person person = new Person();
        person.setScores(new int[] { 90, 85, 100 });

        assertEquals(90, BeanUtils.getProperty(person, "scores[0]"));
        assertEquals(85, BeanUtils.getProperty(person, "scores[1]"));
        assertEquals(100, BeanUtils.getProperty(person, "scores[2]"));

        BeanUtils.setProperty(person, "scores[1]", 95);
        assertEquals(95, person.getScores()[1]);
    }

    @Test
    void testRecordSupport() throws Exception {
        Address address = new Address();
        address.setCity("Tokyo");
        PersonRecord record = new PersonRecord("Alice", 25, address);

        BeanDescriptor bd = BeanDescriptor.getInstance(PersonRecord.class);
        assertTrue(bd.hasReadableProperty("name"));
        assertTrue(bd.hasReadableProperty("age"));
        assertTrue(bd.hasReadableProperty("address"));
        assertFalse(bd.hasWritableProperty("name"));

        assertEquals("Alice", BeanUtils.getProperty(record, "name"));
        assertEquals(25, BeanUtils.getProperty(record, "age"));
        assertEquals("Tokyo", BeanUtils.getProperty(record, "address.city"));
    }

    @Test
    void testHasReadableAndWritableProperty() throws Exception {
        Person person = new Person();

        assertTrue(BeanUtils.hasReadableProperty(person, "name"));
        assertTrue(BeanUtils.hasReadableProperty(person, "address"));
        assertTrue(BeanUtils.hasReadableProperty(person, "address.city"));
        assertTrue(BeanUtils.hasReadableProperty(person, "address.zipcode"));
        assertFalse(BeanUtils.hasReadableProperty(person, "address.country"));
        assertFalse(BeanUtils.hasReadableProperty(person, "unknown"));
        assertFalse(BeanUtils.hasReadableProperty(person, "unknown.property"));

        assertTrue(BeanUtils.hasWritableProperty(person, "name"));
        assertTrue(BeanUtils.hasWritableProperty(person, "address"));
        assertTrue(BeanUtils.hasWritableProperty(person, "address.city"));
        assertFalse(BeanUtils.hasWritableProperty(person, "address.country"));
        assertFalse(BeanUtils.hasWritableProperty(person, "unknown"));
        assertFalse(BeanUtils.hasWritableProperty(person, "unknown.property"));
    }

    @Test
    void testBeanTypeUtils() throws Exception {
        assertEquals(String.class, BeanTypeUtils.getClassPropertyTypeForGetter(Person.class, "name"));
        assertEquals(int.class, BeanTypeUtils.getClassPropertyTypeForGetter(Person.class, "age"));
        assertEquals(String.class, BeanTypeUtils.getClassPropertyTypeForGetter(Person.class, "address.city"));

        assertEquals(String.class, BeanTypeUtils.getClassPropertyTypeForSetter(Person.class, "name"));
        assertEquals(int.class, BeanTypeUtils.getClassPropertyTypeForSetter(Person.class, "age"));
        assertEquals(String.class, BeanTypeUtils.getClassPropertyTypeForSetter(Person.class, "address.city"));

        // DeliveryOrder has getAddress() but NO setAddress() - intermediate property is getter-only
        assertEquals(String.class, BeanTypeUtils.getClassPropertyTypeForSetter(DeliveryOrder.class, "address.city"));
        assertEquals(String.class, BeanTypeUtils.getClassPropertyTypeForSetter(PersonRecord.class, "address.city"));

        Person person = new Person();
        person.setScores(new int[] { 10, 20 });
        assertEquals(Integer.class, BeanTypeUtils.getIndexedType(person, "scores[0]"));

        List<String> list = List.of("hello", "world");
        person.setHobbies(list);
        assertEquals(String.class, BeanTypeUtils.getIndexedType(person, "hobbies[0]"));
    }

    @Test
    void testNestedPropertyOnReadOnlyIntermediate() throws Exception {
        DeliveryOrder order = new DeliveryOrder();
        assertTrue(BeanUtils.hasWritableProperty(order, "address.city"));
        BeanUtils.setProperty(order, "address.city", "Daejeon");
        assertEquals("Daejeon", order.getAddress().getCity());
        assertEquals("Daejeon", BeanUtils.getProperty(order, "address.city"));
    }

    @Test
    void testMapAndPropertiesSupport() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("title", "Aspectran");
        map.put("version", 8);

        assertEquals("Aspectran", BeanUtils.getProperty(map, "title"));
        assertEquals(8, BeanUtils.getProperty(map, "version"));

        BeanUtils.setProperty(map, "title", "Aspectran 8.x");
        assertEquals("Aspectran 8.x", map.get("title"));
    }

}
