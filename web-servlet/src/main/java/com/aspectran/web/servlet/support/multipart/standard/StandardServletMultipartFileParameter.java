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
package com.aspectran.web.servlet.support.multipart.standard;

import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.utils.FilenameUtils;
import jakarta.servlet.http.Part;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@link FileParameter} implementation that wraps a standard Jakarta Servlet
 * {@link Part} object.
 *
 * @since 9.7.0
 */
public class StandardServletMultipartFileParameter extends FileParameter {

    private final Part part;

    /**
     * Constructs a new instance wrapping the given {@link Part}.
     * @param part the standard servlet part
     */
    public StandardServletMultipartFileParameter(@NonNull Part part) {
        super(null, part.getContentType());
        this.part = part;
    }

    /**
     * Returns the underlying standard servlet {@link Part}.
     * @return the servlet part
     */
    public Part getPart() {
        return part;
    }

    @Override
    public String getFileName() {
        return FilenameUtils.getName(part.getSubmittedFileName());
    }

    @Override
    public long getFileSize() {
        return part.getSize();
    }

    @Override
    @Nullable
    public String getContentType() {
        return part.getContentType();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return part.getInputStream();
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (InputStream in = part.getInputStream()) {
            return in.readAllBytes();
        }
    }

    @Override
    public File saveAs(File destFile, boolean overwrite) throws IOException {
        if (destFile == null) {
            throw new IllegalArgumentException("destFile cannot be null");
        }
        destFile = determineDestinationFile(destFile, overwrite);
        try {
            part.write(destFile.getAbsolutePath());
        } catch (Exception e) {
            // Fallback to stream copying if native write fails
            try (InputStream in = part.getInputStream();
                 OutputStream out = new FileOutputStream(destFile)) {
                in.transferTo(out);
            }
        }
        setSavedFile(destFile);
        return destFile;
    }

    @Override
    public File moveTo(File destFile, boolean overwrite) throws IOException {
        File saved = saveAs(destFile, overwrite);
        delete();
        return saved;
    }

    @Override
    public void delete() {
        try {
            part.delete();
        } catch (Exception ignored) {
            // Ignore failure on cleanup
        }
    }

    @Override
    public void release() {
        releaseSavedFile();
    }

}
