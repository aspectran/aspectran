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
package com.aspectran.netty.support.multipart;

import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.utils.Assert;
import com.aspectran.utils.FilenameUtils;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.multipart.FileUpload;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link FileParameter} implementation that wraps a Netty {@link FileUpload}.
 *
 * <p>Created: 2026-09-02</p>
 */
public class NettyFileParameter extends FileParameter {

    private final FileUpload fileUpload;

    public NettyFileParameter(@NonNull FileUpload fileUpload) {
        super(determineFile(fileUpload), fileUpload.getContentType());
        this.fileUpload = fileUpload;
    }

    private static File determineFile(FileUpload fileUpload) {
        if (!fileUpload.isInMemory()) {
            try {
                return fileUpload.getFile();
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String getFileName() {
        return FilenameUtils.getName(fileUpload.getFilename());
    }

    @Override
    public long getFileSize() {
        return fileUpload.length();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (fileUpload.isInMemory()) {
            return new ByteBufInputStream(fileUpload.getByteBuf().duplicate());
        } else {
            return new FileInputStream(fileUpload.getFile());
        }
    }

    @Override
    public byte[] getBytes() throws IOException {
        return fileUpload.get();
    }

    @Override
    public File saveAs(File destFile, boolean overwrite) throws IOException {
        Assert.notNull(destFile, "destFile must not be null");
        if (destFile.exists()) {
            if (!overwrite) {
                throw new IOException("Destination file already exists: " + destFile);
            }
            if (!destFile.delete()) {
                throw new IOException("Failed to delete existing file: " + destFile);
            }
        }
        File parentDir = destFile.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Failed to create parent directory: " + parentDir);
        }

        fileUpload.renameTo(destFile);
        setSavedFile(destFile);
        return destFile;
    }

    @Override
    public void delete() {
        fileUpload.delete();
        super.delete();
    }

}
