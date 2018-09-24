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
package com.aspectran.web.adapter;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.transform.TransformResponse;
import com.aspectran.core.adapter.AbstractResponseAdapter;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpressionParser;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.TransformType;
import com.aspectran.web.activity.response.GZipServletResponseWrapper;
import com.aspectran.web.support.http.HttpHeaders;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;

/**
 * The Class HttpServletResponseAdapter.
 * 
 * @since 2011. 3. 13.
 */
public class HttpServletResponseAdapter extends AbstractResponseAdapter {

    private static final char QUESTION_CHAR = '?';

    private static final char AMPERSAND_CHAR = '&';

    private static final char EQUAL_CHAR = '=';

    private final Activity activity;

    private volatile HttpServletResponse response;

    /**
     * Instantiates a new HttpServletResponseAdapter.
     *
     * @param response the HTTP response
     * @param activity the activity
     */
    public HttpServletResponseAdapter(HttpServletResponse response, Activity activity) {
        super(response);
        this.activity = activity;
    }

    @Override
    @SuppressWarnings("unchecked")
    public HttpServletResponse getAdaptee() {
        if (response == null) {
            if (!activity.isIncluded() && isGzipAccepted()) {
                response = new GZipServletResponseWrapper(super.getAdaptee(), () -> {
                    ((HttpServletResponse)super.getAdaptee()).setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
                    // indicate to the client that the servlet varies it's
                    // output depending on the "Accept-Encoding" header
                    ((HttpServletResponse)super.getAdaptee()).setHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
                });
            } else {
                response = super.getAdaptee();
            }
        }
        return response;
    }

    @Override
    public String getHeader(String name) {
        return getAdaptee().getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return getAdaptee().getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return getAdaptee().getHeaderNames();
    }

    @Override
    public boolean containsHeader(String name) {
        return getAdaptee().containsHeader(name);
    }

    @Override
    public void setHeader(String name, String value) {
        getAdaptee().setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        getAdaptee().addHeader(name, value);
    }

    @Override
    public String getEncoding() {
        return getAdaptee().getCharacterEncoding();
    }

    @Override
    public void setEncoding(String encoding) throws UnsupportedEncodingException {
        getAdaptee().setCharacterEncoding(encoding);
    }

    @Override
    public String getContentType() {
        return getAdaptee().getContentType();
    }

    @Override
    public void setContentType(String contentType) {
        getAdaptee().setContentType(contentType);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        precommit();
        return getAdaptee().getOutputStream();
    }

    @Override
    public Writer getWriter() throws IOException {
        precommit();
        return getAdaptee().getWriter();
    }

    @Override
    public void flush() throws IOException {
        getAdaptee().flushBuffer();
    }

    @Override
    public void redirect(String path) throws IOException {
        getAdaptee().sendRedirect(path);
    }

    @Override
    public String redirect(RedirectResponseRule redirectResponseRule) throws IOException {
        String encoding = getAdaptee().getCharacterEncoding();
        String path = redirectResponseRule.getPath(activity);
        int questionPos = -1;

        StringBuilder sb = new StringBuilder(256);
        if (path != null) {
            sb.append(path);
            questionPos = path.indexOf(QUESTION_CHAR);
        }

        if (redirectResponseRule.getParameterItemRuleMap() != null) {
            ItemEvaluator evaluator = new ItemExpressionParser(activity);
            Map<String, Object> valueMap = evaluator.evaluate(redirectResponseRule.getParameterItemRuleMap());
            if (valueMap != null && !valueMap.isEmpty()) {
                if (questionPos == -1) {
                    sb.append(QUESTION_CHAR);
                }

                String name = null;
                Object value;
                for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                    if (name != null) {
                        sb.append(AMPERSAND_CHAR);
                    }

                    name = entry.getKey();
                    value = entry.getValue();
                    String string = (value != null ? value.toString() : null);
                    if (redirectResponseRule.isExcludeEmptyParameter() && string != null && !string.isEmpty()) {
                        sb.append(name).append(EQUAL_CHAR);
                    } else if (redirectResponseRule.isExcludeNullParameter() && string != null) {
                        sb.append(name).append(EQUAL_CHAR);
                    } else {
                        sb.append(name).append(EQUAL_CHAR);
                    }
                    if (string != null) {
                        string = URLEncoder.encode(string, encoding);
                        sb.append(string);
                    }
                }
            }
        }

        path = sb.toString();
        redirect(path);
        return path;
    }

    @Override
    public int getStatus() {
        return getAdaptee().getStatus();
    }

    @Override
    public void setStatus(int status) {
        getAdaptee().setStatus(status);
    }

    private void precommit() {
        Response response = activity.getDeclaredResponse();
        if (response != null && response.getResponseType() == ResponseType.TRANSFORM) {
            TransformType transformType = ((TransformResponse)response).getTransformType();
            if (transformType == null) {
                response.commit(activity);
            }
        }
    }

    private boolean isGzipAccepted() {
        String contentEncoding = activity.getSetting(ResponseRule.CONTENT_ENCODING_SETTING_NAME);
        if (contentEncoding != null) {
            String acceptEncoding = activity.getRequestAdapter().getHeader(HttpHeaders.ACCEPT_ENCODING);
            if (acceptEncoding != null) {
                return acceptEncoding.contains(contentEncoding);
            }
        }
        return false;
    }

}
