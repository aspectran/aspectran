/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.web.activity.response;

import com.aspectran.core.activity.Activity;
import com.aspectran.utils.Assert;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.FilenameUtils;
import com.aspectran.utils.LinkedCaseInsensitiveMultiValueMap;
import com.aspectran.utils.MultiValueMap;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.web.activity.request.RequestHeaderParser;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpMediaTypeNotAcceptableException;
import com.aspectran.web.support.http.HttpStatus;
import com.aspectran.web.support.http.MediaType;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;

/**
 * Abstract class shared by RestResponse.
 *
 * <p>Created: 2019-06-16</p>
 */
public abstract class AbstractRestResponse implements RestResponse {

    private String name;

    private Object data;

    private Boolean prettyPrint;

    private boolean favorPathExtension = true;

    private boolean ignoreUnknownPathExtensions = true;

    private boolean ignoreAcceptHeader = false;

    private MediaType defaultContentType;

    private int status;

    private MultiValueMap<String, String> headers;

    public AbstractRestResponse() {
    }

    public AbstractRestResponse(Object data) {
        this(null, data);
    }

    public AbstractRestResponse(String name, Object data) {
        setData(name, data);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public boolean hasData() {
        return (data != null);
    }

    @Override
    public RestResponse setData(Object data) {
        return setData(null, data);
    }

    @Override
    public RestResponse setData(String name, Object data) {
        if (name != null) {
            name = name.trim();
            if (name.isEmpty()) {
                name = null;
            }
        }
        this.name = name;
        this.data = data;
        return this;
    }

    protected boolean hasPrettyPrint() {
        return (prettyPrint != null);
    }

    @Override
    public boolean isPrettyPrint() {
        return BooleanUtils.toBoolean(prettyPrint);
    }

    @Override
    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    @Override
    public RestResponse prettyPrint(boolean prettyPrint) {
        setPrettyPrint(prettyPrint);
        return this;
    }

    @Override
    public boolean isFavorPathExtension() {
        return favorPathExtension;
    }

    @Override
    public void setFavorPathExtension(boolean favorPathExtension) {
        this.favorPathExtension = favorPathExtension;
    }

    @Override
    public RestResponse favorPathExtension(boolean favorPathExtension) {
        setFavorPathExtension(favorPathExtension);
        return this;
    }

    @Override
    public boolean isIgnoreUnknownPathExtensions() {
        return ignoreUnknownPathExtensions;
    }

    @Override
    public void setIgnoreUnknownPathExtensions(boolean ignoreUnknownPathExtensions) {
        this.ignoreUnknownPathExtensions = ignoreUnknownPathExtensions;
    }

    @Override
    public RestResponse ignoreUnknownPathExtensions(boolean ignoreUnknownPathExtensions) {
        setIgnoreUnknownPathExtensions(ignoreUnknownPathExtensions);
        return this;
    }

    @Override
    public boolean isIgnoreAcceptHeader() {
        return ignoreAcceptHeader;
    }

    @Override
    public void setIgnoreAcceptHeader(boolean ignoreAcceptHeader) {
        this.ignoreAcceptHeader = ignoreAcceptHeader;
    }

    @Override
    public RestResponse ignoreAcceptHeader(boolean ignoreAcceptHeader) {
        setIgnoreAcceptHeader(ignoreAcceptHeader);
        return this;
    }

    @Override
    public MediaType getDefaultContentType() {
        return defaultContentType;
    }

    @Override
    public void setDefaultContentType(MediaType defaultContentType) {
        this.defaultContentType = defaultContentType;
    }

    @Override
    public void setDefaultContentType(String defaultContentType) {
        this.defaultContentType = MediaType.parseMediaType(defaultContentType);
    }

    @Override
    public RestResponse defaultContentType(MediaType defaultContentType) {
        setDefaultContentType(defaultContentType);
        return this;
    }

    @Override
    public RestResponse ok() {
        this.status = HttpStatus.OK.value();
        return this;
    }

    @Override
    public RestResponse created() {
        return created(null);
    }

    @Override
    public RestResponse created(String location) {
        this.status = HttpStatus.CREATED.value();
        setHeader(HttpHeaders.LOCATION, location);
        return this;
    }

    @Override
    public RestResponse accepted() {
        this.status = HttpStatus.ACCEPTED.value();
        return this;
    }

    @Override
    public RestResponse noContent() {
        this.status = HttpStatus.NO_CONTENT.value();
        return this;
    }

    @Override
    public RestResponse movedPermanently() {
        this.status = HttpStatus.MOVED_PERMANENTLY.value();
        return this;
    }

    @Override
    public RestResponse seeOther() {
        this.status = HttpStatus.SEE_OTHER.value();
        return this;
    }

    @Override
    public RestResponse notModified() {
        this.status = HttpStatus.NOT_MODIFIED.value();
        return this;
    }

    @Override
    public RestResponse temporaryRedirect() {
        this.status = HttpStatus.TEMPORARY_REDIRECT.value();
        return this;
    }

    @Override
    public RestResponse badRequest() {
        this.status = HttpStatus.BAD_REQUEST.value();
        return this;
    }

    @Override
    public RestResponse unauthorized() {
        this.status = HttpStatus.UNAUTHORIZED.value();
        return this;
    }

    @Override
    public RestResponse forbidden() {
        this.status = HttpStatus.FORBIDDEN.value();
        return this;
    }

    @Override
    public RestResponse notFound() {
        this.status = HttpStatus.NOT_FOUND.value();
        return this;
    }

    @Override
    public RestResponse methodNotAllowed() {
        this.status = HttpStatus.METHOD_NOT_ALLOWED.value();
        return this;
    }

    @Override
    public RestResponse notAcceptable() {
        this.status = HttpStatus.NOT_ACCEPTABLE.value();
        return this;
    }

    @Override
    public RestResponse conflict() {
        this.status = HttpStatus.CONFLICT.value();
        return this;
    }

    @Override
    public RestResponse preconditionFailed() {
        this.status = HttpStatus.PRECONDITION_FAILED.value();
        return this;
    }

    @Override
    public RestResponse unsupportedMediaType() {
        this.status = HttpStatus.UNSUPPORTED_MEDIA_TYPE.value();
        return this;
    }

    @Override
    public RestResponse internalServerError() {
        this.status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        return this;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public RestResponse setStatus(int status) {
        this.status = status;
        return this;
    }

    @Override
    public RestResponse setStatus(HttpStatus status) {
        Assert.notNull(status, "'status' must not be null");
        this.status = status.value();
        return this;
    }

    @Override
    public RestResponse setHeader(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Header name must not be null");
        }
        touchHeaders().set(name, value);
        return this;
    }

    @Override
    public RestResponse addHeader(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Header name must not be null");
        }
        touchHeaders().add(name, value);
        return this;
    }

