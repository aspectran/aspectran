package com.aspectran.demo.examples.upload;

import com.aspectran.core.component.bean.annotation.NonSerializable;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.json.JsonWriter;

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

    public static void main(String[] args) {
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setKey(UUID.randomUUID().toString());
        uploadedFile.setFileName("test11");
        uploadedFile.setFileSize(11);
        uploadedFile.setFileType("jpg");

        System.out.println(JsonWriter.stringify(uploadedFile));
    }

}
