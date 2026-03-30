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
package com.aspectran.utils.apon;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.aspectran.utils.apon.AponFormat.SYSTEM_NEW_LINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Test cases for AponWriter rendering styles.
 */
class AponWriterStyleTest {

    @Test
    void testTextInSingleLineStyle() throws IOException {
        Parameters params = new VariableParameters();
        params.putValue("key1", "val1");
        params.putValue("text", "line1" + SYSTEM_NEW_LINE + "line2" + SYSTEM_NEW_LINE + "line3");
        params.setRenderStyle(AponRenderStyle.SINGLE_LINE);

        String result = new AponWriter().write(params).toString();
        System.out.println("SINGLE_LINE style with TEXT:\n" + result);
        
        assertFalse(result.contains(SYSTEM_NEW_LINE));
        assertFalse(result.contains("|"));
    }

    @Test
    void testTextInCompactStyle() throws IOException {
        Parameters params = new VariableParameters();
        params.putValue("key1", "val1");
        params.putValue("text", "line1" + SYSTEM_NEW_LINE + "line2" + SYSTEM_NEW_LINE + "line3");
        params.setRenderStyle(AponRenderStyle.COMPACT);

        String result = new AponWriter().write(params).toString();
        System.out.println("COMPACT style with TEXT:\n" + result);

        assertFalse(result.contains(SYSTEM_NEW_LINE));
        assertFalse(result.contains("|"));
    }

    @Test
    void testStyleConversions() throws IOException, AponParseException {
        String apon = "key1: val1" + SYSTEM_NEW_LINE +
                "text: (" + SYSTEM_NEW_LINE +
                "  |line1" + SYSTEM_NEW_LINE +
                "  |line2" + SYSTEM_NEW_LINE +
                ")" + SYSTEM_NEW_LINE;
        Parameters params = AponParser.parse(apon);

        // Convert to COMPACT
        params.setRenderStyle(AponRenderStyle.COMPACT);
        String compact = new AponWriter().write(params).toString();
        System.out.println("Compact:\n" + compact);

        // Parse compact back
        Parameters params2 = AponParser.parse(compact);
        assertEquals("val1", params2.getString("key1"));
        assertEquals("line1" + SYSTEM_NEW_LINE + "line2", params2.getString("text"));

        // Convert to SINGLE_LINE
        params2.setRenderStyle(AponRenderStyle.SINGLE_LINE);
        String singleLine = new AponWriter().write(params2).toString();
        System.out.println("Single Line:\n" + singleLine);

        // Parse singleLine back
        Parameters params3 = AponParser.parse(singleLine);
        assertEquals("val1", params3.getString("key1"));
        assertEquals("line1" + SYSTEM_NEW_LINE + "line2", params3.getString("text"));
    }

}
