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

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Wraps multiple {@link Throwable} objects into a single {@link Exception}.
 * <p>This class allows operations that may throw multiple exceptions (e.g., shutting down
 * multiple components) to report all failures in a single, aggregated exception.</p>
 */
public class MultiException extends Exception {

    @Serial
    private static final long serialVersionUID = 2675035125716434028L;

    private List<Throwable> nested;

    /**
     * Constructs a new MultiException with a default message.
     */
    public MultiException() {
        super("Multiple exceptions");
    }

    /**
     * Adds a throwable to the list of nested exceptions.
     * <p>If the added throwable is another {@code MultiException}, its nested exceptions
     * are flattened into this one.</p>
     * @param e the throwable to add, must not be null
     */
    public void add(Throwable e) {
        if (e == null) {
            throw new IllegalArgumentException();
        }
        if (nested == null) {
            initCause(e);
            nested = new ArrayList<>();
        } else {
            addSuppressed(e);
        }
        if (e instanceof MultiException me) {
            nested.addAll(me.nested);
        } else {
            nested.add(e);
        }
    }

    /**
     * Returns the number of nested exceptions.
     * @return the number of nested exceptions
     */
    public int size() {
        return (nested == null ? 0 : nested.size());
    }

    /**
     * Returns the list of nested throwables.
     * @return an unmodifiable list of nested throwables
     */
    public List<Throwable> getThrowables() {
        return (nested == null ? Collections.emptyList() : nested);
    }

    /**
     * Returns the throwable at the specified index.
     * @param i the index of the throwable to return
     * @return the throwable at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public Throwable getThrowable(int i) {
        return nested.get(i);
    }

    /**
     * Throws this {@code MultiException} if it contains one or more exceptions.
     * <p>If it contains exactly one exception, that single exception is thrown directly.
     * If it contains multiple exceptions, this {@code MultiException} itself is thrown.
     * If it is empty, this method does nothing.</p>
     * @throws Exception the single nested exception, or this {@code MultiException}
     */
    public void ifExceptionThrow() throws Exception {
        if (nested == null || nested.isEmpty()) {
            return;
        }
        if (nested.size() == 1) {
            Throwable th = nested.getFirst();
            if (th instanceof Error) {
                throw (Error)th;
            }
            if (th instanceof Exception) {
                throw (Exception)th;
            }
        }
        throw this;
    }

    /**
     * Throws a {@link RuntimeException} if this {@code MultiException} contains one or more exceptions.
     * <p>If it contains a single {@link Error} or {@link RuntimeException}, it is thrown directly.
     * If it contains a single checked exception, it is wrapped in a {@code RuntimeException} and thrown.
     * If it contains multiple exceptions, this {@code MultiException} is wrapped in a {@code RuntimeException}
     * and thrown. If it is empty, this method does nothing.</p>
     * @throws Error if this exception contains exactly one {@link Error}
     * @throws RuntimeException if this exception contains one or more throwables
     */
    public void ifExceptionThrowRuntime() throws Error {
        if (nested == null || nested.isEmpty()) {
            return;
        }
        if (nested.size() == 1) {
            Throwable th = nested.getFirst();
            if (th instanceof Error) {
                throw (Error)th;
            } else if (th instanceof RuntimeException) {
                throw (RuntimeException)th;
            } else {
                throw new RuntimeException(th);
            }
        }
        throw new RuntimeException(this);
    }

    /**
     * Throws this {@code MultiException} if it contains any nested exceptions.
     * <p>Unlike {@link #ifExceptionThrow()}, this method always throws the
     * {@code MultiException} itself, even if there is only one nested exception.</p>
     * @throws MultiException if there are any nested exceptions
     */
    public void ifExceptionThrowMulti() throws MultiException {
        if (nested != null && !nested.isEmpty()) {
            throw this;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(MultiException.class.getSimpleName());
        if (nested == null || nested.size() <= 0) {
            sb.append("[]");
        } else {
            sb.append(nested);
        }
        return sb.toString();
    }

}
