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
package com.aspectran.web.activity.response;

import com.aspectran.core.activity.Activity;
import com.aspectran.utils.Assert;
import com.aspectran.utils.FilenameUtils;
import com.aspectran.utils.LinkedCaseInsensitiveMultiValueMap;
import com.aspectran.utils.MultiValueMap;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.web.activity.request.RequestHeaderParser;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpMediaTypeNotAcceptableException;
import com.aspectran.web.support.http.HttpStatus;
import com.aspectran.web.support.http.MediaType;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;

/**
 * Abstract base class for {@link RestResponse} implementations.
 *
 * <p>This class provides the basic structure for managing response data,
 * headers, status, and content negotiation settings. Subclasses must implement
 * {@link #getSupportedContentTypes()} and {@link #getContentTypeByPathExtension(String)}
 * to define the supported media types.
 *
 * <p>Created: 2019-06-16</p>
 */
public abstract class AbstractRestResponse implements RestResponse {

    private String name;

    private Object data;

    private StringifyContext stringifyContext;

    private boolean favorPathExtension = true;

    private boolean ignoreUnknownPathExtensions = true;

    private boolean ignoreAcceptHeader = false;

    private MediaType defaultContentType;

    private int status;

    private MultiValueMap<String, String> headers;

    /**
     * Instantiates a new {@code AbstractRestResponse}.
     */
    public AbstractRestResponse() {
    }

    /**
     * Instantiates a new {@code AbstractRestResponse} with the given data.
     * @param data the response data
     */
    public AbstractRestResponse(Object data) {
        this(null, data);
    }

    /**
     * Instantiates a new {@code AbstractRestResponse} with the given name and data.
     * @param name the name of the response data
     * @param data the response data
     */
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

    @Override
    @Nullable
    public StringifyContext getStringifyContext() {
        return stringifyContext;
    }

    @Override
    @NonNull
    public StringifyContext touchStringifyContext() {
        if (stringifyContext == null) {
            stringifyContext = new StringifyContext();
        }
        return stringifyContext;
    }

    @Override
    public void setStringifyContext(StringifyContext stringifyContext) {
        this.stringifyContext = stringifyContext;
    }

    @Override
    public RestResponse stringifyContext(StringifyContext stringifyContext) {
        setStringifyContext(stringifyContext);
        return this;
    }

    @Override
    public RestResponse prettyPrint(boolean prettyPrint) {
        touchStringifyContext().setPrettyPrint(prettyPrint);
        return this;
    }

    @Override
    public RestResponse nullWritable(boolean nullWritable) {
        touchStringifyContext().setNullWritable(nullWritable);
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

    /**
     * Determines the most appropriate content type based on the request, using content negotiation.
     * The negotiation process is as follows:
     * <ol>
     *     <li>If {@code favorPathExtension} is true, it first checks the request's path extension.
     *     If a supported media type is found, it is returned immediately.</li>
     *     <li>If no content type is found via the path extension or if path extension is not favored,
     *     it checks the {@code Accept} header (unless {@code ignoreAcceptHeader} is true).</li>
     *     <li>If a compatible type is found in the {@code Accept} header, it is returned.</li>
     *     <li>If no compatible type is found, and a {@code defaultContentType} is configured and supported,
     *     the default is returned.</li>
     * </ol>
     * @param activity the current activity
     * @return the resolved content type
     * @throws HttpMediaTypeNotAcceptableException if no acceptable content type can be found
     */
    protected MediaType determineAcceptContentType(@NonNull Activity activity)
            throws HttpMediaTypeNotAcceptableException {
        // 1. Check path extension
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

        // 2. Check 'Accept' header
        if (!isIgnoreAcceptHeader()) {
            List<MediaType> acceptContentTypes = RequestHeaderParser.resolveAcceptContentTypes(activity.getRequestAdapter());
            for (MediaType contentType : acceptContentTypes) {
                if (getDefaultContentType() != null &&
                        getSupportedContentTypes().contains(getDefaultContentType()) &&
                        contentType.equalsTypeAndSubtype(MediaType.ALL)) {
                    return getDefaultContentType();
                }
                for (MediaType supportedContentType : getSupportedContentTypes()) {
                    if (contentType.includes(supportedContentType)) {
                        return supportedContentType;
                    }
                }
            }
            // 3. Check default content type
            if (getDefaultContentType() != null &&
                    getSupportedContentTypes().contains(getDefaultContentType())) {
                return getDefaultContentType();
            }
        }

        // 4. Not acceptable
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
