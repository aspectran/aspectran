/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.util.apon.test;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.core.util.apon.Parameters;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Reader;

public class AponWriterTest {

    public static void main(String argv[]) {
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
            aponWriter.setTypeHintWrite(false);
            aponWriter.write(aspectranConfig);
            aponWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
