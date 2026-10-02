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

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A Windows-specific utility to intercept console control events (like Ctrl+C or console close).
 * <p>This class uses JNA (Java Native Access) to register a native handler that can intercept
 * {@code CTRL_CLOSE_EVENT} and other console signals, allowing a Java application to perform
 * graceful shutdown procedures even when the console window is closed directly.</p>
 * <p>It is typically used in conjunction with JVM shutdown hooks.</p>
 */
public class Win32ConsoleCtrlCloseHook implements StdCallLibrary.StdCallCallback, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Win32ConsoleCtrlCloseHook.class);

    private static final boolean IS_WIN = System.getProperty("os.name", "")
            .toLowerCase(Locale.ROOT).contains("win");

    /** Event fired when the user presses Ctrl+C. */
    public static final int CTRL_C_EVENT = 0;

    /** Event fired when the user presses Ctrl+Break. */
    public static final int CTRL_BREAK_EVENT = 1;

    /** Event fired when the user closes the console window. */
    public static final int CTRL_CLOSE_EVENT = 2;

    /** Event fired when the user logs off. */
    public static final int CTRL_LOGOFF_EVENT = 5;

    /** Event fired when the system is shutting down. */
    public static final int CTRL_SHUTDOWN_EVENT = 6;

    private static volatile boolean nativeLibraryLoaded;

    private final Thread hook;

    private final AtomicBoolean released = new AtomicBoolean(false);

    /**
     * Private constructor to create an instance associated with a specific Java thread.
     * @param hook the Java thread to be started when a console control event is received
     */
    private Win32ConsoleCtrlCloseHook(@NonNull Thread hook) {
        this.hook = hook;
    }

    /**
     * Callback method invoked by the native Windows API when a console control event occurs.
     * <p>If the event is {@code CTRL_CLOSE_EVENT}, {@code CTRL_LOGOFF_EVENT}, or {@code CTRL_SHUTDOWN_EVENT},
     * it starts the associated Java thread (typically a JVM shutdown hook) and waits for it to complete.</p>
     * @param dwCtrlType the type of the console control event
     * @return {@code true} if the event was handled, {@code false} otherwise
     */
    public boolean callback(long dwCtrlType) {
        int event = (int)dwCtrlType;
        if (event == CTRL_CLOSE_EVENT || event == CTRL_LOGOFF_EVENT || event == CTRL_SHUTDOWN_EVENT) {
            if (logger.isDebugEnabled()) {
                logger.debug("Win32ConsoleCtrlHandler receives event {}", dwCtrlType);
            }
            if (!hook.isAlive() && hook.getState() == Thread.State.NEW) {
                try {
                    hook.start();
                } catch (IllegalThreadStateException ignored) {
                }
            }
            boolean interrupted = false;
            while (hook.isAlive()) {
                try {
                    hook.join();
                    break;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            return true;
        }
        return false;
    }

    /**
     * Registers this instance as a console control handler with the Windows API.
     * @return this instance for chaining
     */
    private Win32ConsoleCtrlCloseHook register() {
        SetConsoleCtrlHandler(this, true);
        return this;
    }

    /**
     * Unregisters this instance as a console control handler.
     */
    public void release() {
        if (released.compareAndSet(false, true)) {
            try {
                SetConsoleCtrlHandler(this, false);
            } catch (Throwable t) {
                logger.warn("Failed to unregister Windows console control handler", t);
            }
        }
    }

    @Override
    public void close() {
        release();
    }

    /**
     * Native method to set or remove a console control handler.
     * @param handler the handler to set or remove
     * @param add {@code true} to add the handler, {@code false} to remove it
     * @return {@code true} if the handler was successfully set or removed, {@code false} otherwise
     */
    private native boolean SetConsoleCtrlHandler(StdCallLibrary.StdCallCallback handler, boolean add);

    private static synchronized void loadNativeLibrary() {
        if (!nativeLibraryLoaded) {
            Native.register("kernel32");
            nativeLibraryLoaded = true;
            if (logger.isDebugEnabled()) {
                logger.debug("Windows/Kernel32 library loaded");
            }
        }
    }

    /**
     * Registers a new {@code Win32ConsoleCtrlCloseHook} to intercept console control events.
     * <p>This method will only register the hook if the operating system is Windows and
     * JNA is available.</p>
     * @param hook the Java thread (typically a shutdown hook) to be started upon console events
     * @return the registered hook instance, or {@code null} if not on Windows or JNA is unavailable
     */
    @Nullable
    public static Win32ConsoleCtrlCloseHook register(@NonNull Thread hook) {
        Assert.notNull(hook, "hook must not be null");
        if (!IS_WIN) {
            return null;
        }
        try {
            loadNativeLibrary();
            Win32ConsoleCtrlCloseHook win32ConsoleCtrlCloseHook = new Win32ConsoleCtrlCloseHook(hook);
            return win32ConsoleCtrlCloseHook.register();
        } catch (NoClassDefFoundError e) {
            logger.warn("JNA not found. Native methods and handlers will be disabled.");
        } catch (UnsatisfiedLinkError e) {
            logger.warn("Unable to link Windows/Kernel32 library. Native methods and handlers will be disabled.");
        }
        return null;
    }

}
