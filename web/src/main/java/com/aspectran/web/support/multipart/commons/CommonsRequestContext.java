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

import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.web.support.http.HttpHeaders;
import org.apache.commons.fileupload.RequestContext;

import java.io.IOException;
import java.io.InputStream;

/**
 * An adapter that wraps an {@link RequestAdapter} to provide a
 * {@link RequestContext} for the Apache Commons FileUpload library.
 *
 * <p>Created: 2019-07-31</p>
 *
 * @since 6.3.0
 */
public class CommonsRequestContext implements RequestContext {

    private final RequestAdapter requestAdapter;

    public CommonsRequestContext(RequestAdapter requestAdapter) {
        this.requestAdapter = requestAdapter;
    }

    @Override
    public String getCharacterEncoding() {
        return requestAdapter.getEncoding();
    }

    @Override
    public String getContentType() {
        return requestAdapter.getHeader(HttpHeaders.CONTENT_TYPE);
    }

    @Override
    @Deprecated
    public int getContentLength() {
        String contentLength = requestAdapter.getHeader(HttpHeaders.CONTENT_LENGTH);
        return (contentLength != null && !contentLength.isEmpty() ? Integer.parseInt(contentLength) : -1);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return requestAdapter.getInputStream();
    }

}
