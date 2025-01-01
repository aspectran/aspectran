/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.core.component.bean.annotation.Action;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.RequestToDelete;
import com.aspectran.core.component.bean.annotation.RequestToGet;
import com.aspectran.core.component.bean.annotation.RequestToPost;
import com.aspectran.core.component.bean.annotation.Transform;
import com.aspectran.core.context.rule.type.FormatType;
import com.aspectran.utils.FilenameUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.web.support.http.HttpStatus;
import com.aspectran.web.support.http.HttpStatusSetter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>Created: 2018. 7. 9.</p>
 */
@Component("/examples/file-upload")
public class SimpleFileUploadActivity {

    private static final Logger logger = LoggerFactory.getLogger(SimpleFileUploadActivity.class);

    private final Map<String, UploadedFile> uploadedFiles = new LinkedHashMap<>();

    private int maxFiles = 30;

    public void setMaxFiles(int maxFiles) {
        this.maxFiles = maxFiles;
    }

    public int getMaxFiles() {
        return maxFiles;
    }

    private void addUploadedFile(UploadedFile uploadedFile) {
        synchronized (uploadedFiles) {
            uploadedFiles.put(uploadedFile.getKey(), uploadedFile);
            logger.debug("Uploaded File " + uploadedFile);

            if (uploadedFiles.size() > this.maxFiles) {
                Iterator<String> it = uploadedFiles.keySet().iterator();
                int cnt = uploadedFiles.size() - this.maxFiles;
                while (cnt-- > 0) {
                    if (it.hasNext()) {
                        UploadedFile removedFile = uploadedFiles.remove(it.next());
                        logger.debug("Remove Old File " + removedFile);
                    }
                }
            }
        }
    }

    private UploadedFile removeUploadedFile(String key) {
        synchronized (uploadedFiles) {
            return uploadedFiles.remove(key);
        }
    }

    @RequestToPost("/files")
    @Transform(FormatType.JSON)
    @Action("files")
    public Collection<UploadedFile> upload(@NonNull Translet translet) throws IOException {
        FileParameter fileParameter = translet.getFileParameter("file");
        if (fileParameter != null) {
            String key = UUID.randomUUID().toString();
            String ext = FilenameUtils.getExtension(fileParameter.getFileName());
            if (StringUtils.hasLength(ext)) {
                key += "." + ext.toLowerCase();
            }
            UploadedFile uploadedFile = new UploadedFile();
            uploadedFile.setKey(key);
            uploadedFile.setFileName(fileParameter.getFileName());
            uploadedFile.setFileSize(fileParameter.getFileSize());
            uploadedFile.setHumanFileSize(StringUtils.toHumanFriendlyByteSize(fileParameter.getFileSize()));
            uploadedFile.setFileType((fileParameter.getContentType()));
            uploadedFile.setUrl("/examples/file-upload/files/" + key);
            uploadedFile.setBytes(fileParameter.getBytes());

            addUploadedFile(uploadedFile);

            List<UploadedFile> files = new ArrayList<>();
            files.add(uploadedFile);
            return files;
        } else {
            return null;
        }
    }

    @RequestToGet("/files/${key}")
    public void serve(@NonNull Translet translet) throws IOException {
        String key = translet.getParameter("key");
        UploadedFile uploadedFile = uploadedFiles.get(key);
        if (uploadedFile != null) {
            translet.getResponseAdapter().setContentType(uploadedFile.getFileType());
            translet.getResponseAdapter().setHeader("Content-disposition",
                    "attachment; filename=\"" + uploadedFile.getFileName() + "\"");
            translet.getResponseAdapter().getOutputStream().write(uploadedFile.getBytes());
        } else {
            HttpStatusSetter.setStatus(HttpStatus.NOT_FOUND, translet);
        }
    }

    @RequestToDelete("/files/${key}")
    public void delete(@NonNull Translet translet) {
        String key = translet.getParameter("key");
        UploadedFile removedFile = removeUploadedFile(key);
        if (removedFile == null) {
            HttpStatusSetter.setStatus(HttpStatus.NOT_FOUND, translet);
        }
    }

    @RequestToGet("/files")
    @Transform(FormatType.JSON)
    @Action("files")
    public Collection<UploadedFile> list() {
        return uploadedFiles.values();
    }

}
