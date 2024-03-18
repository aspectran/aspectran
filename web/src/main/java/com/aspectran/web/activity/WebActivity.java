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
package com.aspectran.web.activity;

import com.aspectran.core.activity.ActivityPrepareException;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.support.i18n.locale.LocaleChangeInterceptor;
import com.aspectran.core.support.i18n.locale.LocaleResolver;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.web.activity.request.MultipartFormDataParser;
import com.aspectran.web.activity.request.MultipartRequestParseException;
import com.aspectran.web.activity.request.WebRequestBodyParser;
import com.aspectran.web.adapter.HttpServletRequestAdapter;
import com.aspectran.web.adapter.HttpServletResponseAdapter;
import com.aspectran.web.adapter.HttpSessionAdapter;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.MediaType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;

/**
 * An activity that processes a web request.
 *
 * @since 2008. 4. 28.
 */
public class WebActivity extends CoreActivity {

    private static final String MULTIPART_FORM_DATA_PARSER_SETTING_NAME = "multipartFormDataParser";

    private static final String MAX_REQUEST_SIZE_SETTING_NAME = "maxRequestSize";

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    /**
     * Instantiates a new WebActivity.
     * @param context the current ActivityContext
     * @param request the HTTP request
     * @param response the HTTP response
     */
    public WebActivity(ActivityContext context, String contextPath,
                       HttpServletRequest request, HttpServletResponse response) {
        super(context, contextPath);
        this.request = request;
        this.response = response;
    }

    @Override
    public boolean isRequestWithContextPath() {
        Assert.state(isAdapted(), "Not yet adapted");
        if (getContextPath() != null) {
            String forwardedPath = getRequestAdapter().getHeader(HttpHeaders.X_FORWARDED_PATH);
            return (forwardedPath == null ||
                !(getContextPath().equals(forwardedPath) || forwardedPath.startsWith(getContextPath() + "/")));
        }
        return super.isRequestWithContextPath();
    }

    @Override
    public void prepare(String requestName, MethodType requestMethod, TransletRule transletRule)
            throws ActivityPrepareException {
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

        super.prepare(requestName, requestMethod, transletRule);
    }

    @Override
    protected void adapt() throws AdapterException {
        try {
            SessionAdapter sessionAdapter = new HttpSessionAdapter(request);
            setSessionAdapter(sessionAdapter);

            HttpServletRequestAdapter requestAdapter = new HttpServletRequestAdapter(
                    getTranslet().getRequestMethod(), request);
            if (getParentActivity() == null) {
                String maxRequestSizeSetting = getSetting(MAX_REQUEST_SIZE_SETTING_NAME);
                if (!StringUtils.isEmpty(maxRequestSizeSetting)) {
                    try {
                        long maxRequestSize = Long.parseLong(maxRequestSizeSetting);
                        if (maxRequestSize >= 0L) {
                            requestAdapter.setMaxRequestSize(maxRequestSize);
                        }
                    } catch (NumberFormatException e) {
                        throw new RequestParseException("Illegal value for " + MAX_REQUEST_SIZE_SETTING_NAME +
                                ": " + maxRequestSizeSetting, e);
                    }
                }
                String requestEncoding = getIntendedRequestEncoding();
                if (requestEncoding != null) {
                    try {
                        requestAdapter.setEncoding(requestEncoding);
                    } catch (UnsupportedEncodingException e) {
                        throw new RequestParseException("Unable to set request encoding to " + requestEncoding, e);
                    }
                }
            }
            setRequestAdapter(requestAdapter);

            ResponseAdapter responseAdapter = new HttpServletResponseAdapter(response, this);
            if (getParentActivity() == null) {
                String responseEncoding = getIntendedResponseEncoding();
                if (responseEncoding != null) {
                    responseAdapter.setEncoding(responseEncoding);
                }
            }
            setResponseAdapter(responseAdapter);
        } catch (Exception e) {
            throw new AdapterException("Failed to adapt for the web activity", e);
        }

        super.adapt();
    }

    @Override
    protected void parseRequest() throws RequestParseException, ActivityTerminatedException {
        if (!isRequestParsed()) {
            if (getParentActivity() == null) {
                ((HttpServletRequestAdapter)getRequestAdapter()).preparse();
            } else {
                ((HttpServletRequestAdapter)getRequestAdapter()).preparse(
                        (HttpServletRequestAdapter)getParentActivity().getRequestAdapter());
            }

            MediaType mediaType = ((HttpServletRequestAdapter)getRequestAdapter()).getMediaType();
            if (mediaType != null) {
                if (WebRequestBodyParser.isMultipartForm(getRequestAdapter().getRequestMethod(), mediaType)) {
                    parseMultipartFormData();
                } else if (WebRequestBodyParser.isURLEncodedForm(mediaType)) {
                    parseURLEncodedFormData();
                }
            }
        }

        super.parseRequest();
    }

    /**
     * Parse the multipart form data.
     */
    private void parseMultipartFormData() throws MultipartRequestParseException {
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
     * Parse the URL-encoded Form Data to get the request parameters.
     */
    private void parseURLEncodedFormData() throws RequestParseException {
        WebRequestBodyParser.parseURLEncodedFormData(getRequestAdapter());
    }

    @Override
    protected LocaleResolver resolveLocale() {
        LocaleResolver localeResolver = super.resolveLocale();
        if (localeResolver != null) {
            String localeChangeInterceptorId = getSetting(RequestRule.LOCALE_CHANGE_INTERCEPTOR_SETTING_NAME);
            if (localeChangeInterceptorId != null) {
                LocaleChangeInterceptor localeChangeInterceptor = getBean(LocaleChangeInterceptor.class,
                        localeChangeInterceptorId);
                localeChangeInterceptor.handle(getTranslet(), localeResolver);
            }
        }
        return localeResolver;
    }

}
