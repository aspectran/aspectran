/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.web.activity.request.RequestHeaderParser;
import com.aspectran.web.support.http.HttpMediaTypeNotAcceptableException;
import com.aspectran.web.support.http.HttpStatus;
import com.aspectran.web.support.http.MediaType;
import com.aspectran.web.support.http.MediaTypeUtils;

import java.util.List;

/**
 * <p>Created: 2019-06-16</p>
 */
public abstract class AbstractRestResponse<T> implements RestResponse {

    private Object data;

    private boolean prettyPrint = true;

    private boolean favorPathExtension = true;

    private boolean ignoreUnknownPathExtensions = true;

    private boolean ignoreAcceptHeader = false;

    private MediaType defaultContentType;

    private int status;

    private String location;

    public AbstractRestResponse() {
    }

    public AbstractRestResponse(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    @Override
    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T prettyPrint(boolean prettyPrint) {
        setPrettyPrint(prettyPrint);
        return (T)this;
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
    @SuppressWarnings("unchecked")
    public T favorPathExtension(boolean favorPathExtension) {
        setFavorPathExtension(favorPathExtension);
        return (T)this;
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
    @SuppressWarnings("unchecked")
    public T ignoreUnknownPathExtensions(boolean ignoreUnknownPathExtensions) {
        setIgnoreUnknownPathExtensions(ignoreUnknownPathExtensions);
        return (T)this;
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
    @SuppressWarnings("unchecked")
    public T ignoreAcceptHeader(boolean ignoreAcceptHeader) {
        setIgnoreAcceptHeader(ignoreAcceptHeader);
        return (T)this;
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
        this.defaultContentType = MediaTypeUtils.parseMediaType(defaultContentType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T defaultContentType(MediaType defaultContentType) {
        setDefaultContentType(defaultContentType);
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T ok() {
        this.status = HttpStatus.OK.value();
        return (T)this;
    }

    @Override
    public T created() {
        return created(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T created(String location) {
        this.status = HttpStatus.CREATED.value();
        this.location = location;
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T accepted() {
        this.status = HttpStatus.ACCEPTED.value();
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T noContent() {
        this.status = HttpStatus.NO_CONTENT.value();
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T movedPermanently() {
        this.status = HttpStatus.MOVED_PERMANENTLY.value();
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T seeOther() {
        this.status = HttpStatus.SEE_OTHER.value();
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T notModified() {
        this.status = HttpStatus.NOT_MODIFIED.value();
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T temporaryRedirect() {
        this.status = HttpStatus.TEMPORARY_REDIRECT.value();
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T badRequest() {
        this.status = HttpStatus.BAD_REQUEST.value();
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T unauthorized() {
        this.status = HttpStatus.UNAUTHORIZED.value();
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T forbidden() {
        this.status = HttpStatus.FORBIDDEN.value();
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T notFound() {
        this.status = HttpStatus.NOT_FOUND.value();
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T methodNotAllowed() {
        this.status = HttpStatus.METHOD_NOT_ALLOWED.value();
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T notAcceptable() {
        this.status = HttpStatus.NOT_ACCEPTABLE.value();
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T conflict() {
        this.status = HttpStatus.CONFLICT.value();
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T preconditionFailed() {
        this.status = HttpStatus.PRECONDITION_FAILED.value();
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T unsupportedMediaType() {
        this.status = HttpStatus.UNSUPPORTED_MEDIA_TYPE.value();
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T internalServerError() {
        this.status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        return (T)this;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String getLocation() {
        return location;
    }

    protected MediaType determineContentType(RequestAdapter requestAdapter, List<MediaType> supportedContentTypes)
        throws HttpMediaTypeNotAcceptableException {
        List<MediaType> contentTypes = resolveContentTypes(requestAdapter);
        if (contentTypes != null) {
            for (MediaType mediaType : contentTypes) {
                if (mediaType.equals(MediaType.ALL) && getDefaultContentType() != null) {
                    if (supportedContentTypes.contains(getDefaultContentType())) {
                        return getDefaultContentType();
                    }
                }
                for (MediaType supportedContentType : supportedContentTypes) {
                    if (mediaType.includes(supportedContentType)) {
                        return supportedContentType;
                    }
                }
            }
        }
        if (supportedContentTypes.contains(getDefaultContentType())) {
            return getDefaultContentType();
        }
        throw new HttpMediaTypeNotAcceptableException(supportedContentTypes);
    }

    protected List<MediaType> resolveContentTypes(RequestAdapter requestAdapter) throws HttpMediaTypeNotAcceptableException {
        List<MediaType> contentTypes = null;
        if (isFavorPathExtension()) {
            //TODO
        }
        if (contentTypes == null && !isIgnoreAcceptHeader()) {
            contentTypes = RequestHeaderParser.resolveAcceptContentTypes(requestAdapter);
        }
        return contentTypes;
    }

}
