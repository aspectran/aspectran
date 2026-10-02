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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Provides utilities for manipulating and examining {@link Throwable} objects.
 *
 * <p>Created: 2017. 10. 7.</p>
 *
 * @since 5.0.0
 */
public class ExceptionUtils {

    /**
     * This class cannot be instantiated.
     */
    private ExceptionUtils() {
    }

    /**
     * Returns the cause of the specified throwable. If the cause does not exist,
     * the specified throwable is returned.
     * <p>This is a "shallow" get, not a deep root cause search.</p>
     * @param t the throwable to get the cause of, may not be null
     * @return the cause of the throwable, or the throwable itself if null
     */
    public static @NonNull Throwable getCause(@NonNull Throwable t) {
        Assert.notNull(t, "t must not be null");
        return (t.getCause() != null ? t.getCause() : t);
    }

    /**
     * Returns the cause of the specified exception. If the cause is not an
     * {@link Exception}, or if it does not exist, the specified exception is returned.
     * This method avoids a {@link ClassCastException} if the cause is an {@link Error}.
     * @param e the exception to get the cause of, may not be null
     * @return the cause of the exception, or the exception itself if the cause is
     *      not an Exception or is null
     */
    public static @NonNull Exception getCause(@NonNull Exception e) {
        Assert.notNull(e, "e must not be null");
        Throwable cause = e.getCause();
        return (cause instanceof Exception ex ? ex : e);
    }

    /**
     * Finds the "root cause" of a throwable, the innermost of a chain of wrapped exceptions.
     * Handles cyclic cause chains safely.
     * @param t the throwable to inspect, may not be null
     * @return the root cause of the throwable
     */
    public static @NonNull Throwable getRootCause(@NonNull Throwable t) {
        Assert.notNull(t, "t must not be null");
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        visited.add(t);
        while (t.getCause() != null && visited.add(t.getCause())) {
            t = t.getCause();
        }
        return t;
    }

    /**
     * Finds the "root cause" of an exception and returns it if it is an {@link Exception}.
     * If the root cause is not an {@link Exception} (e.g., an {@link Error}),
     * the original exception is returned.
     * @param e the exception to inspect, may not be null
     * @return the root cause if it is an exception; otherwise, the original exception
     */
    public static @NonNull Exception getRootCauseException(@NonNull Exception e) {
        Assert.notNull(e, "e must not be null");
        Throwable cause = getRootCause(e);
        return (cause instanceof Exception ex ? ex : e);
    }

    /**
     * Tests if the throwable's causal chain contains a wrapped exception of the given type.
     * Handles cyclic cause chains safely.
     * @param chain the root of a throwable causal chain
     * @param type the exception type to test for
     * @return true if the causal chain contains a cause of the given type, false otherwise
     */
    public static boolean hasCause(@Nullable Throwable chain, @NonNull Class<? extends Throwable> type) {
        return findCause(chain, type) != null;
    }

