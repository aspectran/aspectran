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
package com.aspectran.core.adapter;

import com.aspectran.core.context.rule.RedirectResponseRule;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * The Class BasicResponseAdapter.
 * 
 * @since 2016. 2. 13.
 */
public class BasicResponseAdapter extends AbstractResponseAdapter {

    private String encoding;

    private String contentType;

    private OutputStream outputStream;

    private Writer writer;

    /**
     * Instantiates a new Basic response adapter.
     *
     * @param adaptee the adaptee object
     */
    public BasicResponseAdapter(Object adaptee) {
        super(adaptee);
    }

    /**
     * Instantiates a new Basic response adapter.
     *
     * @param adaptee the adaptee object
     * @param writer the writer to output
     */
    public BasicResponseAdapter(Object adaptee, Writer writer) {
        super(adaptee);
        setWriter(writer);
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            throw new UnsupportedOperationException();
        }
        return outputStream;
    }

    protected void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public Writer getWriter() throws IOException {
        if (writer == null) {
            throw new UnsupportedOperationException();
        }
        return writer;
    }

    protected void setWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void redirect(String target) throws IOException {
    }

    @Override
    public String redirect(RedirectResponseRule redirectResponseRule) {
        throw new UnsupportedOperationException("redirect");
    }

    @Override
    public void flush() {
        // nothing to do
    }

}
