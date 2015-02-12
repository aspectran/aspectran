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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.aspectran.core.context.rule.type.ImportType;

/**
 * <p>Created: 2008. 04. 24 오전 11:23:36</p>
 * 
 * @author Gulendol
 */
public abstract class Importable {
	
	private ImportType importType;
	
	private long lastModified;
	
	public Importable(ImportType importType) {
		this.importType = importType;
	}

	public long getLastModified() {
		return lastModified;
	}
	
	protected void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public ImportType getImportType() {
		return importType;
	}
	
	abstract public InputStream getInputStream() throws IOException;
	
	public Reader getReader() throws IOException {
		return getReader(null);
	}
	
	public Reader getReader(String encoding) throws IOException {
		if(encoding != null)
			return new InputStreamReader(getInputStream(), encoding);
		else
			return new InputStreamReader(getInputStream());
	}
	
}
