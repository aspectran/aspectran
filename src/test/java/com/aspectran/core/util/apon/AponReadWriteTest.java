/**
 * Copyright 2008-2016 Juho Jeong
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
package com.aspectran.core.util.apon;

import java.io.File;
import java.io.IOException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * <p>Created: 2016. 9. 7.</p>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AponReadWriteTest {

	@Test
	public void readWriteTest() throws IOException {
		File file = new File("./target/test-classes/config/apon-test.apon");
		Parameters parameters = AponReader.parse(file);
		
		File outputFile = new File("./target/test-classes/config/apon-test-output.apon");
		if (outputFile.exists()) {
			outputFile.delete();
		}
		
		AponWriter aponWriter = new FileAponWriter(outputFile, true, "  ");
		aponWriter.setNoQuotes(true);
		aponWriter.setNullWrite(true);
		aponWriter.setTypeHintWrite(true);
		aponWriter.write(parameters);
		aponWriter.close();
	}

}