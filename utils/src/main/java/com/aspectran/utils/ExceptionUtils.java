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
 * Provides utilities for manipulating and examining Throwable objects.
 *
 * <p>Created: 2017. 10. 7.</p>
 *
 * @since 5.0.0
 */
public class ExceptionUtils {

    public static Throwable getCause(@NonNull Throwable t) {
        return (t.getCause() != null ? t.getCause() : t);
    }

    /**
     * Method that can be used to find the "root cause", innermost
     * of chained (wrapped) exceptions.
     * @param t the Throwable to possibly propagate
     * @return the root cause
     */
    @NonNull
    public static Throwable getRootCause(@NonNull Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    @NonNull
    public static Exception getRootCauseException(@NonNull Exception e) {
        Throwable cause = getRootCause(e);
        if (cause instanceof Exception ex) {
            return ex;
        } else {
            return e;
        }
    }

    /**
     * Tests if the throwable's causal chain have a wrapped exception of the given type.
     * @param chain the root of a Throwable causal chain
     * @param type the exception type to test
     * @return true, if chain is wrapping a cause of the given type
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
     * Gets the stack trace from a Throwable as a String.
     * <p>The result of this method vary by JDK version as this method
     * uses {@link Throwable#printStackTrace(java.io.PrintWriter)}.
     * On JDK1.3 and earlier, the cause exception will not be shown
     * unless the specified throwable alters printStackTrace.</p>
     * @param t the {@code Throwable} to be examined
     * @return the stack trace as generated by the exception's
     *      {@code printStackTrace(PrintWriter)} method
     */
    @NonNull
    public static String getStacktrace(@NonNull Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        return sw.getBuffer().toString().trim();
    }

    @NonNull
    public static String getSimpleMessage(@NonNull Throwable t) {
        return (t.getMessage() != null ? t.getMessage() : t.toString());
    }

    @NonNull
    public static String getRootCauseSimpleMessage(@NonNull Throwable t) {
        return getSimpleMessage(getRootCause(t));
    }

    /*
     **********************************************************
     * Exception handling; simple re-throw
     **********************************************************
     */

    /**
     * Helper method that will check if argument is an {@link Error},
     * and if so, (re)throw it; otherwise just return.
     * @param t the Throwable to possibly propagate
     * @return the Throwable
     */
    public static Throwable throwIfError(Throwable t) {
        if (t instanceof Error e) {
            throw e;
        }
        return t;
    }

    /**
     * Helper method that will check if argument is an {@link RuntimeException},
     * and if so, (re)throw it; otherwise just return.
     * @param t the Throwable to possibly propagate
     * @return the Throwable
     */
    public static Throwable throwIfRTE(Throwable t) {
        if (t instanceof RuntimeException re) {
            throw re;
        }
        return t;
    }

    /**
     * Helper method that will check if argument is an {@link IOException},
     * and if so, (re)throw it; otherwise just return.
     * @param t the Throwable to possibly propagate
     * @return the Throwable
     * @throws IOException rethrow the IOException
     */
    public static Throwable throwIfIOE(Throwable t) throws IOException {
        if (t instanceof IOException ioe) {
            throw ioe;
        }
        return t;
    }

    /**
     * Method that works like by calling {@link #getRootCause} and then
     * either throwing it (if instanceof {@link IOException}), or return.
     * @param t the Throwable to possibly propagate
     * @return the Throwable
     * @throws IOException rethrow the IOException
     */
    public static Throwable throwRootCauseIfIOE(Throwable t) throws IOException {
        return throwIfIOE(getRootCause(t));
    }

    /**
     * Method that will wrap 't' as an {@link IllegalArgumentException} if it
     * is a checked exception; otherwise (runtime exception or error) throw as is.
     * @param t the Throwable to possibly propagate
     */
    public static IllegalArgumentException throwAsIAE(Throwable t) {
        return throwAsIAE(t, t.getMessage());
    }

    /**
     * Method that will wrap 't' as an {@link IllegalArgumentException} (and with
     * specified message) if it is a checked exception; otherwise (runtime exception or error)
     * throw as is.
     * @param t the Throwable to possibly propagate
     * @param msg the detail message
     */
    public static IllegalArgumentException throwAsIAE(Throwable t, String msg) {
        throwIfRTE(t);
        throwIfError(t);
        throw new IllegalArgumentException(msg, t);
    }

    /**
     * Method that will locate the innermost exception for given Throwable;
     * and then wrap it as an {@link IllegalArgumentException} if it
     * is a checked exception; otherwise (runtime exception or error) throw as is.
     * @param t the Throwable to possibly propagate
     */
    public static IllegalArgumentException unwrapAndThrowAsIAE(Throwable t) {
        return throwAsIAE(getRootCause(t));
    }

    /**
     * Method that will locate the innermost exception for given Throwable;
     * and then wrap it as an {@link IllegalArgumentException} if it
     * is a checked exception; otherwise (runtime exception or error) throw as is.
     * @param t the Throwable to possibly propagate
     * @param msg the detail msg
     */
    public static IllegalArgumentException unwrapAndThrowAsIAE(Throwable t, String msg) {
        throw throwAsIAE(getRootCause(t), msg);
    }

    /**
     * Examines a Throwable object and gets it's root cause
     * @param t the exception to examine
     * @return the root cause
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