    protected MultiValueMap<String, String> getHeaders() {
        return headers;
    }

    private MultiValueMap<String, String> touchHeaders() {
        if (headers == null) {
            headers = new LinkedCaseInsensitiveMultiValueMap<>();
        }
        return headers;
    }

    protected abstract List<MediaType> getSupportedContentTypes();

    protected abstract MediaType getContentTypeByPathExtension(String extension);

    protected MediaType determineAcceptContentType(@NonNull Activity activity)
            throws HttpMediaTypeNotAcceptableException {
        if (isFavorPathExtension()) {
            String path = activity.getTranslet().getRequestName();
            String ext = FilenameUtils.getExtension(path);
            if (StringUtils.hasLength(ext)) {
                ext = ext.toLowerCase(Locale.ENGLISH);
                MediaType contentType = getContentTypeByPathExtension(ext);
                if (contentType != null) {
                    return contentType;
                }
            }
            if (!isIgnoreUnknownPathExtensions()) {
                throw new HttpMediaTypeNotAcceptableException(getSupportedContentTypes());
            }
        }
        if (!isIgnoreAcceptHeader()) {
            List<MediaType> acceptContentTypes = RequestHeaderParser.resolveAcceptContentTypes(activity.getRequestAdapter());
            for (MediaType contentType : acceptContentTypes) {
                if (contentType.equalsTypeAndSubtype(MediaType.ALL) &&
                        getDefaultContentType() != null &&
                        getSupportedContentTypes().contains(getDefaultContentType())) {
                    return getDefaultContentType();
                }
                for (MediaType supportedContentType : getSupportedContentTypes()) {
                    if (contentType.includes(supportedContentType)) {
                        return contentType;
                    }
                }
            }
            if (getSupportedContentTypes().contains(getDefaultContentType())) {
                return getDefaultContentType();
            }
        }
        throw new HttpMediaTypeNotAcceptableException(getSupportedContentTypes());
    }

    protected MediaType determineResponseContentType(@NonNull Activity activity, @NonNull MediaType acceptContentType) {
        Charset charset = acceptContentType.getCharset();
        if (charset == null) {
            String encoding = determineIntendedEncoding(activity);
            if (encoding != null) {
                charset = Charset.forName(encoding);
            }
        }
        if (charset != null) {
            return new MediaType(acceptContentType.getType(), acceptContentType.getSubtype(), charset);
        } else {
            return new MediaType(acceptContentType.getType(), acceptContentType.getSubtype());
        }
    }

    protected String determineIntendedEncoding(@NonNull Activity activity) {
        return activity.getTranslet().getDefinitiveResponseEncoding();
    }

}
