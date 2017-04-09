/*
 * Copyright 2008-2017 Juho Jeong
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

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import com.aspectran.core.context.builder.config.AspectranConfig;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.Parameters;

public class AponReaderTest {

    public static void main(String argv[]) {
        try {
            Reader reader = new FileReader(new File(argv[0]));

            Parameters aspectranConfig = new AspectranConfig();

            AponReader aponReader = new AponReader(reader);
            aponReader.read(aspectranConfig);
            aponReader.close();

            System.out.println(aspectranConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}