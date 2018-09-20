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
package com.aspectran.web.support.multipart.inmemory;

import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.util.FilenameUtils;
import com.aspectran.core.util.LinkedMultiValueMap;
import com.aspectran.core.util.MultiValueMap;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.web.activity.request.MultipartFormDataParser;
import com.aspectran.web.activity.request.MultipartRequestParseException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * Multi-part form data parser that use Apache Commons FileUpload 1.3 or above.
 *
 * <p>It is designed to store uploaded data items in memory, such as GAE,
 * where the file system is not available.</p>
 */
public class MemoryMultipartFormDataParser implements MultipartFormDataParser {

    private static final Log log = LogFactory.getLog(MemoryMultipartFormDataParser.class);

    private long maxRequestSize = -1L;

    private long maxFileSize = -1L;

    private int maxInMemorySize = -1;

    private String allowedFileExtensions;

    private String deniedFileExtensions;

    /**
     * Instantiates a new MemoryMultipartFormDataParser.
     */
    public MemoryMultipartFormDataParser() {
    }

    @Override
    public String getTempDirectoryPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTempDirectoryPath(String tempDirectoryPath) {
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
    public void parse(RequestAdapter requestAdapter) {
        try {
            MemoryFileItemFactory factory = new MemoryFileItemFactory();
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
                RequestContext requestContext = createRequestContext(requestAdapter.getAdaptee());
                fileItemListMap = upload.parseParameterMap(requestContext);
            } catch (SizeLimitExceededException e) {
                log.warn("Maximum request length exceeded; multipart.maxRequestSize: " + maxRequestSize);
                requestAdapter.setMaxLengthExceeded(true);
                return;
            } catch (FileSizeLimitExceededException e) {
                log.warn("Maximum file length exceeded; multipart.maxFileSize: " + maxFileSize);
                requestAdapter.setMaxLengthExceeded(true);
                return;
            }
            parseMultipartParameters(fileItemListMap, requestAdapter);
        } catch (Exception e) {
            throw new MultipartRequestParseException("Could not parse multipart servlet request", e);
        }
    }

    /**
     * Parse form fields and file items.
     *
     * @param fileItemListMap the file item list map
     * @param requestAdapter the request adapter
     */
    private void parseMultipartParameters(Map<String, List<FileItem>> fileItemListMap, RequestAdapter requestAdapter) {
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

                        boolean valid = FilenameUtils.isValidFileExtension(fileName, allowedFileExtensions, deniedFileExtensions);
                        if (!valid) {
                            continue;
                        }

                        MemoryMultipartFileParameter fileParameter = new MemoryMultipartFileParameter(fileItem);
                        fileParameterMap.add(fieldName, fileParameter);

                        if (log.isDebugEnabled()) {
                            log.debug("Found multipart file [" + fileParameter.getFileName() + "] of size " +
                                    fileParameter.getFileSize() + " bytes, stored " +
                                    fileParameter.getStorageDescription());
                        }
                    }
                }
            }
        }

        if (!parameterMap.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : parameterMap.entrySet()) {
                String name = entry.getKey();
                List<String> list = entry.getValue();
                String[] values = list.toArray(new String[0]);
                requestAdapter.setParameter(name, values);
            }
        }

        if (!fileParameterMap.isEmpty()) {
            for (Map.Entry<String, List<FileParameter>> entry : fileParameterMap.entrySet()) {
                String name = entry.getKey();
                List<FileParameter> list = entry.getValue();
                FileParameter[] values = list.toArray(new FileParameter[0]);
                requestAdapter.setFileParameter(name, values);
            }
        }
    }

    private String getString(FileItem fileItem, String encoding) {
        String value;
        if (encoding != null) {
            try {
                value = fileItem.getString(encoding);
            } catch (UnsupportedEncodingException ex) {
                log.warn("Could not decode multipart item '" + fileItem.getFieldName() +
                        "' with encoding '" + encoding + "': using platform default");
                value = fileItem.getString();
            }
        } else {
            value = fileItem.getString();
        }
        return value;
    }

    /**
     * Creates a RequestContext needed by Jakarta Commons Upload.
     *
     * @param req the HTTP request
     * @return a new request context
     */
    private RequestContext createRequestContext(final HttpServletRequest req) {
        return new RequestContext() {
            @Override
            public String getCharacterEncoding() {
                return req.getCharacterEncoding();
            }

            @Override
            public String getContentType() {
                return req.getContentType();
            }

            @Override
            @Deprecated
            public int getContentLength() {
                return req.getContentLength();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return req.getInputStream();
            }
        };
    }

}
