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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link ExceptionUtils}.
 */
class ExceptionUtilsTest {

    @Test
    void testGetCause() {
        Exception cause = new IOException("IO error");
        Exception top = new RuntimeException("Runtime error", cause);

        assertSame(cause, ExceptionUtils.getCause((Throwable) top));
        assertSame(cause, ExceptionUtils.getCause(top));

        // When there is no cause, returns itself
        assertSame(cause, ExceptionUtils.getCause((Throwable) cause));
        assertSame(cause, ExceptionUtils.getCause(cause));

        // When cause is Error, getCause(Exception) returns the exception itself
        Error error = new OutOfMemoryError();
        Exception exWithError = new RuntimeException("Wrapped error", error);
        assertSame(error, ExceptionUtils.getCause((Throwable) exWithError));
        assertSame(exWithError, ExceptionUtils.getCause(exWithError));
    }

    @Test
    void testGetRootCause() {
        Exception root = new IllegalStateException("Root");
        Exception level1 = new IOException("Level 1", root);
        Exception level2 = new RuntimeException("Level 2", level1);

        assertSame(root, ExceptionUtils.getRootCause(level2));
        assertSame(root, ExceptionUtils.getRootCause(root));
    }

    @Test
    void testGetRootCauseWithCycle() {
        CustomThrowable t1 = new CustomThrowable("t1");
        CustomThrowable t2 = new CustomThrowable("t2");
        t1.setCustomCause(t2);
        t2.setCustomCause(t1);

        // Cyclic exception must not cause infinite loop
        Throwable root = ExceptionUtils.getRootCause(t1);
        assertNotNull(root);
        assertTrue(root == t1 || root == t2);
    }

    @Test
    void testGetRootCauseException() {
        Exception root = new IllegalStateException("Root");
        Exception top = new RuntimeException("Top", root);
        assertSame(root, ExceptionUtils.getRootCauseException(top));

        Error rootError = new AssertionError("Root error");
        Exception exWithRootError = new RuntimeException("Top", rootError);
        assertSame(exWithRootError, ExceptionUtils.getRootCauseException(exWithRootError));
    }

    @Test
    void testHasCause() {
        Exception root = new SQLException("Database error");
        Exception level1 = new IOException("IO error", root);
        Exception top = new RuntimeException("Top error", level1);

        assertTrue(ExceptionUtils.hasCause(top, SQLException.class));
        assertTrue(ExceptionUtils.hasCause(top, IOException.class));
        assertTrue(ExceptionUtils.hasCause(top, RuntimeException.class));
        assertTrue(ExceptionUtils.hasCause(top, Exception.class));
        assertFalse(ExceptionUtils.hasCause(top, IllegalArgumentException.class));
        assertFalse(ExceptionUtils.hasCause(null, Exception.class));

        assertTrue(ExceptionUtils.hasCause(top, IllegalArgumentException.class, SQLException.class));
        assertFalse(ExceptionUtils.hasCause(top, IllegalArgumentException.class, IllegalStateException.class));
    }

    @Test
    void testFindCause() {
        SQLException sqlEx = new SQLException("Database error");
        IOException ioEx = new IOException("IO error", sqlEx);
        RuntimeException top = new RuntimeException("Top error", ioEx);

        SQLException foundSql = ExceptionUtils.findCause(top, SQLException.class);
        assertSame(sqlEx, foundSql);

        IOException foundIo = ExceptionUtils.findCause(top, IOException.class);
        assertSame(ioEx, foundIo);

        IllegalArgumentException foundIae = ExceptionUtils.findCause(top, IllegalArgumentException.class);
        assertNull(foundIae);

        assertNull(ExceptionUtils.findCause(null, Exception.class));
    }

    @Test
    void testGetThrowableList() {
        Exception root = new SQLException("Root");
        Exception mid = new IOException("Mid", root);
        Exception top = new RuntimeException("Top", mid);

        List<Throwable> list = ExceptionUtils.getThrowableList(top);
        assertEquals(3, list.size());
        assertSame(top, list.get(0));
        assertSame(mid, list.get(1));
        assertSame(root, list.get(2));

        assertTrue(ExceptionUtils.getThrowableList(null).isEmpty());

        // Cycle test
        CustomThrowable t1 = new CustomThrowable("t1");
        CustomThrowable t2 = new CustomThrowable("t2");
        t1.setCustomCause(t2);
        t2.setCustomCause(t1);

        List<Throwable> cyclicList = ExceptionUtils.getThrowableList(t1);
        assertEquals(2, cyclicList.size());
        assertSame(t1, cyclicList.get(0));
        assertSame(t2, cyclicList.get(1));
    }

    @Test
    void testGetStackTrace() {
        Exception ex = new RuntimeException("Test message");
        String stackTrace = ExceptionUtils.getStackTrace(ex);
        assertNotNull(stackTrace);
        assertTrue(stackTrace.contains("RuntimeException: Test message"));
        assertTrue(stackTrace.contains("ExceptionUtilsTest.testGetStackTrace"));

        // Deprecated method compatibility
        assertEquals(stackTrace, ExceptionUtils.getStacktrace(ex));
    }

