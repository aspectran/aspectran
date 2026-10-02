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

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Win32ConsoleCtrlCloseHookTest {

    @Test
    void testEventConstants() {
        assertEquals(0, Win32ConsoleCtrlCloseHook.CTRL_C_EVENT);
        assertEquals(1, Win32ConsoleCtrlCloseHook.CTRL_BREAK_EVENT);
        assertEquals(2, Win32ConsoleCtrlCloseHook.CTRL_CLOSE_EVENT);
        assertEquals(5, Win32ConsoleCtrlCloseHook.CTRL_LOGOFF_EVENT);
        assertEquals(6, Win32ConsoleCtrlCloseHook.CTRL_SHUTDOWN_EVENT);
    }

    @Test
    void testRegisterWithNullThread() {
        assertThrows(IllegalArgumentException.class, () -> Win32ConsoleCtrlCloseHook.register(null));
    }

    @Test
    void testRegisterOnCurrentOS() {
        Thread thread = new Thread(() -> {});
        Win32ConsoleCtrlCloseHook hook = Win32ConsoleCtrlCloseHook.register(thread);
        boolean isWindows = System.getProperty("os.name", "")
                .toLowerCase(Locale.ROOT).contains("win");
        if (!isWindows) {
            assertNull(hook);
        } else {
            // On Windows, if JNA is present it returns non-null
            if (hook != null) {
                hook.close();
            }
        }
    }

}
