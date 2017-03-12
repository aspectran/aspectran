/**
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
package com.aspectran.core.context.builder.importer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import com.aspectran.core.context.rule.type.ImporterFileFormatType;
import com.aspectran.core.context.rule.type.ImporterType;

/**
 * The Class AbstractImporter.
 *
 * <p>Created: 2008. 04. 24 AM 11:23:36</p>
 */
abstract class AbstractImporter implements Importer {

	private ImporterType importerType;

	private ImporterFileFormatType importerFileFormatType;
	
	private String[] profiles;

	private long lastModified;

	AbstractImporter(ImporterType importerType) {
		this.importerType = importerType;
	}

	@Override
	public ImporterType getImporterType() {
		return importerType;
	}
	
	@Override
	public ImporterFileFormatType getImporterFileFormatType() {
		return importerFileFormatType;
	}

	@Override
	public void setImporterFileFormatType(ImporterFileFormatType importerFileFormatType) {
		this.importerFileFormatType = importerFileFormatType;
	}

	@Override
	public String[] getProfiles() {
		return profiles;
	}

	@Override
	public void setProfiles(String[] profiles) {
		this.profiles = profiles;
	}

	@Override
	public long getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public Reader getReader() throws IOException {
		return getReader(null);
	}
	
	@Override
	public Reader getReader(String encoding) throws IOException {
		if (encoding != null) {
			return new InputStreamReader(getInputStream(), encoding);
		} else {
			return new InputStreamReader(getInputStream());
		}
	}

}
