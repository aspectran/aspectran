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
package com.aspectran.demo.examples.upload;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.component.bean.annotation.Action;
import com.aspectran.core.component.bean.annotation.Configuration;
import com.aspectran.core.component.bean.annotation.RequestAsDelete;
import com.aspectran.core.component.bean.annotation.RequestAsGet;
import com.aspectran.core.component.bean.annotation.RequestAsPost;
import com.aspectran.core.component.bean.annotation.Transform;
import com.aspectran.core.context.rule.type.TransformType;
import com.aspectran.core.util.FilenameUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
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
@Configuration(namespace = "/examples/file-upload")
public class SimpleFileUploadAction {

    private Log log = LogFactory.getLog(SimpleFileUploadAction.class);

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
            log.debug("Uploaded File " + uploadedFile);

            if (uploadedFiles.size() > this.maxFiles) {
                Iterator<String> iter = uploadedFiles.keySet().iterator();
                int cnt = uploadedFiles.size() - this.maxFiles;
                while (cnt-- > 0) {
                    if (iter.hasNext()) {
                        UploadedFile removedFile = uploadedFiles.remove(iter.next());
                        log.debug("Remove Old File " + removedFile);
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

    @RequestAsPost("/files")
    @Transform(type = TransformType.JSON)
    @Action(id = "files")
    public Collection upload(Translet translet) throws IOException {
        if (!translet.getRequestAdapter().isMaxLengthExceeded()) {
            FileParameter fileParameter = translet.getFileParameter("file");
            if (fileParameter != null) {
                String key = UUID.randomUUID().toString();
                String ext = FilenameUtils.getExtension(fileParameter.getFileName());
                if (ext != null) {
                    key += "." + ext.toLowerCase();
                }
                UploadedFile uploadedFile = new UploadedFile();
                uploadedFile.setKey(key);
                uploadedFile.setFileName(fileParameter.getFileName());
                uploadedFile.setFileSize(fileParameter.getFileSize());
                uploadedFile.setHumanFileSize(StringUtils.convertToHumanFriendlyByteSize(fileParameter.getFileSize()));
                uploadedFile.setFileType((fileParameter.getContentType()));
                uploadedFile.setUrl("/examples/file-upload/files/" + key);
                uploadedFile.setBytes(fileParameter.getBytes());

                addUploadedFile(uploadedFile);

                List<UploadedFile> files = new ArrayList<>();
                files.add(uploadedFile);
                return files;
            }
        }
        return null;
    }

    @RequestAsGet("/files/${key}")
    public void serve(Translet translet) throws IOException {
        String key = translet.getParameter("key");
        UploadedFile uploadedFile = uploadedFiles.get(key);
        if (uploadedFile != null) {
            translet.getResponseAdapter().setContentType(uploadedFile.getFileType());
            translet.getResponseAdapter().setHeader("Content-disposition", "attachment; filename=\"" + uploadedFile.getFileName() + "\"");
            translet.getResponseAdapter().getOutputStream().write(uploadedFile.getBytes());
        } else {
            HttpStatusSetter.setStatus(HttpStatus.NOT_FOUND, translet);
        }
    }

    @RequestAsDelete("/files/${key}")
    public void delete(Translet translet) {
        String key = translet.getParameter("key");
        UploadedFile removedFile = removeUploadedFile(key);
        if (removedFile == null) {
            HttpStatusSetter.setStatus(HttpStatus.NOT_FOUND, translet);
        }
    }

    @RequestAsGet("/files")
    @Transform(type = TransformType.JSON)
    @Action(id = "files")
    public Collection list(Translet translet) {
        return uploadedFiles.values();
    }

}