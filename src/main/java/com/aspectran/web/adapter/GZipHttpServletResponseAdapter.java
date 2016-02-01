/**
 * Copyright 2008-2016 Juho Jeong
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
package com.aspectran.web.adapter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

/**
 * The Class GZipHttpServletResponseAdapter.
 *
 * <p>Created: 2016. 1. 31.</p>
 *
 * @since 2.0.0
 * @author Juho Jeong
 */
public class GZipHttpServletResponseAdapter extends HttpServletResponseAdapter {

    /**
     * Instantiates a new GZipHttpServletResponseAdapter.
     *
     * @param response the HTTP response
     */
    public GZipHttpServletResponseAdapter(HttpServletResponse response) {
        super(response);
        response.setHeader("Content-Encoding", "gzip");
    }

    /* (non-Javadoc)
     * @see com.aspectran.core.adapter.ResponseAdapter#getOutputStream()
     */
    public OutputStream getOutputStream() throws IOException {
        OutputStream os = ((HttpServletResponse)adaptee).getOutputStream();
        return  new FlushableGZIPOutputStream(os);
    }

    /* (non-Javadoc)
     * @see com.aspectran.core.adapter.ResponseAdapter#getWriter()
     */
    public Writer getWriter() throws IOException {
        String characterEncoding = getCharacterEncoding();

        OutputStream os = getOutputStream();
        Writer writer;

        if(characterEncoding != null)
            writer = new OutputStreamWriter(os, characterEncoding);
        else
            writer = new OutputStreamWriter(os);

        return writer;
    }

}
