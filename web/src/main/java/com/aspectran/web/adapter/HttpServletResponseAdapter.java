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
package com.aspectran.web.adapter;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.activity.response.transform.TransformResponse;
import com.aspectran.core.adapter.AbstractResponseAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.expr.ItemEvaluation;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.type.FormatType;
import com.aspectran.web.support.util.SendRedirectBasedOnXForwardedProtocol;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 * Adapt {@link HttpServletResponse} to Core {@link ResponseAdapter}.
 *
 * @since 2011. 3. 13.
 */
public class HttpServletResponseAdapter extends AbstractResponseAdapter {

    public static final String PROXY_PROTOCOL_AWARE_SETTING_NAME = "proxyProtocolAware";

    private static final char QUESTION_CHAR = '?';

    private static final char AMPERSAND_CHAR = '&';

    private static final char EQUAL_CHAR = '=';

    private final Activity activity;

    private boolean precommitDone;

    /**
     * Instantiates a new HttpServletResponseAdapter.
     * @param response the HTTP response
     * @param activity the activity
     */
    public HttpServletResponseAdapter(HttpServletResponse response, Activity activity) {
        super(response);
        this.activity = activity;
    }

    @Override
    public String getHeader(String name) {
        return ((HttpServletResponse)getAdaptee()).getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return ((HttpServletResponse)getAdaptee()).getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return ((HttpServletResponse)getAdaptee()).getHeaderNames();
    }

    @Override
    public boolean containsHeader(String name) {
        return ((HttpServletResponse)getAdaptee()).containsHeader(name);
    }

    @Override
    public void setHeader(String name, String value) {
        ((HttpServletResponse)getAdaptee()).setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        ((HttpServletResponse)getAdaptee()).addHeader(name, value);
    }

    @Override
    public String getEncoding() {
        return ((HttpServletResponse)getAdaptee()).getCharacterEncoding();
    }

    @Override
    public void setEncoding(String encoding) throws UnsupportedEncodingException {
        ((HttpServletResponse)getAdaptee()).setCharacterEncoding(encoding);
    }

    @Override
    public String getContentType() {
        return ((HttpServletResponse)getAdaptee()).getContentType();
    }

    @Override
    public void setContentType(String contentType) {
        ((HttpServletResponse)getAdaptee()).setContentType(contentType);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        precommit();
        return ((HttpServletResponse)getAdaptee()).getOutputStream();
    }

    @Override
    public Writer getWriter() throws IOException {
        precommit();
        return ((HttpServletResponse)getAdaptee()).getWriter();
    }

    @Override
    public void flush() throws IOException {
        if (((HttpServletResponse)getAdaptee()).isCommitted()) {
            ((HttpServletResponse)getAdaptee()).flushBuffer();
        }
    }

    @Override
    public void redirect(String location) throws IOException {
        boolean proxyProtocolAware = Boolean.parseBoolean(activity.getSetting(PROXY_PROTOCOL_AWARE_SETTING_NAME));
        if (proxyProtocolAware) {
            location = SendRedirectBasedOnXForwardedProtocol.getLocation(activity.getTranslet(), location);
        }
        ((HttpServletResponse)getAdaptee()).sendRedirect(location);
    }

    @Override
    public String redirect(RedirectRule redirectRule) throws IOException {
        String path = makeRedirectPath(redirectRule, activity);
        redirect(path);
        return path;
    }

    @Override
    public int getStatus() {
        return ((HttpServletResponse)getAdaptee()).getStatus();
    }

    @Override
    public void setStatus(int status) {
        ((HttpServletResponse)getAdaptee()).setStatus(status);
    }

    private void precommit() throws IOException {
        if (!precommitDone) {
            precommitDone = true;
            Response response = activity.getDeclaredResponse();
            if (response instanceof TransformResponse) {
                FormatType formatType = ((TransformResponse)response).getFormatType();
                if (formatType == null) {
                    try {
                        response.commit(activity);
                    } catch (ResponseException e) {
                        throw new IOException("Error during precommit", e);
                    }
                }
            }
        }
    }

    public static String makeRedirectPath(RedirectRule redirectRule, Activity activity) throws IOException {
        if (redirectRule == null) {
            throw new IllegalArgumentException("redirectRule must not be null");
        }
        if (redirectRule.getEncoding() == null) {
            redirectRule.setEncoding(StandardCharsets.ISO_8859_1.name());
        }
        String path = redirectRule.getPath(activity);
        int questionPos = -1;
        StringBuilder sb = new StringBuilder(256);
        if (path != null) {
            sb.append(path);
            questionPos = path.indexOf(QUESTION_CHAR);
        }
        ItemRuleMap parameterItemRuleMap = redirectRule.getParameterItemRuleMap();
        if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
            ItemEvaluator evaluator = new ItemEvaluation(activity);
            Map<String, Object> valueMap = evaluator.evaluate(parameterItemRuleMap);
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
                    String stringValue = (value != null ? value.toString() : null);
                    if (redirectRule.isExcludeEmptyParameters() &&
                        stringValue != null && !stringValue.isEmpty()) {
                        sb.append(name).append(EQUAL_CHAR);
                    } else if (redirectRule.isExcludeNullParameters() && stringValue != null) {
                        sb.append(name).append(EQUAL_CHAR);
                    } else {
                        sb.append(name).append(EQUAL_CHAR);
                    }
                    if (stringValue != null) {
                        stringValue = URLEncoder.encode(stringValue, redirectRule.getEncoding());
                        sb.append(stringValue);
                    }
                }
            }
        }
        return sb.toString();
    }

}
