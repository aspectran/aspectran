/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.web.support.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 12/20/23</p>
 */
class JavaScriptUtilsTest {

    @Test
    public void escape() {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        sb.append('\'');
        sb.append('\\');
        sb.append('/');
        sb.append('\t');
        sb.append('\n');
        sb.append('\r');
        sb.append('\f');
        sb.append('\b');
        sb.append('\013');
        assertEquals(JavaScriptUtils.javaScriptEscape(sb.toString()), "\\\"\\'\\\\\\/\\t\\n\\n\\f\\b\\v");
    }

    @Test
    public void escapePsLsLineTerminators() {
        StringBuilder sb = new StringBuilder();
        sb.append('\u2028');
        sb.append('\u2029');
        String result = JavaScriptUtils.javaScriptEscape(sb.toString());
        assertEquals(result, "\\u2028\\u2029");
    }

    @Test
    public void escapeLessThanGreaterThanSigns() {
        assertEquals(JavaScriptUtils.javaScriptEscape("<>"), "\\u003C\\u003E");
    }

}
