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
import com.aspectran.core.activity.request.SizeLimitExceededException;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.FilenameUtils;
import com.aspectran.utils.LinkedMultiValueMap;
import com.aspectran.utils.MultiValueMap;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.web.activity.request.MultipartFormDataParser;
import com.aspectran.web.activity.request.MultipartRequestParseException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * Multipart form data parser that use Apache Commons FileUpload 1.4 or above.
 */
public class CommonsMultipartFormDataParser implements MultipartFormDataParser {

    private static final Logger logger = LoggerFactory.getLogger(CommonsMultipartFormDataParser.class);

    private String tempFileDir;

    private long maxRequestSize = -1L;

    private long maxFileSize = -1L;

    private int maxInMemorySize = -1;

    private String allowedFileExtensions;

    private String deniedFileExtensions;

    /**
     * Instantiates a new CommonsMultipartFormDataParser.
     */
    public CommonsMultipartFormDataParser() {
    }

    @Override
    public String getTempFileDir() {
        return tempFileDir;
    }

    @Override
    public void setTempFileDir(String tempFileDir) throws IOException {
        if (tempFileDir == null) {
            throw new IllegalArgumentException("tempFileDir must not be null");
        }
        File dir = new File(tempFileDir);
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IOException("Given tempFileDir [" + tempFileDir +
                        "] exists but is not a directory");
            }
        } else {
            if (!dir.mkdirs()) {
                throw new IOException("Given tempFileDir [" + tempFileDir +
                        "] could not be created");
            }
        }
        this.tempFileDir = tempFileDir;
    }

    @Override
    public void setMaxRequestSize(long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    @Override
    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    @Override
    public void setMaxInMemorySize(int maxInMemorySize) {
        this.maxInMemorySize = maxInMemorySize;
    }

    @Override
    public void setAllowedFileExtensions(String allowedFileExtensions) {
        this.allowedFileExtensions = allowedFileExtensions;
    }

    @Override
    public void setDeniedFileExtensions(String deniedFileExtensions) {
        this.deniedFileExtensions = deniedFileExtensions;
    }

    @Override
    public void parse(RequestAdapter requestAdapter) throws MultipartRequestParseException {
        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            if (maxInMemorySize > -1) {
                factory.setSizeThreshold(maxInMemorySize);
            }

            if (tempFileDir != null) {
                File dir = new File(tempFileDir);
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new IllegalArgumentException("Given tempFileDir [" +
                            tempFileDir + "] could not be created");
                }
                factory.setRepository(dir);
            }

            FileUpload upload = new ServletFileUpload(factory);
            upload.setHeaderEncoding(requestAdapter.getEncoding());
            if (maxRequestSize > -1L) {
                upload.setSizeMax(maxRequestSize);
            }
            if (maxFileSize > -1L) {
                upload.setFileSizeMax(maxFileSize);
            }

            Map<String, List<FileItem>> fileItemListMap;
            try {
                RequestContext requestContext = new CommonsRequestContext(requestAdapter);
                fileItemListMap = upload.parseParameterMap(requestContext);
            } catch (FileUploadBase.SizeLimitExceededException e) {
                throw new SizeLimitExceededException("Maximum request length exceeded; actual: " +
                        e.getActualSize() + "; permitted: " + e.getPermittedSize(),
                        e.getActualSize(), e.getPermittedSize());
            } catch (FileUploadBase.FileSizeLimitExceededException e) {
                throw new SizeLimitExceededException("Maximum file length exceeded; actual: " +
                        e.getActualSize() + "; permitted: " + e.getPermittedSize(),
                        e.getActualSize(), e.getPermittedSize());
            }
            parseMultipartParameters(fileItemListMap, requestAdapter);
        } catch (Exception e) {
            Throwable cause = ExceptionUtils.getRootCause(e);
            throw new MultipartRequestParseException("Failed to parse multipart request; Cause: " +
                ExceptionUtils.getSimpleMessage(cause), e);
        }
    }

    /**
     * Parse form fields and file items.
     * @param fileItemListMap the file item list map
     * @param requestAdapter the request adapter
     */
    private void parseMultipartParameters(@NonNull Map<String, List<FileItem>> fileItemListMap,
                                          @NonNull RequestAdapter requestAdapter) {
        String encoding = requestAdapter.getEncoding();
        MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<>();
        MultiValueMap<String, FileParameter> fileParameterMap = new LinkedMultiValueMap<>();

        for (Map.Entry<String, List<FileItem>> entry : fileItemListMap.entrySet()) {
            String fieldName = entry.getKey();
            List<FileItem> fileItemList = entry.getValue();

            if (fileItemList != null && !fileItemList.isEmpty()) {
                for (FileItem fileItem : fileItemList) {
                    if (fileItem.isFormField()) {
                        String value = getString(fileItem, encoding);
                        parameterMap.add(fieldName, value);
                    } else {
                        String fileName = fileItem.getName();

                        // Skip file uploads that don't have a file name - meaning that
                        // no file was selected.
                        if (StringUtils.isEmpty(fileName)) {
                            continue;
                        }

                        boolean valid = FilenameUtils.isValidFileExtension(fileName,
                                allowedFileExtensions, deniedFileExtensions);
                        if (!valid) {
                            continue;
                        }

                        CommonsMultipartFileParameter fileParameter = new CommonsMultipartFileParameter(fileItem);
                        fileParameterMap.add(fieldName, fileParameter);

                        if (logger.isDebugEnabled()) {
                            logger.debug("Found multipart file [{}] of size {} bytes, stored {}",
                                    fileParameter.getFileName(),
                                    fileParameter.getFileSize(),
                                    fileParameter.getStorageDescription());
                        }
                    }
                }
            }
        }

        requestAdapter.putAllParameters(parameterMap);
        requestAdapter.putAllFileParameters(fileParameterMap);
    }

    private String getString(FileItem fileItem, String encoding) {
        String value;
        if (encoding != null) {
            try {
                value = fileItem.getString(encoding);
            } catch (UnsupportedEncodingException ex) {
                logger.warn("Could not decode multipart item '{}' with encoding '{}': using platform default",
                        fileItem.getFieldName(), encoding);
                value = fileItem.getString();
            }
        } else {
            value = fileItem.getString();
        }
        return value;
    }

}
