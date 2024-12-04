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

import com.aspectran.utils.ResourceUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

/**
 * <p>Created: 2016. 9. 7.</p>
 */
class AponReadWriteTest {

    @Test
    void writeTest() throws IOException {
        File file = ResourceUtils.getResourceAsFile("config/apon/apon-test.apon");
        Parameters parameters = AponReader.read(file);

        File outputFile = new File(ResourceUtils.getResourceAsFile("config/apon"), "apon-test-output.apon");

        try (AponWriterCloseable aponWriter = new AponWriterCloseable(outputFile)) {
            //aponWriter.setTypeHintWrite(true);
            aponWriter.write(parameters);
        }
    }

}
