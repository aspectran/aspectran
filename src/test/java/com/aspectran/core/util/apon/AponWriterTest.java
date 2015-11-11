/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.util.apon;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;

import com.aspectran.core.context.loader.config.AspectranConfig;

public class AponWriterTest {

	public static void main(String argv[]) {
		try {
			Reader fileReader = new FileReader(new File(argv[0]));
			
			AponReader reader = new AponReader(fileReader);
			
			Parameters aspectranConfig = new AspectranConfig();
			
			try {
				reader.read(aspectranConfig);
				
				System.out.println(aspectranConfig);
			} finally {
				reader.close();
			}
			
			StringWriter stringWriter = new StringWriter();
			
			AponWriter writer = new AponWriter(stringWriter);
			
			try {
				writer.write(aspectranConfig);
			} finally {
				writer.close();
			}
			
			System.out.println(stringWriter.toString());
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
