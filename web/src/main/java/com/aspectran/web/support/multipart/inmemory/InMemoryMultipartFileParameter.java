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
 * This class represents a file item that was received within a multipart/form-data POST request.
 */
public class InMemoryMultipartFileParameter extends FileParameter {

    private FileItem fileItem;

    /**
     * Create an instance wrapping the given FileItem.
     * @param fileItem the FileItem to wrap
     */
    public InMemoryMultipartFileParameter(FileItem fileItem) {
        this.fileItem = fileItem;
    }

    @Override
    public File getFile() {
        return null;
    }

    /**
     * Gets the content type of the data being uploaded. This is never null, and
     * defaults to "content/unknown" when the mime type of the data couldn't be
     * determined and was not set manually.
     * @return the content type
     */
    @Override
    public String getContentType() {
        return fileItem.getContentType();
    }

    /**
     * Returns the file name that was uploaded in the multipart form.
     * @return the file name
     */
    @Override
    public String getFileName() {
        return getCanonicalName(fileItem.getName());
    }

    /**
     * Returns the file size that was uploaded in the multipart form.
     * @return the file size
     */
    @Override
    public long getFileSize() {
        return fileItem.getSize();
    }

    /**
     * Return an InputStream to read the contents of the file from.
     * @return the contents of the file as stream, or an empty stream if empty
     * @throws IOException in case of access errors (if the temporary store fails)
     */
    @Override
    public InputStream getInputStream() throws IOException {
        InputStream inputStream = fileItem.getInputStream();
        return (inputStream != null ? inputStream : new ByteArrayInputStream(new byte[0]));
    }

    /**
     * Return an byte array to read the contents of the file from.
     * @return the byte array
     */
    @Override
    public byte[] getBytes() {
        byte[] bytes = fileItem.get();
        return (bytes != null ? bytes : new byte[0]);
    }

    /**
     * Save an uploaded file as a given destination file.
     * @param destFile the destination file
     * @param overwrite whether to overwrite if it already exists
     * @return a saved file
     * @throws IOException if an I/O error has occurred
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

    @Override
    public File renameTo(File destFile, boolean overwrite) {
        throw new IllegalStateException("Can not rename because it is a file stored in memory");
    }

    /**
     * Deletes the underlying Commons FileItem instances.
     */
    @Override
    public void delete() {
        fileItem.delete();
    }

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
     * Return a description for the storage location of the multipart content.
     * Tries to be as specific as possible: mentions the file location in case
     * of a temporary file.
     * @return a description for the storage location of the multipart content
     */
    public String getStorageDescription() {
        return "in memory";
    }

}
