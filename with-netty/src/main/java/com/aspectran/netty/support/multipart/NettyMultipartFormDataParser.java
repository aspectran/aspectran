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
import com.aspectran.core.activity.request.SizeLimitExceededException;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.LinkedMultiValueMap;
import com.aspectran.utils.MultiValueMap;
import com.aspectran.utils.StringUtils;
import com.aspectran.web.activity.request.AbstractMultipartFormDataParser;
import com.aspectran.web.activity.request.MultipartFormDataParser;
import com.aspectran.web.activity.request.MultipartRequestParseException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Netty-native implementation of {@link MultipartFormDataParser}.
 * <p>Uses Netty's {@link HttpPostRequestDecoder} to parse multipart/form-data
 * efficiently with zero external library overhead.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public class NettyMultipartFormDataParser extends AbstractMultipartFormDataParser {

    /**
     * Constructs a new NettyMultipartFormDataParser.
     */
    public NettyMultipartFormDataParser() {
        super();
        setMaxInMemorySize(10240);
    }

    @Override
    public void setTempFileDir(String tempFileDir) throws IOException {
        super.setTempFileDir(tempFileDir);
        DiskFileUpload.baseDirectory = tempFileDir;
        DiskAttribute.baseDirectory = tempFileDir;
    }

    @Override
    public void parse(@NonNull RequestAdapter requestAdapter) throws MultipartRequestParseException {
        Object adaptee = requestAdapter.getAdaptee();
        if (!(adaptee instanceof FullHttpRequest request)) {
            throw new MultipartRequestParseException("Request adaptee is not a Netty FullHttpRequest: " + adaptee);
        }

        Charset charset = (requestAdapter.getEncoding() != null
                ? Charset.forName(requestAdapter.getEncoding()) : StandardCharsets.UTF_8);

        HttpDataFactory factory = new DefaultHttpDataFactory(getMaxInMemorySize());
        InterfaceHttpPostRequestDecoder decoder = null;
        try {
            long maxRequestSize = getMaxRequestSize();
            if (maxRequestSize >= 0L && request.content().readableBytes() > maxRequestSize) {
                throw new SizeLimitExceededException("Maximum request length exceeded; actual: " +
                        request.content().readableBytes() + "; permitted: " + maxRequestSize,
                        request.content().readableBytes(), maxRequestSize);
            }
            decoder = new HttpPostRequestDecoder(factory, request, charset);
            MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<>();
            MultiValueMap<String, FileParameter> fileParameterMap = new LinkedMultiValueMap<>();

            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                        Attribute attribute = (Attribute)data;
                        parameterMap.add(attribute.getName(), attribute.getValue());
                    } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                        FileUpload fileUpload = (FileUpload)data;
                        if (fileUpload.isCompleted()) {
                            String fileName = fileUpload.getFilename();
                            if (StringUtils.hasLength(fileName)) {
                                long maxFileSize = getMaxFileSize();
                                if (maxFileSize >= 0L && fileUpload.length() > maxFileSize) {
                                    throw new SizeLimitExceededException("Maximum file length exceeded; actual: " +
                                            fileUpload.length() + "; permitted: " + maxFileSize,
                                            fileUpload.length(), maxFileSize);
                                }
                                if (isValidFileExtension(fileName)) {
                                    decoder.removeHttpDataFromClean(fileUpload);
                                    NettyFileParameter fileParameter = new NettyFileParameter(fileUpload.retain());
                                    fileParameterMap.add(fileUpload.getName(), fileParameter);
                                }
                            }
                        }
                    }
                }
            }

            requestAdapter.putAllParameters(parameterMap);
            requestAdapter.putAllFileParameters(fileParameterMap);
        } catch (Exception e) {
            Throwable cause = ExceptionUtils.getRootCause(e);
            throw new MultipartRequestParseException("Failed to parse multipart request: " +
                    ExceptionUtils.getSimpleMessage(cause), e);
        } finally {
            if (decoder != null) {
                decoder.destroy();
            }
        }
    }

}
