/*
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
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.aspectran.core.context.rule.type.ImporterFileFormatType;
import com.aspectran.core.context.rule.type.ImporterType;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class UrlImporter.
 * 
 * <p>Created: 2008. 04. 24 AM 11:23:36</p>
 */
public class UrlImporter extends AbstractImporter {
	
	private final static ImporterType URL_IMPORTER = ImporterType.URL;
	
	private final String urlString;

	public UrlImporter(String urlString, ImporterFileFormatType importerFileFormatType) {
		super(URL_IMPORTER);

		if (importerFileFormatType == null) {
			importerFileFormatType = urlString.endsWith(".apon") ? ImporterFileFormatType.APON : ImporterFileFormatType.XML;
		}

		setImporterFileFormatType(importerFileFormatType);
		
		this.urlString = urlString;
	}
	
	@Override
	public String getDistinguishedName() {
		return urlString;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		setLastModified(conn.getLastModified());

		return conn.getInputStream();
	}
	
	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("importerType", getImporterType());
		tsb.append("url", urlString);
		tsb.append("profile", getProfiles());
		return tsb.toString();
	}
	
}
