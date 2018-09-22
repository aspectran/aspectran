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
package com.aspectran.web.activity;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.request.RequestException;
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.translet.TransletNotFoundException;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.support.i18n.locale.LocaleChangeInterceptor;
import com.aspectran.core.support.i18n.locale.LocaleResolver;
import com.aspectran.core.util.StringUtils;
import com.aspectran.web.activity.request.HttpPutFormContentParser;
import com.aspectran.web.activity.request.MultipartFormDataParser;
import com.aspectran.web.activity.request.MultipartRequestParseException;
import com.aspectran.web.adapter.HttpServletRequestAdapter;
import com.aspectran.web.adapter.HttpServletResponseAdapter;
import com.aspectran.web.adapter.HttpSessionAdapter;
import com.aspectran.web.support.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

/**
 * The Class WebActivity.
 *
 * @since 2008. 4. 28.
 */
public class WebActivity extends CoreActivity {

    private static final String MULTIPART_FORM_DATA_PARSER_SETTING_NAME = "multipartFormDataParser";

    private static final String MULTIPART_FORM_DATA = "multipart/form-data";

    private static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

    private HttpServletRequest request;

    private HttpServletResponse response;

    /**
     * Instantiates a new WebActivity.
     *
     * @param context the current ActivityContext
     * @param request the HTTP request
     * @param response the HTTP response
     */
    public WebActivity(ActivityContext context, HttpServletRequest request, HttpServletResponse response) {
        super(context);

        this.request = request;
        this.response = response;
    }

    @Override
    public void prepare(String transletName, MethodType requestMethod) {
        // Check for HTTP POST with the X-HTTP-Method-Override header
        if (requestMethod == MethodType.POST) {
            String method = request.getHeader(HttpHeaders.X_METHOD_OVERRIDE);
            if (method != null) {
                // Check if the header value is in our methods list
                MethodType hiddenRequestMethod = MethodType.resolve(method);
                if (hiddenRequestMethod != null) {
                    // Change the request method
                    requestMethod = hiddenRequestMethod;
                }
            }
        }

        try {
            super.prepare(transletName, requestMethod);
        } catch (TransletNotFoundException e) {
            if (!StringUtils.endsWith(transletName, ActivityContext.TRANSLET_NAME_SEPARATOR_CHAR)) {
                String transletName2 = transletName + ActivityContext.TRANSLET_NAME_SEPARATOR_CHAR;
                if (getActivityContext().getTransletRuleRegistry().contains(transletName2, requestMethod)) {
                    response.setStatus(301);
                    response.setHeader("Location", transletName2);
                    response.setHeader("Connection", "close");
                    throw new ActivityTerminatedException("Provides for \"trailing slash\" redirects and " +
                            "serving directory index files");
                }
            }
            throw e;
        }
    }

    @Override
    protected void adapt() throws AdapterException {
        try {
            SessionAdapter sessionAdapter = new HttpSessionAdapter(request, getActivityContext());
            setSessionAdapter(sessionAdapter);

            RequestAdapter requestAdapter = new HttpServletRequestAdapter(request);
            setRequestAdapter(requestAdapter);

            ResponseAdapter responseAdapter = new HttpServletResponseAdapter(response, this);
            setResponseAdapter(responseAdapter);

            super.adapt();
        } catch (Exception e) {
            throw new AdapterException("Failed to specify adapters required for web service activity", e);
        }
    }

    @Override
    protected void parseRequest() {
        String encoding = resolveRequestEncoding();
        if (encoding != null) {
            try {
                getRequestAdapter().setEncoding(encoding);
            } catch (UnsupportedEncodingException e) {
                throw new RequestException("Unable to set request encoding to " + encoding, e);
            }
        }

        MethodType requestMethod = getRequestAdapter().getRequestMethod();
        MethodType allowedMethod = getRequestRule().getAllowedMethod();
        if (allowedMethod != null && !allowedMethod.equals(requestMethod)) {
            throw new RequestMethodNotAllowedException(allowedMethod);
        }

        String contentType = request.getContentType();
        if (contentType != null) {
            if (MethodType.POST.equals(requestMethod) && contentType.startsWith(MULTIPART_FORM_DATA)) {
                parseMultipartFormData();
            } else if ((MethodType.PUT.equals(requestMethod) ||
                    MethodType.PATCH.equals(requestMethod) ||
                    MethodType.DELETE.equals(requestMethod)) &&
                    contentType.startsWith(APPLICATION_FORM_URLENCODED)) {
                parseHttpPutFormContent();
            }
        }

        super.parseRequest();
    }

    /**
     * Parse the multipart form data.
     */
    private void parseMultipartFormData() {
        String multipartFormDataParser = getSetting(MULTIPART_FORM_DATA_PARSER_SETTING_NAME);
        if (multipartFormDataParser == null) {
            throw new MultipartRequestParseException("The setting name 'multipartFormDataParser' for multipart " +
                    "form data parsing is not specified. Please specify 'multipartFormDataParser' via Aspect so " +
                    "that Translet can parse multipart form data.");
        }

        MultipartFormDataParser parser = getBean(multipartFormDataParser);
        if (parser == null) {
            throw new MultipartRequestParseException("No bean named '" + multipartFormDataParser + "' is defined");
        }
        parser.parse(getRequestAdapter());
    }

    /**
     * Parse the HTTP PUT requests.
     */
    private void parseHttpPutFormContent() {
        HttpPutFormContentParser.parse(getRequestAdapter());
    }

    @Override
    protected LocaleResolver resolveLocale() {
        LocaleResolver localeResolver = super.resolveLocale();
        if (localeResolver != null) {
            String localeChangeInterceptorId = getSetting(RequestRule.LOCALE_CHANGE_INTERCEPTOR_SETTING_NAME);
            if (localeChangeInterceptorId != null) {
                LocaleChangeInterceptor localeChangeInterceptor = getBean(localeChangeInterceptorId,
                        LocaleChangeInterceptor.class);
                localeChangeInterceptor.handle(getTranslet(), localeResolver);
            }
        }
        return localeResolver;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Activity> T newActivity() {
        WebActivity activity = new WebActivity(getActivityContext(), request, getResponseAdapter().getAdaptee());
        activity.setIncluded(true);
        return (T)activity;
    }

}
