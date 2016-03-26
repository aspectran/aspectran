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
package com.aspectran.core.context.builder.importer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import com.aspectran.core.context.rule.type.ImportFileType;
import com.aspectran.core.context.rule.type.ImporterType;

/**
 * The Class AbstractImporter.
 *
 * <p>Created: 2008. 04. 24 AM 11:23:36</p>
 * 
 * @author Juho Jeong
 */
abstract class AbstractImporter implements Importer {

	private ImporterType importerType;

	private ImportFileType importFileType;

	private long lastModified;

	AbstractImporter(ImporterType importerType) {
		this.importerType = importerType;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public ImporterType getImporterType() {
		return importerType;
	}
	
	public ImportFileType getImportFileType() {
		return importFileType;
	}

	public void setImportFileType(ImportFileType importFileType) {
		this.importFileType = importFileType;
	}

	public Reader getReader() throws IOException {
		return getReader(null);
	}
	
	public Reader getReader(String encoding) throws IOException {
		if(encoding != null) {
			return new InputStreamReader(getInputStream(), encoding);
		} else {
			return new InputStreamReader(getInputStream());
		}
	}

}
