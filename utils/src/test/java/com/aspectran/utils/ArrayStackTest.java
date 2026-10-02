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

import java.util.EmptyStackException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrayStackTest {

    @Test
    void testPushAndPop() {
        ArrayStack<String> stack = new ArrayStack<>();
        assertTrue(stack.empty());
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());

        stack.push("first");
        stack.push("second");
        stack.push("third");

        assertFalse(stack.empty());
        assertEquals(3, stack.size());
        assertEquals("third", stack.pop());
        assertEquals("second", stack.pop());
        assertEquals("first", stack.pop());
        assertTrue(stack.empty());

        assertThrows(EmptyStackException.class, stack::pop);
    }

    @Test
    void testPeek() {
        ArrayStack<String> stack = new ArrayStack<>();
        assertThrows(EmptyStackException.class, stack::peek);

        stack.push("A");
        stack.push("B");
        stack.push("C");

        assertEquals("C", stack.peek());
        assertEquals(3, stack.size());

        assertEquals("C", stack.peek(0));
        assertEquals("B", stack.peek(1));
        assertEquals("A", stack.peek(2));

        assertThrows(EmptyStackException.class, () -> stack.peek(3));
        assertThrows(EmptyStackException.class, () -> stack.peek(-1));
    }

    @Test
    void testPeekAndPopWithType() {
        ArrayStack<Object> stack = new ArrayStack<>();
        stack.push("text");
        stack.push(null);
        stack.push(123);
        stack.push(45.67);

        // Polymorphic lookup (Double and Integer are Numbers)
        Double d = stack.peek(Double.class);
        assertEquals(45.67, d);

        Number num = stack.peek(Number.class);
        assertEquals(45.67, num);

        Integer i = stack.peek(Integer.class);
        assertEquals(123, i);

        String s = stack.peek(String.class);
        assertEquals("text", s);

        // Pop by type
        Integer poppedInt = stack.pop(Integer.class);
        assertEquals(123, poppedInt);
        assertEquals(3, stack.size());

        // Null should be safely skipped without NPE
        assertThrows(EmptyStackException.class, () -> stack.peek(Integer.class));
        assertThrows(EmptyStackException.class, () -> stack.peek(Boolean.class));
    }

    @Test
    void testPopWithIndex() {
        ArrayStack<String> stack = new ArrayStack<>();
        stack.push("A");
        stack.push("B");
        stack.push("C");

        assertEquals("B", stack.pop(1)); // Remove second from top
        assertEquals(2, stack.size());
        assertEquals("C", stack.peek());

        assertThrows(EmptyStackException.class, () -> stack.pop(2));
        assertThrows(EmptyStackException.class, () -> stack.pop(-1));
    }

    @Test
    void testPeekOrNullAndPopOrNull() {
        ArrayStack<Object> stack = new ArrayStack<>();
        assertNull(stack.peekOrNull());
        assertNull(stack.peekOrNull(0));
        assertNull(stack.peekOrNull(-1));
        assertNull(stack.peekOrNull(String.class));
        assertNull(stack.popOrNull());
        assertNull(stack.popOrNull(0));
        assertNull(stack.popOrNull(-1));
        assertNull(stack.popOrNull(String.class));

        stack.push("first");
        stack.push(100);

        assertEquals(100, stack.peekOrNull());
        assertEquals(100, stack.peekOrNull(0));
        assertEquals("first", stack.peekOrNull(1));
        assertNull(stack.peekOrNull(2));
        assertNull(stack.peekOrNull(-1));
        assertEquals("first", stack.peekOrNull(String.class));
        assertNull(stack.peekOrNull(Double.class));

        assertEquals(100, stack.popOrNull());
        assertEquals("first", stack.popOrNull(String.class));
        assertNull(stack.popOrNull());
        assertTrue(stack.empty());
    }

    @Test
    void testUpdate() {
        ArrayStack<String> stack = new ArrayStack<>();
        assertThrows(EmptyStackException.class, () -> stack.update("new"));
        assertThrows(EmptyStackException.class, () -> stack.update(0, "new"));

        stack.push("A");
        stack.push("B");

        assertEquals("B", stack.update("B_updated"));
        assertEquals("B_updated", stack.peek());

        assertEquals("A", stack.update(1, "A_updated"));
        assertEquals("A_updated", stack.peek(1));

        assertThrows(EmptyStackException.class, () -> stack.update(2, "invalid"));
        assertThrows(EmptyStackException.class, () -> stack.update(-1, "invalid"));
    }

    @Test
    void testSearch() {
        ArrayStack<String> stack = new ArrayStack<>();
        stack.push("first");
        stack.push("second");
        stack.push(null);
        stack.push("third");

        assertEquals(1, stack.search("third"));
        assertEquals(2, stack.search(null));
        assertEquals(3, stack.search("second"));
        assertEquals(4, stack.search("first"));
        assertEquals(-1, stack.search("non-existent"));
    }

    @Test
    void testCollectionConstructorAndClone() {
        List<String> list = List.of("alpha", "beta", "gamma");
        ArrayStack<String> stack = new ArrayStack<>(list);

        assertEquals(3, stack.size());
        assertEquals("gamma", stack.peek());

        ArrayStack<String> cloned = stack.clone();
        assertNotSame(stack, cloned);
        assertEquals(stack, cloned);
        assertEquals("gamma", cloned.pop());
        assertEquals(3, stack.size());
        assertEquals(2, cloned.size());
    }

}
