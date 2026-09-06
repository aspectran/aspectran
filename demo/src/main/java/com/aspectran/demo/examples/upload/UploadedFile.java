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
package com.aspectran.demo.examples.upload;

import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.utils.DataSizeUtils;
import com.aspectran.utils.FilenameUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.NonSerializable;
import com.aspectran.utils.json.JsonBuilder;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;

/**
 * <p>Created: 2018. 7. 9.</p>
 */
public class UploadedFile {

    private String key;

    private String fileName;

    private long fileSize;

    private String humanFileSize;

    private String fileType;

    private String url;

    private byte[] bytes;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getHumanFileSize() {
        return humanFileSize;
    }

    public void setHumanFileSize(String humanFileSize) {
        this.humanFileSize = humanFileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @NonSerializable
    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("key", key);
        tsb.append("fileName", fileName);
        tsb.append("fileSize", fileSize);
        tsb.append("humanFileSize", humanFileSize);
        tsb.append("fileType", fileType);
        tsb.append("url", url);
        return tsb.toString();
    }

    /**
     * Creates an {@code UploadedFile} instance from the given {@link FileParameter}.
     * @param fileParameter the file parameter to create from
     * @return a new {@code UploadedFile} instance, or {@code null} if fileParameter is null
     * @throws IOException if an I/O error occurs while reading the file bytes
     */
    @Nullable
    public static UploadedFile of(@Nullable FileParameter fileParameter) throws IOException {
        if (fileParameter == null) {
            return null;
        }
        String key = UUID.randomUUID().toString();
        String ext = FilenameUtils.getExtension(fileParameter.getFileName());
        if (StringUtils.hasLength(ext)) {
            key += "." + ext.toLowerCase();
        }
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setKey(key);
        uploadedFile.setFileName(fileParameter.getFileName());
        uploadedFile.setFileSize(fileParameter.getFileSize());
        uploadedFile.setHumanFileSize(DataSizeUtils.toHumanFriendlyByteSize(fileParameter.getFileSize()));
        uploadedFile.setFileType(fileParameter.getContentType());
        uploadedFile.setBytes(fileParameter.getBytes());
        return uploadedFile;
    }

    public static void main(String[] args) {
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setKey(UUID.randomUUID().toString());
        uploadedFile.setFileName("test11");
        uploadedFile.setFileSize(11);
        uploadedFile.setFileType("jpg");

        System.out.println(new JsonBuilder().nullWritable(false).put(uploadedFile));
    }

}
