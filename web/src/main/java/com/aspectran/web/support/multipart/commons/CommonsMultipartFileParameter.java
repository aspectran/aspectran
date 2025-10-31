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
package com.aspectran.web.support.multipart.commons;

import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.utils.FilenameUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link FileParameter} implementation that wraps an Apache Commons FileUpload
 * {@link FileItem}. This class provides access to the metadata and content of a
 * file uploaded in a multipart/form-data request.
 *
 * <p>Created: 2008. 04. 11 PM 8:55:25</p>
 */
public class CommonsMultipartFileParameter extends FileParameter {

    private FileItem fileItem;

    private long fileSize;

    /**
     * Create an instance wrapping the given FileItem.
     * @param fileItem the FileItem to wrap
     */
    public CommonsMultipartFileParameter(@NonNull FileItem fileItem) {
        super(determineFile(fileItem), fileItem.getContentType());
        this.fileItem = fileItem;
        this.fileSize = fileItem.getSize();
    }

    private static File determineFile(FileItem fileItem) {
        if (fileItem instanceof DiskFileItem diskFileItem) {
            return diskFileItem.getStoreLocation();
        }
        return null;
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
        return fileSize;
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
     * <p>This implementation uses the {@link FileItem#write(File)} method, which may be
     * more efficient than a manual stream copy.</p>
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

    /**
     * {@inheritDoc}
     * <p>This operation is only available if the uploaded file is stored on disk
     * (i.e., it is a {@link DiskFileItem}).</p>
     * @throws IllegalStateException if the file is not stored on disk
     */
    @Override
    public File moveTo(File destFile, boolean overwrite) throws IOException {
        File file = getFile();
        if (file == null) {
            throw new IllegalStateException("The uploaded temporary file does not exist");
        }
        if (destFile == null) {
            throw new IllegalArgumentException("destFile can not be null");
        }

        validateFile();
        return super.moveTo(destFile, overwrite);
    }

    /**
     * {@inheritDoc}
     * <p>This delegates to {@link FileItem#delete()}.</p>
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
            fileSize = 0L;
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

    private void validateFile() {
        if (!isAvailable()) {
            throw new IllegalStateException("File has been moved - cannot be read again");
        }
    }

    /**
     * Determine whether the multipart content is still available.
     * If a temporary file has been moved, the content is no longer available.
     * @return true, if the multipart content is still available
     */
    private boolean isAvailable() {
        // If in memory, it's available.
        if (fileItem.isInMemory()) {
            return true;
        }
        // Check actual existence of temporary file
        if (fileItem instanceof DiskFileItem) {
            return ((DiskFileItem)fileItem).getStoreLocation().exists();
        }
        // Check whether current file size is different from original one
        return (fileItem.getSize() == fileSize);
    }

    /**
     * Returns a description of the storage location for the multipart content.
     * This is primarily for debugging and logging purposes.
     * @return a description of the storage location
     */
    public String getStorageDescription() {
        if (fileItem.isInMemory()) {
            return "in memory";
        } else if (this.fileItem instanceof DiskFileItem) {
            return "as [" + ((DiskFileItem)this.fileItem).getStoreLocation().getAbsolutePath() + "]";
        } else {
            return "on disk";
        }
    }

}
