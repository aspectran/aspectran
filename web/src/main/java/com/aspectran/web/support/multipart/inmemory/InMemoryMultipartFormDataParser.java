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
package com.aspectran.web.support.multipart.inmemory;

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
import com.aspectran.web.support.multipart.commons.CommonsRequestContext;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * Multipart form data parser that use Apache Commons FileUpload 1.3 or above.
 *
 * <p>It is designed to store uploaded data items in memory, such as GAE,
 * where the file system is not available.</p>
 */
public class InMemoryMultipartFormDataParser implements MultipartFormDataParser {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryMultipartFormDataParser.class);

    private long maxRequestSize = -1L;

    private long maxFileSize = -1L;

    private int maxInMemorySize = -1;

    private String allowedFileExtensions;

    private String deniedFileExtensions;

    /**
     * Instantiates a new InMemoryMultipartFormDataParser.
     */
    public InMemoryMultipartFormDataParser() {
    }

    @Override
    public String getTempFileDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTempFileDir(String tempFileDir) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMaxRequestSize(long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    @Override
    public void setMaxFileSize(long maxFileSize) {
        if (maxFileSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("In the In-Memory file uploads," +
                    "the maximum file size is up to 2147483648 bytes");
        }

        this.maxFileSize = maxFileSize;
        this.maxInMemorySize = (int)maxFileSize;
    }

    @Override
    public void setMaxInMemorySize(int maxInMemorySize) {
        throw new UnsupportedOperationException();
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
            InMemoryFileItemFactory factory = new InMemoryFileItemFactory();
            if (maxInMemorySize > -1) {
                factory.setSizeThreshold(maxInMemorySize);
            }

            FileUpload upload = new ServletFileUpload(factory);
            upload.setHeaderEncoding(requestAdapter.getEncoding());
            if (maxRequestSize > -1) {
                upload.setSizeMax(maxRequestSize);
            }
            if (maxFileSize > -1) {
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

                        InMemoryMultipartFileParameter fileParameter = new InMemoryMultipartFileParameter(fileItem);
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
