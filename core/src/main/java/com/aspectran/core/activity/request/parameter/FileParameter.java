/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.activity.request.parameter;

import com.aspectran.core.util.FilenameUtils;
import com.aspectran.core.util.ToStringBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The Class FileParameter.
 * 
 * <p>Created: 2008. 04. 11 PM 4:19:40</p>
 */
public class FileParameter {

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private final File file;

    private boolean refused;

    protected File savedFile;

    /**
     * Instantiates a new FileParameter.
     */
    protected FileParameter() {
        this.file = null;
    }

    /**
     * Instantiates a new FileParameter.
     *
     * @param file the file
     */
    public FileParameter(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    /**
     * Gets the actual name of the file uploaded.
     *
     * @return the actual name of the file uploaded
     */
    public String getFileName() {
        return (file != null ? file.getName() : null);
    }

    /**
     * Gets the size of the file uploaded.
     *
     * @return the size of the file uploaded
     */
    public long getFileSize() {
        return (file != null ? file.length() : -1);
    }

    /**
     * Gets the the content type of the file.
     *
     * @return the content type of the file
     */
    public String getContentType() {
        return null;
    }

    /**
     * Returns an {@code InputStream} object of the file.
     *
     * @return an {@link java.io.OutputStream OutputStream} that can be used
     *         for storing the contents of the file.
     * @throws IOException if an I/O error has occurred
     */
    public InputStream getInputStream() throws IOException {
        if (file == null) {
            throw new IOException("No file specified for this file parameter " + this);
        }
        return new FileInputStream(file);
    }

    /**
     * Returns the contents of the file in a byte array.
     * Can not use a large array of memory than the JVM Heap deal.
     *
     * @return a byte array
     * @throws IOException if an I/O error has occurred
     */
    public byte[] getBytes() throws IOException {
        InputStream input = getInputStream();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int len;

        try {
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                // ignore
            }
            try {
                input.close();
            } catch (IOException e) {
                // ignore
            }
        }

        return output.toByteArray();
    }

    /**
     * Checks if the file is refused.
     *
     * @return true if the file is refused; false otherwise
     */
    public boolean isRefused() {
        return refused;
    }

    /**
     * Sets whether the refused file.
     *
     * @param refused whether the file is refused or not
     */
    public void setRefused(boolean refused) {
        this.refused = refused;
    }

    /**
     * Save an uploaded file as a given destination file.
     * If the file already exists in directory the save with a different name.
     *
     * @param destFile the destination file
     * @return a saved file
     * @throws IOException if an I/O error has occurred
     */
    public File saveAs(File destFile) throws IOException {
        return saveAs(destFile, false);
    }

    /**
     * Save an file as a given destination file.
     *
     * @param destFile the destination file
     * @param overwrite whether to overwrite if it already exists
     * @return a saved file
     * @throws IOException if an I/O error has occurred
     */
    public File saveAs(File destFile, boolean overwrite) throws IOException {
        if (destFile == null) {
            throw new IllegalArgumentException("Argument 'destFile' must not be null");
        }

        if (!overwrite) {
            File newFile = FilenameUtils.getUniqueFile(destFile);
            if (destFile != newFile) {
                destFile = newFile;
            }
        } else {
            if (destFile.exists() && !destFile.delete()) {
                throw new IOException("Destination file [" + destFile.getAbsolutePath() +
                        "] already exists and could not be deleted");
            }
        }

        InputStream input = getInputStream();
        OutputStream output = new FileOutputStream(destFile);

        final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int len;

        try {
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                // ignore
            }
            try {
                input.close();
            } catch (IOException e) {
                // ignore
            }
        }

        savedFile = destFile;

        return destFile;
    }

    /**
     * Returns the saved file.
     *
     * @return the saved file
     */
    public File getSavedFile() {
        return savedFile;
    }

    /**
     * Delete a file.
     */
    public void delete() {
        if (file != null) {
            file.delete();
        }
    }

    /**
     * If the saved file exists, delete it.
     */
    public void rollback() {
        if (savedFile != null) {
            savedFile.delete();
        }
    }

    /**
     * Sets the access permission that allow write operations on the file associated with this FileParameter.
     */
    public void release() {
        if (file != null) {
            file.setWritable(true);
        }
        if (savedFile != null) {
            savedFile.setWritable(true);
        }
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("file", file);
        tsb.append("savedFile", savedFile);
        tsb.append("refused", refused);
        return tsb.toString();
    }

}
