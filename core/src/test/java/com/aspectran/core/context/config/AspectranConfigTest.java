/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.utils.apon.AponWriter;
import com.aspectran.utils.apon.Parameters;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * <p>Created: 2016. 9. 7.</p>
 */
class AspectranConfigTest {

    @Test
    void aspectranConfigTest() throws IOException {
        File file = ResourceUtils.getResourceAsFile("config/aspectran-config-test.apon");
        AspectranConfig aspectranConfig = new AspectranConfig();
        AponReader.parse(file, aspectranConfig);

        File outputFile = new File("./target/test-classes/config/aspectran-config-test-output.apon");

        try (AponWriter aponWriter = new AponWriter(outputFile)) {
            aponWriter.nullWritable(false);
            aponWriter.write(aspectranConfig);
        }
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

            AponWriter aponWriter = new AponWriter(new PrintWriter(System.out));
            aponWriter.comment("\ncomment line-1\ncomment line-2\ncomment line-3\n");
            aponWriter.enableValueTypeHints(false);
            aponWriter.write(aspectranConfig);
            aponWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
