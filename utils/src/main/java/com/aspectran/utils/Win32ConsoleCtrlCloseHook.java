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

import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

public class Win32ConsoleCtrlCloseHook implements StdCallLibrary.StdCallCallback {

    private static final Logger logger = LoggerFactory.getLogger(Win32ConsoleCtrlCloseHook.class);

    private static final boolean IS_WIN = System.getProperty("os.name").toLowerCase().contains("win");

    private static final int CTRL_CLOSE_EVENT = 2;

    private final Thread hook;

    private Win32ConsoleCtrlCloseHook(Thread hook) {
        this.hook = hook;
    }

    public boolean callback(long dwCtrlType) {
        if ((int)dwCtrlType == CTRL_CLOSE_EVENT) {
            if (logger.isDebugEnabled()) {
                logger.debug("Win32ConsoleCtrlHandler receives event " + dwCtrlType);
            }
            hook.start();
            while (true) {
                try {
                    hook.join();
                    break;
                } catch (InterruptedException ignored) {
                }
            }
            return true;
        }
        return false;
    }

    private Win32ConsoleCtrlCloseHook register() {
        SetConsoleCtrlHandler(this, true);
        return this;
    }

    public void release() {
        SetConsoleCtrlHandler(this, false);
    }

    private native boolean SetConsoleCtrlHandler(StdCallLibrary.StdCallCallback handler, boolean add);

    @Nullable
    public static Win32ConsoleCtrlCloseHook register(Thread hook) {
        if (!IS_WIN) {
            return null;
        }
        try {
            Native.register("kernel32");
            if (logger.isDebugEnabled()) {
                logger.debug("Windows/Kernel32 library loaded");
            }
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
