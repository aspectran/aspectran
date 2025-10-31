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
package com.aspectran.web.support.multipart.inmemory;

import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.utils.FilenameUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * An in-memory implementation of {@link FileParameter} that wraps an {@link InMemoryFileItem}.
 * <p>This class provides access to the metadata and content of a file that was
 * uploaded in a multipart/form-data request and is being stored in memory.
 */
public class InMemoryMultipartFileParameter extends FileParameter {

    private FileItem fileItem;

    /**
     * Create an instance wrapping the given FileItem.
     * @param fileItem the FileItem to wrap
     */
    public InMemoryMultipartFileParameter(@NonNull FileItem fileItem) {
        super(null, fileItem.getContentType());
        this.fileItem = fileItem;
    }

    /**
     * Returns the original filename in the client's filesystem.
     * @return the original filename, or {@code null} if not defined
     */
    @Override
    public String getFileName() {
        return getCanonicalName(fileItem.getName());
    }

    /**
     * Returns the size of the uploaded file in bytes.
     * @return the size of the file in bytes
     */
    @Override
    public long getFileSize() {
        return fileItem.getSize();
    }

    /**
     * Returns an {@link InputStream} to read the contents of the file.
     * @return an {@link InputStream} for the file's contents
     * @throws IOException if an I/O error occurs
     */
    @Override
    public InputStream getInputStream() throws IOException {
        InputStream inputStream = fileItem.getInputStream();
        return (inputStream != null ? inputStream : new ByteArrayInputStream(new byte[0]));
    }

    /**
     * Returns the contents of the file as a byte array.
     * @return the file's contents as a byte array
     */
    @Override
    public byte[] getBytes() {
        byte[] bytes = fileItem.get();
        return (bytes != null ? bytes : new byte[0]);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation writes the in-memory data to the specified file.</p>
     */
    @Override
    public File saveAs(File destFile, boolean overwrite) throws IOException {
        if (destFile == null) {
            throw new IllegalArgumentException("destFile can not be null");
        }

        destFile = determineDestinationFile(destFile, overwrite);

        try {
            fileItem.write(destFile);
        } catch (FileUploadException e) {
            throw new IllegalStateException(e.getMessage());
        } catch (Exception e) {
            throw new IOException("Could not save as file " + destFile, e);
        }

        setSavedFile(destFile);
        return destFile;
    }

    /**
     * {@inheritDoc}
     * <p>This operation is not supported for in-memory file items and will always
     * throw an {@link IllegalStateException}. Use {@link #saveAs(File, boolean)} instead.</p>
     */
    @Override
    public File moveTo(File destFile, boolean overwrite) {
        throw new IllegalStateException("Cannot move an in-memory file. Use saveAs() instead.");
    }

    /**
     * {@inheritDoc}
     * <p>For an in-memory file, this method releases the internal byte buffer to be
     * garbage collected by delegating to {@link FileItem#delete()}.</p>
     */
    @Override
    public void delete() {
        fileItem.delete();
    }

    /**
     * Releases all resources associated with this file parameter.
     */
    @Override
    public void release() {
        if (fileItem != null) {
            fileItem = null;
        }
        releaseSavedFile();
    }

    /**
     * Returns the canonical name of the given filename.
     * @param filename the given filename
     * @return the canonical name of the given filename
     */
    @NonNull
    private String getCanonicalName(String filename) {
        return FilenameUtils.getName(filename);
    }

    /**
     * Returns a description of the storage location, which is always "in memory".
     * @return the string "in memory"
     */
    public String getStorageDescription() {
        return "in memory";
    }

}
