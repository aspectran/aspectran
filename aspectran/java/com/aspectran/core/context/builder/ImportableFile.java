/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.core.context.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.aspectran.core.context.rule.type.ImportType;

/**
 * <p>Created: 2008. 04. 24 오전 11:23:36</p>
 * 
 * @author Gulendol
 */
public class ImportableFile extends Importable {
	
	private final static ImportType FILE_IMPORT = ImportType.FILE;
	
	private String basePath;
	
	private String filePath;

	public ImportableFile(String filePath) {
		this(null, filePath);
	}
	
	public ImportableFile(String basePath, String filePath) {
		super(FILE_IMPORT);
	
		this.basePath = basePath;
		this.filePath = filePath;
	}
	
	/**
	 * Gets the input stream.
	 * 
	 * @return the input stream
	 */
	public InputStream getInputStream() throws IOException {
		File file;
		
		if(basePath == null)
			file = new File(filePath);
		else
			file = new File(basePath, filePath);
		
		if(!file.isFile()) {
			throw new IOException("Could not find file to import. file: " + file.getAbsolutePath());
		}
		
		setLastModified(file.lastModified());
		
		InputStream inputStream = new FileInputStream(file);
		
		return inputStream;
	}
	
}
