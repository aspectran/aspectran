/*
 * Copyright (c) 2008-2022 The Aspectran Project
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

import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.AponWriter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

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

}
