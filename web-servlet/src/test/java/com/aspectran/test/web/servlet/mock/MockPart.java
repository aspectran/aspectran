/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.test.web.servlet.mock;

import jakarta.servlet.http.Part;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;

/**
 * Mock implementation of {@link Part}.
 *
 * <p>Created: 2026-11-28</p>
 */
public class MockPart implements Part {

    private final String name;
    private final String submittedFileName;
    private final String contentType;
    private final byte[] content;
    private boolean deleted;

    public MockPart(String name, byte[] content) {
        this(name, null, null, content);
    }

    public MockPart(String name, String submittedFileName, String contentType, byte[] content) {
        this.name = name;
        this.submittedFileName = submittedFileName;
        this.contentType = contentType;
        this.content = (content != null ? content : new byte[0]);
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSubmittedFileName() {
        return submittedFileName;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public void write(String fileName) throws IOException {
        try (OutputStream out = new FileOutputStream(fileName)) {
            out.write(content);
        }
    }

    @Override
    public void delete() {
        this.deleted = true;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return Collections.emptyList();
    }

}
