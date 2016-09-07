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
import java.io.FileWriter;
import java.io.IOException;

/**
 * The Class FileAponWriter.
 */
public class FileAponWriter extends AponWriter {

	public FileAponWriter(File file) throws IOException {
		super(new FileWriter(file));
	}

	public FileAponWriter(File file, boolean prettyPrint) throws IOException {
		super(new FileWriter(file), prettyPrint);
	}
	
	public FileAponWriter(File file, boolean prettyPrint, String indentString) throws IOException {
		super(new FileWriter(file), prettyPrint, indentString);
	}
	
}
