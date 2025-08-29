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

import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * Provides utilities for manipulating and examining {@link Throwable} objects.
 *
 * <p>Created: 2017. 10. 7.</p>
 *
 * @since 5.0.0
 */
public class ExceptionUtils {

    /**
     * Returns the cause of the specified throwable. If the cause does not exist,
     * the specified throwable is returned.
     * <p>This is a "shallow" get, not a deep root cause search.</p>
     * @param t the throwable to get the cause of, may not be null
     * @return the cause of the throwable, or the throwable itself if null
     */
    public static Throwable getCause(@NonNull Throwable t) {
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
    public static Exception getCause(@NonNull Exception e) {
        Throwable cause = e.getCause();
        return (cause instanceof Exception ? (Exception)cause : e);
    }

    /**
     * Finds the "root cause" of a throwable, the innermost of a chain of wrapped exceptions.
     * @param t the throwable to inspect, may not be null
     * @return the root cause of the throwable
     */
    public static Throwable getRootCause(@NonNull Throwable t) {
        while (t.getCause() != null) {
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
    public static Exception getRootCauseException(@NonNull Exception e) {
        Throwable cause = getRootCause(e);
        if (cause instanceof Exception ex) {
            return ex;
        } else {
            return e;
        }
    }

    /**
     * Tests if the throwable's causal chain contains a wrapped exception of the given type.
     * @param chain the root of a throwable causal chain
     * @param type the exception type to test for
     * @return true if the causal chain contains a cause of the given type, false otherwise
     */
    public static boolean hasCause(Throwable chain, Class<? extends Throwable> type) {
        if (chain == null) {
            return false;
        }
        if (type.isInstance(chain)) {
            return true;
        }
        return hasCause(chain.getCause(), type);
    }

    /**
     * Tests if the throwable's causal chain contains a wrapped exception of any of the given types.
     * @param chain the root of a throwable causal chain
     * @param type the first exception type to test for
     * @param more additional exception types to test for
     * @return true if the causal chain contains a cause of any of the given types, false otherwise
     */
    @SafeVarargs
    public static boolean hasCause(Throwable chain, Class<? extends Throwable> type, Class<? extends Throwable>... more) {
        if (hasCause(chain, type)) {
            return true;
        } else if (more.length == 0) {
            return false;
        }
        for (Class<? extends Throwable> one : more) {
            if (hasCause(chain, one)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the stack trace from a {@link Throwable} as a String.
     * <p>The result of this method may vary by JDK version as this method
     * uses {@link Throwable#printStackTrace(java.io.PrintWriter)}.
     * On JDK 1.3 and earlier, the cause exception will not be shown
     * unless the specified throwable alters its printStackTrace behavior.</p>
     * @param t the {@code Throwable} to be examined
     * @return the stack trace as generated by the exception's
     *      {@code printStackTrace(PrintWriter)} method
     */
    public static String getStacktrace(@NonNull Throwable t) {
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
    public static String getSimpleMessage(@NonNull Throwable t) {
        return (t.getMessage() != null ? t.getMessage() : t.toString());
    }

    /**
     * Returns a simple message for the root cause of a {@link Throwable}.
     * @param t the {@code Throwable} to get the root cause message for
     * @return the message of the root cause, or its class name if the message is null
     */
    public static String getRootCauseSimpleMessage(@NonNull Throwable t) {
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
    public static Throwable throwIfError(Throwable t) {
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
    public static Throwable throwIfRTE(Throwable t) {
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
    public static Throwable throwIfIOE(Throwable t) throws IOException {
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
    public static Throwable throwRootCauseIfIOE(Throwable t) throws IOException {
        return throwIfIOE(getRootCause(t));
    }

    /**
     * Wraps a throwable as an {@link IllegalArgumentException} if it is a checked exception.
     * Runtime exceptions and errors are re-thrown as is.
     * @param t the throwable to wrap or re-throw
     * @return never returns normally
     * @throws IllegalArgumentException for checked exceptions
     */
    public static IllegalArgumentException throwAsIAE(Throwable t) {
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
    public static IllegalArgumentException throwAsIAE(Throwable t, String msg) {
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
    public static IllegalArgumentException unwrapAndThrowAsIAE(Throwable t) {
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
    public static IllegalArgumentException unwrapAndThrowAsIAE(Throwable t, String msg) {
        throw throwAsIAE(getRootCause(t), msg);
    }

    /**
     * Unwraps a throwable, specifically handling common wrapper exceptions like
     * {@link InvocationTargetException} and {@link UndeclaredThrowableException}.
     * This method repeatedly unwraps the throwable until it is no longer a known wrapper.
     * @param t the exception to unwrap
     * @return the unwrapped, underlying throwable
     */
    public static Throwable unwrapThrowable(Throwable t) {
        Throwable t2 = t;
        while (true) {
            if (t2 instanceof InvocationTargetException e) {
                t2 = e.getTargetException();
            } else if (t2 instanceof UndeclaredThrowableException e) {
                t2 = e.getUndeclaredThrowable();
            } else {
                return t2;
            }
        }
    }

}
