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
package com.aspectran.core.context.parser.importer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.aspectran.core.context.rule.type.ImporterFileFormatType;
import com.aspectran.core.context.rule.type.ImporterType;

/**
 * The Interface Importer.
 *
 * <p>Created: 2008. 04. 24 AM 11:23:36</p>
 */
public interface Importer {

    ImporterType getImporterType();

    ImporterFileFormatType getImporterFileFormatType();

    void setImporterFileFormatType(ImporterFileFormatType importerFileFormatType);

    String[] getProfiles();

    void setProfiles(String[] profiles);

    long getLastModified();

    void setLastModified(long lastModified);

    String getDistinguishedName();

    InputStream getInputStream() throws IOException;

    Reader getReader() throws IOException;

    Reader getReader(String encoding) throws IOException;

}
