/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.builder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.aspectran.core.context.rule.type.ImportFileType;
import com.aspectran.core.context.rule.type.ImportType;

/**
 * <p>Created: 2008. 04. 24 오전 11:23:36</p>
 * 
 * @author Juho Jeong
 */
public class ImportableUrl extends Importable {
	
	private final static ImportType URL_IMPORT = ImportType.URL;
	
	private final String urlString;

	public ImportableUrl(String urlString, ImportFileType importFileType) {
		super(URL_IMPORT);

		if(importFileType == null)
			importFileType = urlString.endsWith(".apon") ? ImportFileType.APON : ImportFileType.XML;
		
		setImportFileType(importFileType);
		
		this.urlString = urlString;
	}
	
	public String getDistinguishedName() {
		return urlString;
	}

	/**
	 * Gets the input stream.
	 * 
	 * @return the input stream
	 */
	public InputStream getInputStream() throws IOException {
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		setLastModified(conn.getLastModified());
		InputStream inputStream = conn.getInputStream();

		return inputStream;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{importType=").append(getImportType());
		sb.append(", url=").append(urlString);
		sb.append("}");
		
		return sb.toString();
	}
	
}
