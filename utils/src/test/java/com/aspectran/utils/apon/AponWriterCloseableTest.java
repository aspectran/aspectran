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
package com.aspectran.utils.apon;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AponWriterCloseableTest {

    @Test
    void testWriterClose() throws IOException {
        String apon = """
            aspectran: {
                settings: {
                    transletNameSuffix: .job
                }
                bean: {
                    id: *
                    scan: test.**.*Schedule
                    mask: test.**.*
                    scope: singleton
                }
            }
            """;

        Parameters ps = AponReader.read(apon);
        String expected = apon.replace("\n", AponFormat.SYSTEM_NEW_LINE);

        StringWriter writer = new StringWriter();
        try (AponWriterCloseable aponWriter = new AponWriterCloseable(writer)) {
            aponWriter.indentString("    ");
            aponWriter.write(ps);
            assertEquals(expected, writer.toString());
        }
    }

}
