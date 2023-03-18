/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.web.support.multipart.commons;

import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.core.util.FilenameUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class represents a file item that was received within
 * a multipart/form-data POST request.
 * 
 * <p>Created: 2008. 04. 11 PM 8:55:25</p>
 */
public class CommonsMultipartFileParameter extends FileParameter {

    private FileItem fileItem;

    private long fileSize;

    /**
     * Create an instance wrapping the given FileItem.
     *
     * @param fileItem the FileItem to wrap
     */
    public CommonsMultipartFileParameter(FileItem fileItem) {
        this.fileItem = fileItem;
        this.fileSize = fileItem.getSize();
    }

    @Override
    public File getFile() {
        if (fileItem instanceof DiskFileItem) {
            return ((DiskFileItem)fileItem).getStoreLocation();
        } else {
            return null;
        }
    }

    /**
     * Gets the content type of the data being uploaded. This is never null, and
     * defaults to "content/unknown" when the mime type of the data couldn't be
     * determined and was not set manually.
     *
     * @return the content type
     */
    @Override
    public String getContentType() {
        return fileItem.getContentType();
    }

    /**
     * Returns the file name that was uploaded in the multipart form.
     *
     * @return the file name
     */
    @Override
    public String getFileName() {
        return getCanonicalName(fileItem.getName());
    }

    /**
     * Returns the file size that was uploaded in the multipart form.
     *
     * @return the file size
     */
    @Override
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Return an InputStream to read the contents of the file from.
     *
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
     *
     * @return the byte array
     */
    @Override
    public byte[] getBytes() {
        byte[] bytes = fileItem.get();
        return (bytes != null ? bytes : new byte[0]);
    }

    /**
     * Save an uploaded file as a given destination file.
     *
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

        validateFile();

        try {
            destFile = determineDestinationFile(destFile, overwrite);
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
    public File renameTo(File destFile, boolean overwrite) throws IOException {
        File file = getFile();
        if (file == null) {
            throw new IllegalStateException("The uploaded temporary file does not exist");
        }
        if (destFile == null) {
            throw new IllegalArgumentException("destFile can not be null");
        }

        validateFile();
        return super.renameTo(destFile, overwrite);
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
            fileSize = 0L;
        }
        releaseSavedFile();
    }

    /**
     * Returns the canonical name of the given filename.
     *
     * @param filename the given filename
     * @return the canonical name of the given filename
     */
    private String getCanonicalName(String filename) {
        return FilenameUtils.getName(filename);
    }

    private void validateFile() {
        if (!isAvailable()) {
            throw new IllegalStateException("File has been moved - cannot be read again");
        }
    }

    /**
     * Determine whether the multipart content is still available.
     * If a temporary file has been moved, the content is no longer available.
     *
     * @return true, if the multipart content is still available
     */
    private boolean isAvailable() {
        // If in memory, it's available.
        if (fileItem.isInMemory()) {
            return true;
        }
        // Check actual existence of temporary file.
        if (fileItem instanceof DiskFileItem) {
            return ((DiskFileItem)fileItem).getStoreLocation().exists();
        }
        // Check whether current file size is different than original one.
        return (fileItem.getSize() == fileSize);
    }

    /**
     * Return a description for the storage location of the multipart content.
     * Tries to be as specific as possible: mentions the file location in case
     * of a temporary file.
     *
     * @return a description for the storage location of the multipart content
     */
    public String getStorageDescription() {
        if (fileItem.isInMemory()) {
            return "in memory";
        } else if (this.fileItem instanceof DiskFileItem) {
            return "at [" + ((DiskFileItem)this.fileItem).getStoreLocation().getAbsolutePath() + "]";
        } else {
            return "on disk";
        }
    }

}