    /**
     * Tests if the throwable's causal chain contains a wrapped exception of any of the given types.
     * Handles cyclic cause chains safely.
     * @param chain the root of a throwable causal chain
     * @param type the first exception type to test for
     * @param more additional exception types to test for
     * @return true if the causal chain contains a cause of any of the given types, false otherwise
     */
    @SafeVarargs
    public static boolean hasCause(
            @Nullable Throwable chain,
            @NonNull Class<? extends Throwable> type,
            Class<? extends Throwable>... more
    ) {
        if (hasCause(chain, type)) {
            return true;
        }
        if (more != null && more.length > 0) {
            for (Class<? extends Throwable> one : more) {
                if (hasCause(chain, one)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Finds the first throwable of the specified type in the causal chain.
     * Handles cyclic cause chains safely.
     * @param <T> the type of throwable to search for
     * @param chain the root of a throwable causal chain
     * @param type the exception class to search for
     * @return the first matching throwable, or {@code null} if not found
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> @Nullable T findCause(@Nullable Throwable chain, @NonNull Class<T> type) {
        Assert.notNull(type, "type must not be null");
        if (chain == null) {
            return null;
        }
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = chain;
        while (current != null && visited.add(current)) {
            if (type.isInstance(current)) {
                return (T) current;
            }
            current = current.getCause();
        }
        return null;
    }

    /**
     * Returns the list of throwables in the causal chain from the specified throwable.
     * Handles cyclic cause chains safely.
     * @param t the throwable to inspect
     * @return the list of throwables in the causal chain
     */
    public static @NonNull List<Throwable> getThrowableList(@Nullable Throwable t) {
        if (t == null) {
            return Collections.emptyList();
        }
        List<Throwable> list = new ArrayList<>();
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = t;
        while (current != null && visited.add(current)) {
            list.add(current);
            current = current.getCause();
        }
        return list;
    }

    /**
     * Gets the stack trace from a {@link Throwable} as a String.
     * @param t the {@code Throwable} to be examined
     * @return the stack trace as generated by the exception's
     *      {@code printStackTrace(PrintWriter)} method
     */
    public static @NonNull String getStackTrace(@NonNull Throwable t) {
        Assert.notNull(t, "t must not be null");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        return sw.getBuffer().toString().trim();
    }

    /**
     * Returns a simple message for a {@link Throwable}.
     * If the message is null, the full class name of the throwable is returned.
     * @param t the {@code Throwable} to get the message for
     * @return the message, or the throwable's class name if the message is null
     */
    public static @NonNull String getSimpleMessage(@NonNull Throwable t) {
        Assert.notNull(t, "t must not be null");
        return (t.getMessage() != null ? t.getMessage() : t.toString());
    }

    /**
     * Returns a simple message for the root cause of a {@link Throwable}.
     * @param t the {@code Throwable} to get the root cause message for
     * @return the message of the root cause, or its class name if the message is null
     */
    public static @NonNull String getRootCauseSimpleMessage(@NonNull Throwable t) {
        Assert.notNull(t, "t must not be null");
        return getSimpleMessage(getRootCause(t));
    }

    /*
     **********************************************************
     * Exception handling; simple re-throw
     **********************************************************
     */

    /**
     * Checks if the argument is an {@link Error}, and if so, (re)throws it.
     * @param t the throwable to check
     * @return the throwable if it is not an Error
     * @throws Error if the throwable is an Error
     */
    public static @Nullable Throwable throwIfError(@Nullable Throwable t) {
        if (t instanceof Error e) {
            throw e;
        }
        return t;
    }

    /**
     * Checks if the argument is a {@link RuntimeException}, and if so, (re)throws it.
     * @param t the throwable to check
     * @return the throwable if it is not a RuntimeException
     * @throws RuntimeException if the throwable is a RuntimeException
     */
    public static @Nullable Throwable throwIfRTE(@Nullable Throwable t) {
        if (t instanceof RuntimeException re) {
            throw re;
        }
        return t;
    }

    /**
     * Checks if the argument is an {@link IOException}, and if so, (re)throws it.
     * @param t the throwable to check
     * @return the throwable if it is not an IOException
     * @throws IOException if the throwable is an IOException
     */
    public static @Nullable Throwable throwIfIOE(@Nullable Throwable t) throws IOException {
        if (t instanceof IOException ioe) {
            throw ioe;
        }
        return t;
    }

    /**
     * Finds the root cause of the throwable and re-throws it if it is an {@link IOException}.
     * @param t the throwable to inspect
     * @return the throwable if its root cause is not an IOException
     * @throws IOException if the root cause of the throwable is an IOException
     */
    public static @Nullable Throwable throwRootCauseIfIOE(@Nullable Throwable t) throws IOException {
        if (t == null) {
            return null;
        }
        return throwIfIOE(getRootCause(t));
    }

    /**
     * Wraps a throwable as an {@link IllegalArgumentException} if it is a checked exception.
     * Runtime exceptions and errors are re-thrown as is.
     * @param t the throwable to wrap or re-throw
     * @return never returns normally
     * @throws IllegalArgumentException for checked exceptions
     */
    public static @NonNull IllegalArgumentException throwAsIAE(@NonNull Throwable t) {
        return throwAsIAE(t, t.getMessage());
    }

    /**
     * Wraps a throwable as an {@link IllegalArgumentException} with a specified message
     * if it is a checked exception. Runtime exceptions and errors are re-thrown as is.
     * @param t the throwable to wrap or re-throw
     * @param msg the detail message for the new exception
     * @return never returns normally
     * @throws IllegalArgumentException for checked exceptions
     */
    public static @NonNull IllegalArgumentException throwAsIAE(@NonNull Throwable t, @Nullable String msg) {
        Assert.notNull(t, "t must not be null");
        throwIfRTE(t);
        throwIfError(t);
        throw new IllegalArgumentException(msg, t);
    }

    /**
     * Finds the root cause of a throwable and wraps it as an {@link IllegalArgumentException}
     * if it is a checked exception. Runtime exceptions and errors are re-thrown as is.
     * @param t the throwable to unwrap and process
     * @return never returns normally
     * @throws IllegalArgumentException for checked exceptions
     */
    public static @NonNull IllegalArgumentException unwrapAndThrowAsIAE(@NonNull Throwable t) {
        return throwAsIAE(getRootCause(t));
    }

    /**
     * Finds the root cause of a throwable and wraps it as an {@link IllegalArgumentException}
     * with a specified message if it is a checked exception. Runtime exceptions and errors
     * are re-thrown as is.
     * @param t the throwable to unwrap and process
     * @param msg the detail message for the new exception
     * @return never returns normally
     * @throws IllegalArgumentException for checked exceptions
     */
    public static @NonNull IllegalArgumentException unwrapAndThrowAsIAE(@NonNull Throwable t, @Nullable String msg) {
        return throwAsIAE(getRootCause(t), msg);
    }

    /**
     * Unwraps a throwable, specifically handling common wrapper exceptions like
     * {@link InvocationTargetException}, {@link UndeclaredThrowableException}, and
     * {@link ExecutionException}.
     * This method repeatedly unwraps the throwable until it is no longer a known wrapper.
     * Handles cyclic wrapper chains safely.
     * @param t the exception to unwrap
     * @return the unwrapped, underlying throwable
     */
    public static @Nullable Throwable unwrapThrowable(@Nullable Throwable t) {
        if (t == null) {
            return null;
        }
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = t;
        while (current != null && visited.add(current)) {
            if (current instanceof InvocationTargetException e) {
                current = e.getTargetException();
            } else if (current instanceof UndeclaredThrowableException e) {
                current = e.getUndeclaredThrowable();
            } else if (current instanceof ExecutionException e && e.getCause() != null) {
                current = e.getCause();
            } else {
                return current;
            }
        }
        return current;
    }

}
