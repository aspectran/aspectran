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

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.core.component.bean.annotation.Action;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Multipart;
import com.aspectran.core.component.bean.annotation.Qualifier;
import com.aspectran.core.component.bean.annotation.RequestToDelete;
import com.aspectran.core.component.bean.annotation.RequestToGet;
import com.aspectran.core.component.bean.annotation.RequestToPost;
import com.aspectran.core.component.bean.annotation.Transform;
import com.aspectran.core.context.rule.type.FormatType;
import com.aspectran.web.support.http.HttpStatus;
import com.aspectran.web.support.http.HttpStatusSetter;
import com.aspectran.web.support.util.WebUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            logger.debug("Uploaded File {}", uploadedFile);

            if (uploadedFiles.size() > this.maxFiles) {
                Iterator<String> it = uploadedFiles.keySet().iterator();
                int cnt = uploadedFiles.size() - this.maxFiles;
                while (cnt-- > 0) {
                    if (it.hasNext()) {
                        UploadedFile removedFile = uploadedFiles.remove(it.next());
                        logger.debug("Remove Old File {}", removedFile);
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
    @Multipart("standardFileUploader")
    public Collection<UploadedFile> upload(@Qualifier("file") FileParameter[] files) throws IOException {
        if (files != null && files.length > 0) {
            List<UploadedFile> uploadedFileList = new ArrayList<>(files.length);
            for (FileParameter file : files) {
                if (file.getFileSize() > 0) {
                    UploadedFile uploadedFile = UploadedFile.of(file);
                    if (uploadedFile != null) {
                        uploadedFile.setUrl("/examples/file-upload/files/" + uploadedFile.getKey());
                        addUploadedFile(uploadedFile);
                        uploadedFileList.add(uploadedFile);
                    }
                }
            }
            return (!uploadedFileList.isEmpty() ? uploadedFileList : null);
        } else {
            return null;
        }
    }

    @RequestToGet("/files/${key}")
    public void serve(@NonNull Translet translet) throws IOException {
        String key = translet.getParameter("key");
        UploadedFile uploadedFile = uploadedFiles.get(key);
        if (uploadedFile != null) {
            WebUtils.serveFile(translet, uploadedFile.getFileName(), uploadedFile.getFileType(), uploadedFile.getBytes());
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
