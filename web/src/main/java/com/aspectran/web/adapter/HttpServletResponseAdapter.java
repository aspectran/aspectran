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
import com.aspectran.core.adapter.AbstractResponseAdapter;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpressionParser;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.util.MultiValueMap;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.List;
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
    public String getEncoding() {
        return ((HttpServletResponse)adaptee).getCharacterEncoding();
    }

    @Override
    public void setEncoding(String encoding) throws UnsupportedEncodingException {
        ((HttpServletResponse)adaptee).setCharacterEncoding(encoding);
    }

    @Override
    public String getContentType() {
        return ((HttpServletResponse)adaptee).getContentType();
    }

    @Override
    public void setContentType(String contentType) {
        ((HttpServletResponse)adaptee).setContentType(contentType);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return ((HttpServletResponse)adaptee).getOutputStream();
    }

    @Override
    public Writer getWriter() throws IOException {
        return ((HttpServletResponse)adaptee).getWriter();
    }

    @Override
    public void redirect(String target) throws IOException {
        ((HttpServletResponse)adaptee).sendRedirect(target);
    }

    @Override
    public String redirect(RedirectResponseRule redirectResponseRule) throws IOException {
        String encoding = ((HttpServletResponse)adaptee).getCharacterEncoding();
        String target = redirectResponseRule.getTarget(activity);
        int questionPos = -1;

        StringBuilder sb = new StringBuilder(256);

        if (target != null) {
            sb.append(target);
            questionPos = target.indexOf(QUESTION_CHAR);
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

        target = sb.toString();
        redirect(target);

        return target;
    }

    @Override
    public void flush() {
        MultiValueMap<String, String> headers = getHeaders();
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                List<String> values = entry.getValue();
                if (values.size() > 0) {
                    ((HttpServletResponse)adaptee).setHeader(entry.getKey(), values.get(0));
                    if (values.size() > 1) {
                        for (int i = 1; i < values.size(); i++) {
                            ((HttpServletResponse)adaptee).addHeader(entry.getKey(), values.get(i));
                        }
                    }

                }
            }
        }

        if (getStatus() != 0) {
            ((HttpServletResponse)adaptee).setStatus(getStatus());
        }
    }

}
