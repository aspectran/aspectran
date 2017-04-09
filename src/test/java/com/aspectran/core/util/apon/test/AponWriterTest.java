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
import java.io.StringWriter;
import java.io.Writer;

import com.aspectran.core.context.builder.config.AspectranConfig;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.core.util.apon.Parameters;

public class AponWriterTest {

    public static void main(String argv[]) {
        try {
            // 먼저 AponWriter를 사용해서 파일로 저장된 APON 문서를 읽어서 Parameters 객체로 변환합니다.
            Reader reader = new FileReader(new File(argv[0]));

            Parameters aspectranConfig = new AspectranConfig();

            AponReader aponReader = new AponReader(reader);
            aponReader.read(aspectranConfig);
            aponReader.close();

            System.out.println(aspectranConfig);

            Writer writer = new StringWriter();

            AponWriter aponWriter = new AponWriter(writer);
            aponWriter.write(aspectranConfig);
            aponWriter.close();

            System.out.println(writer.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
