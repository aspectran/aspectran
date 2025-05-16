/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.core.activity.response;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;

/**
 * The Class ResponseTemplate.
 *
 * <p>Created: 2020/07/26</p>
 */
public class ResponseTemplate implements Response, ResponseAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ResponseTemplate.class);

    private final ResponseAdapter responseAdapter;

    public ResponseTemplate(@NonNull ResponseAdapter responseAdapter) {
        this.responseAdapter = responseAdapter;
    }

    @Override
    public void respond(Activity activity) throws ResponseException {
        if (logger.isDebugEnabled()) {
            ToStringBuilder tsb = new ToStringBuilder("Response");
            tsb.appendForce("type", getResponseType());
            logger.debug(tsb.toString());
        }
    }

    @Override
    public ResponseType getResponseType() {
        return ResponseType.TEMPLATE;
    }

    @Override
    public <T> T getAdaptee() {
        return responseAdapter.getAdaptee();
    }

    @Override
    public String getHeader(String name) {
        return responseAdapter.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return responseAdapter.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return responseAdapter.getHeaderNames();
    }

    @Override
    public boolean containsHeader(String name) {
        return responseAdapter.containsHeader(name);
    }

    @Override
    public void setHeader(String name, String value) {
        responseAdapter.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        responseAdapter.addHeader(name, value);
    }

    @Override
    public String getEncoding() {
        return responseAdapter.getEncoding();
    }

    @Override
    public void setEncoding(String encoding) {
        responseAdapter.setEncoding(encoding);
    }

    @Override
    public String getContentType() {
        return responseAdapter.getContentType();
    }

    public void setContentType(String contentType) {
        responseAdapter.setContentType(contentType);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return responseAdapter.getOutputStream();
    }

    @Override
    public Writer getWriter() throws IOException {
        return responseAdapter.getWriter();
    }

    @Override
    public void commit() throws IOException {
        responseAdapter.commit();
    }

    @Override
    public void redirect(String location) throws IOException {
        responseAdapter.redirect(location);
    }

    @Override
    public RedirectTarget redirect(RedirectRule redirectRule) throws IOException {
        return responseAdapter.redirect(redirectRule);
    }

    @Override
    public int getStatus() {
        return responseAdapter.getStatus();
    }

    @Override
    public void setStatus(int status) {
        responseAdapter.setStatus(status);
    }

    @Override
    public String transformPath(String path) {
        return responseAdapter.transformPath(path);
    }

    @Override
    public Response replicate() {
        throw new UnsupportedOperationException();
    }

}