    @Test
    void testGetSimpleMessage() {
        Exception exWithMessage = new RuntimeException("Custom message");
        assertEquals("Custom message", ExceptionUtils.getSimpleMessage(exWithMessage));

        Exception exWithoutMessage = new RuntimeException();
        assertEquals(RuntimeException.class.getName(), ExceptionUtils.getSimpleMessage(exWithoutMessage));

        Exception root = new IllegalArgumentException("Root message");
        Exception top = new RuntimeException("Top message", root);
        assertEquals("Root message", ExceptionUtils.getRootCauseSimpleMessage(top));
    }

    @Test
    void testThrowIfError() {
        Error error = new OutOfMemoryError("OOM");
        assertThrows(OutOfMemoryError.class, () -> ExceptionUtils.throwIfError(error));

        Exception ex = new RuntimeException("Not an error");
        assertSame(ex, ExceptionUtils.throwIfError(ex));
        assertNull(ExceptionUtils.throwIfError(null));
    }

    @Test
    void testThrowIfRTE() {
        RuntimeException rte = new IllegalArgumentException("RTE");
        assertThrows(RuntimeException.class, () -> ExceptionUtils.throwIfRTE(rte));

        Exception checked = new IOException("Checked");
        assertSame(checked, ExceptionUtils.throwIfRTE(checked));
        assertNull(ExceptionUtils.throwIfRTE(null));
    }

    @Test
    void testThrowIfIOE() {
        IOException ioe = new IOException("IO");
        assertThrows(IOException.class, () -> ExceptionUtils.throwIfIOE(ioe));

        Exception other = new SQLException("SQL");
        try {
            assertSame(other, ExceptionUtils.throwIfIOE(other));
            assertNull(ExceptionUtils.throwIfIOE(null));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void testThrowRootCauseIfIOE() {
        IOException rootIoe = new IOException("Root IO");
        RuntimeException wrapped = new RuntimeException("Wrapped", rootIoe);
        assertThrows(IOException.class, () -> ExceptionUtils.throwRootCauseIfIOE(wrapped));

        SQLException rootSql = new SQLException("SQL");
        RuntimeException nonIoe = new RuntimeException("Non IO", rootSql);
        try {
            assertSame(rootSql, ExceptionUtils.throwRootCauseIfIOE(nonIoe));
            assertNull(ExceptionUtils.throwRootCauseIfIOE(null));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void testThrowAsIAE() {
        Exception checked = new SQLException("Checked");
        IllegalArgumentException iae = assertThrows(
                IllegalArgumentException.class,
                () -> ExceptionUtils.throwAsIAE(checked)
        );
        assertSame(checked, iae.getCause());
        assertEquals("Checked", iae.getMessage());

        IllegalArgumentException iaeWithMsg = assertThrows(
                IllegalArgumentException.class,
                () -> ExceptionUtils.throwAsIAE(checked, "Custom IAE message")
        );
        assertSame(checked, iaeWithMsg.getCause());
        assertEquals("Custom IAE message", iaeWithMsg.getMessage());

        // RTE is rethrown directly
        IllegalStateException ise = new IllegalStateException("Direct RTE");
        assertThrows(IllegalStateException.class, () -> ExceptionUtils.throwAsIAE(ise));

        // Error is rethrown directly
        AssertionError err = new AssertionError("Direct Error");
        assertThrows(AssertionError.class, () -> ExceptionUtils.throwAsIAE(err));
    }

    @Test
    void testUnwrapAndThrowAsIAE() {
        SQLException root = new SQLException("Root SQL");
        Exception wrapped = new Exception("Wrapped", root);

        IllegalArgumentException iae = assertThrows(
                IllegalArgumentException.class,
                () -> ExceptionUtils.unwrapAndThrowAsIAE(wrapped)
        );
        assertSame(root, iae.getCause());

        IllegalArgumentException iaeWithMsg = assertThrows(
                IllegalArgumentException.class,
                () -> ExceptionUtils.unwrapAndThrowAsIAE(wrapped, "Custom message")
        );
        assertSame(root, iaeWithMsg.getCause());
        assertEquals("Custom message", iaeWithMsg.getMessage());
    }

    @Test
    void testUnwrapThrowable() {
        assertNull(ExceptionUtils.unwrapThrowable(null));

        Exception original = new IllegalArgumentException("Original");
        InvocationTargetException ite = new InvocationTargetException(original);
        UndeclaredThrowableException ute = new UndeclaredThrowableException(ite);
        ExecutionException ee = new ExecutionException(ute);

        assertSame(original, ExceptionUtils.unwrapThrowable(ee));
        assertSame(original, ExceptionUtils.unwrapThrowable(ute));
        assertSame(original, ExceptionUtils.unwrapThrowable(ite));
        assertSame(original, ExceptionUtils.unwrapThrowable(original));

        // Empty execution exception without cause
        ExecutionException emptyEe = new ExecutionException("Empty", null);
        assertSame(emptyEe, ExceptionUtils.unwrapThrowable(emptyEe));
    }

    private static class CustomThrowable extends Throwable {
        private Throwable customCause;

        public CustomThrowable(String message) {
            super(message);
        }

        public void setCustomCause(Throwable cause) {
            this.customCause = cause;
        }

        @Override
        public Throwable getCause() {
            return (customCause != null ? customCause : super.getCause());
        }
    }

}
