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
package com.aspectran.web.servlet.support.multipart.standard;

import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.core.activity.request.SizeLimitExceededException;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.FilenameUtils;
import com.aspectran.utils.LinkedMultiValueMap;
import com.aspectran.utils.MultiValueMap;
import com.aspectran.utils.StringUtils;
import com.aspectran.web.activity.request.MultipartFormDataParser;
import com.aspectran.web.activity.request.MultipartRequestParseException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * Standard Jakarta Servlet {@link Part} based {@link MultipartFormDataParser} implementation.
 * <p>Uses standard {@link HttpServletRequest#getParts()} provided by Servlet 3.0+ containers
 * (Tomcat, Jetty, Undertow, etc.) without requiring external third-party libraries.</p>
 *
 * @since 9.7.0
 */
public class StandardServletMultipartFormDataParser implements MultipartFormDataParser {

    private static final Logger logger = LoggerFactory.getLogger(StandardServletMultipartFormDataParser.class);

    private String tempFileDir;

    private long maxRequestSize = -1L;

    private long maxFileSize = -1L;

    private int maxInMemorySize = -1;

    private String allowedFileExtensions;

    private String deniedFileExtensions;

    /**
     * Constructs a new StandardServletMultipartFormDataParser.
     */
    public StandardServletMultipartFormDataParser() {
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
                throw new IOException("Given tempFileDir [" + tempFileDir + "] exists but is not a directory");
            }
        } else {
            if (!dir.mkdirs()) {
                throw new IOException("Given tempFileDir [" + tempFileDir + "] could not be created");
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
    public void parse(@NonNull RequestAdapter requestAdapter) throws MultipartRequestParseException {
        Object adaptee = requestAdapter.getAdaptee();
        if (!(adaptee instanceof HttpServletRequest request)) {
            throw new MultipartRequestParseException("Request adaptee is not an HttpServletRequest: " + adaptee);
        }

        try {
            long contentLength = request.getContentLengthLong();
            if (maxRequestSize >= 0L && contentLength > maxRequestSize) {
                throw new SizeLimitExceededException("Maximum request length exceeded; actual: " +
                        contentLength + "; permitted: " + maxRequestSize,
                        contentLength, maxRequestSize);
            }

            Collection<Part> parts;
            try {
                parts = request.getParts();
            } catch (Exception e) {
                Throwable rootCause = ExceptionUtils.getRootCause(e);
                String rootMsg = (rootCause != null ? rootCause.getMessage() : e.getMessage());
                if (rootMsg != null && rootMsg.toLowerCase().contains("size")) {
                    throw new SizeLimitExceededException("Multipart upload size limit exceeded: " + rootMsg, e);
                }
                throw e;
            }

            MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<>();
            MultiValueMap<String, FileParameter> fileParameterMap = new LinkedMultiValueMap<>();
            String encoding = requestAdapter.getEncoding();

            for (Part part : parts) {
                String submittedFileName = part.getSubmittedFileName();
                if (submittedFileName == null) {
                    // Form field
                    String value = getValueAsString(part, encoding);
                    parameterMap.add(part.getName(), value);
                } else {
                    // File upload
                    if (StringUtils.isEmpty(submittedFileName)) {
                        continue;
                    }
                    if (maxFileSize >= 0L && part.getSize() > maxFileSize) {
                        throw new SizeLimitExceededException("Maximum file length exceeded; actual: " +
                                part.getSize() + "; permitted: " + maxFileSize,
                                part.getSize(), maxFileSize);
                    }
                    if (!FilenameUtils.isValidFileExtension(submittedFileName,
                            allowedFileExtensions, deniedFileExtensions)) {
                        continue;
                    }

                    StandardServletMultipartFileParameter fileParameter = new StandardServletMultipartFileParameter(part);
                    fileParameterMap.add(part.getName(), fileParameter);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Found multipart file [{}] of size {} bytes",
                                fileParameter.getFileName(), fileParameter.getFileSize());
                    }
                }
            }

            requestAdapter.putAllParameters(parameterMap);
            requestAdapter.putAllFileParameters(fileParameterMap);
        } catch (SizeLimitExceededException e) {
            throw new MultipartRequestParseException(e.getMessage(), e);
        } catch (Exception e) {
            Throwable cause = ExceptionUtils.getRootCause(e);
            throw new MultipartRequestParseException("Failed to parse multipart request: " +
                    ExceptionUtils.getSimpleMessage(cause), e);
        }
    }

    private String getValueAsString(Part part, String encoding) throws IOException {
        byte[] bytes;
        try (InputStream in = part.getInputStream()) {
            bytes = in.readAllBytes();
        }
        if (encoding != null) {
            try {
                return new String(bytes, encoding);
            } catch (UnsupportedEncodingException ex) {
                logger.warn("Could not decode multipart item '{}' with encoding '{}': using UTF-8 default",
                        part.getName(), encoding);
                return new String(bytes, StandardCharsets.UTF_8);
            }
        } else {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

}
