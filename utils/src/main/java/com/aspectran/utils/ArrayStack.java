/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import java.util.ArrayList;
import java.util.EmptyStackException;

/**
 * <p>This class is a clone of org.apache.commons.collections4.ArrayStack</p>
 *
 * An implementation of the {@link java.util.Stack} API that is based on an
 * <code>ArrayList</code> instead of a <code>Vector</code>, so it is not
 * synchronized to protect against multi-threaded access.  The implementation
 * is therefore operates faster in environments where you do not need to
 * worry about multiple thread contention.
 * <p>
 * The removal order of an <code>ArrayStack</code> is based on insertion
 * order: The most recently added element is removed first.  The iteration
 * order is <i>not</i> the same as the removal order.  The iterator returns
 * elements from the bottom up, whereas the {@link #pop()} method removes
 * them from the top down.</p>
 * <p>
 * Unlike <code>Stack</code>, <code>ArrayStack</code> accepts null entries.<p>
 *
 * @param <E> the type of elements in this list
 * @see java.util.Stack
 */
public class ArrayStack<E> extends ArrayList<E> {

    /** Ensure serialization compatibility. */
    private static final long serialVersionUID = 4952513157310856314L;

    /**
     * Constructs a new empty <code>ArrayStack</code>. The initial size
     * is controlled by <code>ArrayList</code> and is currently 10.
     */
    public ArrayStack() {
        super();
    }

    /**
     * Constructs a new empty <code>ArrayStack</code> with an initial size.
     * @param initialSize the initial size to use
     * @throws IllegalArgumentException if the specified initial size is negative
     */
    public ArrayStack(int initialSize) {
        super(initialSize);
    }

    /**
     * Returns the top item off of this stack without removing it.
     * @return the top item on the stack
     * @throws EmptyStackException if the stack is empty
     */
    public E peek() throws EmptyStackException {
        int n = size();
        if (n == 0) {
            throw new EmptyStackException();
        }
        return get(n - 1);
    }

    /**
     * Returns the n'th item down (zero-relative) from the top of this
     * stack without removing it.
     * @param n the number of items down to go
     * @return the n'th item on the stack, zero relative
     * @throws EmptyStackException if there are not enough items on the
     *  stack to satisfy this request
     */
    public E peek(int n) throws EmptyStackException {
        int m = (size() - n) - 1;
        if (m < 0) {
            throw new EmptyStackException();
        }
        return get(m);
    }

    public E peek(Class<?> target) throws EmptyStackException {
        for (int i = size() - 1; i >= 0; i--) {
            E item = get(i);
            if (item.getClass().equals(target)) {
                return item;
            }
        }
        throw new EmptyStackException();
    }

    /**
     * Pops the top item off of this stack and return it.
     * @return the top item on the stack
     * @throws EmptyStackException if the stack is empty
     */
    public E pop() throws EmptyStackException {
        int n = size();
        if (n == 0) {
            throw new EmptyStackException();
        }
        return remove(n - 1);
    }

    /**
     * Pushes a new item onto the top of this stack. The pushed item is also
     * returned. This is equivalent to calling <code>add</code>.
     * @param item the item to be added
     * @return the item just pushed
     */
    public E push(E item) {
        add(item);
        return item;
    }

    /**
     * Replaces the top item of this stack with another item without removing it.
     * @return the top item previously on the stack
     * @throws EmptyStackException if the stack is empty
     */
    public E update(E item) throws EmptyStackException {
        int n = size();
        if (n == 0) {
            throw new EmptyStackException();
        }
        return set(n - 1, item);
    }

    /**
     * Replaces the n'th item down (zero-relative) from the top of this
     * stack with another item without removing it.
     * @param n the number of items down to go
     * @return the n'th item previously on the stack, zero relative
     * @throws EmptyStackException if there are not enough items on the
     *      stack to satisfy this request
     */
    public E update(int n, E item) throws EmptyStackException {
        int m = (size() - n) - 1;
        if (m < 0) {
            throw new EmptyStackException();
        }
        return set(m, item);
    }

    /**
     * Returns the one-based position of the distance from the top that the
     * specified object exists on this stack, where the top-most element is
     * considered to be at distance <code>1</code>.  If the object is not
     * present on the stack, return <code>-1</code> instead.  The
     * <code>equals()</code> method is used to compare to the items
     * in this stack.
     * @param object the object to be searched for
     * @return the 1-based depth into the stack of the object, or -1 if not found
     */
    public int search(E object) {
        int i = size() - 1; // Current index
        int n = 1; // Current distance
        while (i >= 0) {
            Object current = get(i);
            if ((object == null && current == null) ||
                    (object != null && object.equals(current))) {
                return n;
            }
            i--;
            n++;
        }
        return -1;
    }

}
