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
package com.aspectran.core.context.config;

import com.aspectran.utils.ResourceUtils;
import com.aspectran.utils.apon.AponReader;
import com.aspectran.utils.apon.AponWriterCloseable;
import com.aspectran.utils.apon.Parameters;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2016. 9. 7.</p>
 */
class AspectranConfigTest {

    @Test
    void aspectranConfigTest() throws IOException {
        File file = ResourceUtils.getResourceAsFile("config/aspectran-config-test.apon");
        AspectranConfig aspectranConfig = new AspectranConfig();
        AponReader.read(file, aspectranConfig);

        File outputFile = new File("./target/test-classes/config/aspectran-config-test-output.apon");
        try (AponWriterCloseable aponWriter = new AponWriterCloseable(outputFile)) {
            aponWriter.nullWritable(false);
            aponWriter.write(aspectranConfig);
        }

        String expected = readAllCharactersOneByOne(new FileReader(outputFile));
        String actual = AponReader.read(outputFile).toString();

        assertEquals(expected, actual);
    }

    static String readAllCharactersOneByOne(@NonNull Reader reader) throws IOException {
        StringBuilder content = new StringBuilder();
        int nextChar;
        while ((nextChar = reader.read()) != -1) {
            content.append((char) nextChar);
        }
        return String.valueOf(content);
    }

    public static void main(String[] args) {
        try {
            File file = ResourceUtils.getResourceAsFile("config/aspectran-config-test.apon");
            Reader reader = new FileReader(file);

            Parameters aspectranConfig = new AspectranConfig();

            AponReader aponReader = new AponReader(reader);
            aponReader.read(aspectranConfig);
            aponReader.close();

            //System.out.println(aspectranConfig);

            try (AponWriterCloseable aponWriter = new AponWriterCloseable(new PrintWriter(System.out))) {
                aponWriter.comment("\ncomment line-1\ncomment line-2\ncomment line-3\n");
                aponWriter.enableValueTypeHints(false);
                aponWriter.write(aspectranConfig);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
