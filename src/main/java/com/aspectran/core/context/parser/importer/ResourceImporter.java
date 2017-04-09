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

import com.aspectran.core.context.rule.type.ImporterFileFormatType;
import com.aspectran.core.context.rule.type.ImporterType;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class ResourceImporter.
 * 
 * <p>Created: 2008. 04. 24 AM 11:23:36</p>
 */
public class ResourceImporter extends AbstractImporter {

    private final static ImporterType RESOURCE_IMPORTER = ImporterType.RESOURCE;

    private final ClassLoader classLoader;

    private final String resource;

    public ResourceImporter(ClassLoader classLoader, String resource, ImporterFileFormatType importerFileFormatType) {
        super(RESOURCE_IMPORTER);

        if (importerFileFormatType == null) {
            importerFileFormatType = resource.endsWith(".apon") ? ImporterFileFormatType.APON : ImporterFileFormatType.XML;
        }

        setImporterFileFormatType(importerFileFormatType);

        this.classLoader = classLoader;
        this.resource = resource;

        setLastModified(System.currentTimeMillis());
    }

    @Override
    public String getDistinguishedName() {
        return resource;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream inputStream = classLoader.getResourceAsStream(resource);

        if (inputStream == null) {
            throw new IOException("Could not find resource to import. resource: " + resource);
        }

        return inputStream;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("importerType", getImporterType());
        tsb.append("resource", resource);
        tsb.append("profile", getProfiles());
        return tsb.toString();
    }

}
