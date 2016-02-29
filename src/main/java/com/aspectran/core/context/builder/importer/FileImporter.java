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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.aspectran.core.context.rule.type.ImportFileType;
import com.aspectran.core.context.rule.type.ImporterType;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class FileImporter.
 * 
 * <p>Created: 2008. 04. 24 AM 11:23:36</p>
 * 
 * @author Juho Jeong
 */
public class FileImporter extends AbstractImporter {
	
	private final static ImporterType FILE_IMPORTER = ImporterType.FILE;
	
	private final String basePath;
	
	private final String filePath;

	public FileImporter(String filePath, ImportFileType importFileType) {
		this(null, filePath, importFileType);
	}

	public FileImporter(String basePath, String filePath, ImportFileType importFileType) {
		super(FILE_IMPORTER);
	
		if(importFileType == null)
			importFileType = filePath.endsWith(".apon") ? ImportFileType.APON : ImportFileType.XML;
		
		setImportFileType(importFileType);

		this.basePath = basePath;
		this.filePath = filePath;
	}

	@Override
	public String getDistinguishedName() {
		return filePath;
	}
	
	public String getBasePath() {
		return basePath;
	}

	public String getFilePath() {
		return filePath;
	}

	public File getFile() {
		File file;
		if(basePath == null)
			file = new File(filePath);
		else
			file = new File(basePath, filePath);
		
		return file;
	}
	
	@Override
	public long getLastModified() {
		File file = getFile();
		return file.lastModified();
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		File file = getFile();
		
		if(!file.isFile()) {
			throw new IOException("Could not find file to import. file: " + file.getAbsolutePath());
		}
		
		setLastModified(file.lastModified());

		return new FileInputStream(file);
	}
	
	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("importerType", getImporterType());
		tsb.append("basePath", basePath);
		tsb.append("filePath", filePath);
		return tsb.toString();
	}
	
}
