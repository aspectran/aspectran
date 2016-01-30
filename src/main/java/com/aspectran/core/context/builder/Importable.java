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
package com.aspectran.core.context.builder;

import com.aspectran.core.context.rule.type.ImportFileType;
import com.aspectran.core.context.rule.type.ImportType;
import com.aspectran.core.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * The Class Importable.
 *
 * <p>Created: 2008. 04. 24 AM 11:23:36</p>
 * 
 * @author Juho Jeong
 */
public abstract class Importable {
	
	private ImportType importType;
	
	private ImportFileType importFileType;
	
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
	
	public ImportFileType getImportFileType() {
		return importFileType;
	}

	public void setImportFileType(ImportFileType importFileType) {
		this.importFileType = importFileType;
	}

	abstract public String getDistinguishedName();
	
	abstract public InputStream getInputStream() throws IOException;
	
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
	
	public static Importable newInstance(ContextBuilderAssistant assistant, String file, String resource, String url, String fileType) {
		ImportFileType importFileType = ImportFileType.valueOf(fileType);
		Importable importable;
		
		if(StringUtils.hasText(file)) {
			importable = new ImportableFile(assistant.getApplicationBasePath(), file, importFileType);
		} else if(StringUtils.hasText(resource)) {
			importable = new ImportableResource(assistant.getClassLoader(), resource, importFileType);
		} else if(StringUtils.hasText(url)) {
			importable = new ImportableUrl(url, importFileType);
		} else {
			throw new IllegalArgumentException("The <import> element requires either a resource or a file or a url attribute.");
		}
		
		return importable;
	}
	
}
