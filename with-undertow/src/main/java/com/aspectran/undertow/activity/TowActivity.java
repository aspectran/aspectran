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
package com.aspectran.undertow.activity;

import com.aspectran.core.activity.ActivityPrepareException;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.support.i18n.locale.LocaleChangeInterceptor;
import com.aspectran.core.support.i18n.locale.LocaleResolver;
import com.aspectran.undertow.adapter.TowRequestAdapter;
import com.aspectran.undertow.adapter.TowResponseAdapter;
import com.aspectran.undertow.adapter.TowSessionAdapter;
import com.aspectran.undertow.service.TowService;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.web.activity.request.MultipartFormDataParser;
import com.aspectran.web.activity.request.MultipartRequestParseException;
import com.aspectran.web.activity.request.WebRequestBodyParser;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.MediaType;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;

import java.io.UnsupportedEncodingException;

import static com.aspectran.core.context.rule.RequestRule.LOCALE_CHANGE_INTERCEPTOR_SETTING_NAME;

/**
 * <p>Created: 2019-07-27</p>
 */
public class TowActivity extends CoreActivity {

    private static final String MULTIPART_FORM_DATA_PARSER_SETTING_NAME = "multipartFormDataParser";

    private static final String MAX_REQUEST_SIZE_SETTING_NAME = "maxRequestSize";

    private final HttpServerExchange exchange;

    /**
     * Instantiates a new tow service
     * @param service the tow service
     * @param exchange the adaptee object
     */
    public TowActivity(@NonNull TowService service, HttpServerExchange exchange) {
        super(service.getActivityContext());
        this.exchange = exchange;
    }

    @Override
    public void prepare(String requestName, MethodType requestMethod, TransletRule transletRule)
            throws RequestMethodNotAllowedException, ActivityPrepareException{
        // Check for HTTP POST with the X-HTTP-Method-Override header
        if (requestMethod == MethodType.POST) {
            String method = exchange.getRequestHeaders().getFirst(HttpHeaders.X_METHOD_OVERRIDE);
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
            if (getParentActivity() == null) {
                SessionManager sessionManager = exchange.getAttachment(SessionManager.ATTACHMENT_KEY);
                SessionConfig sessionConfig = exchange.getAttachment(SessionConfig.ATTACHMENT_KEY);
                if (sessionManager != null && sessionConfig != null) {
                    setSessionAdapter(new TowSessionAdapter(exchange));
                }
            } else {
                setSessionAdapter(getParentActivity().getSessionAdapter());
            }

            TowRequestAdapter requestAdapter = new TowRequestAdapter(getTranslet().getRequestMethod(), exchange);
            if (getParentActivity() == null) {
                String maxRequestSizeSetting = getSetting(MAX_REQUEST_SIZE_SETTING_NAME);
                if (StringUtils.hasLength(maxRequestSizeSetting)) {
                    try {
                        long maxRequestSize = Long.parseLong(maxRequestSizeSetting);
                        if (maxRequestSize >= 0L) {
                            requestAdapter.setMaxRequestSize(maxRequestSize);
                            exchange.setMaxEntitySize(maxRequestSize);
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

            ResponseAdapter responseAdapter = new TowResponseAdapter(exchange, this);
            if (getParentActivity() == null) {
                String responseEncoding = getIntendedResponseEncoding();
                if (responseEncoding != null) {
                    responseAdapter.setEncoding(responseEncoding);
                }
            }
            setResponseAdapter(responseAdapter);
        } catch (Exception e) {
            throw new AdapterException("Failed to adapt for the tow activity", e);
        }

        super.adapt();
    }

    @Override
    protected void parseRequest() throws ActivityTerminatedException, RequestParseException {
        if (!isRequestParsed()) {
            if (getParentActivity() == null) {
                ((TowRequestAdapter)getRequestAdapter()).preparse();
            } else {
                ((TowRequestAdapter)getRequestAdapter()).preparse(
                        (TowRequestAdapter)getParentActivity().getRequestAdapter());
            }

            MediaType mediaType = ((TowRequestAdapter)getRequestAdapter()).getMediaType();
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
            String localeChangeInterceptorId = getSetting(LOCALE_CHANGE_INTERCEPTOR_SETTING_NAME);
            if (localeChangeInterceptorId != null) {
                LocaleChangeInterceptor localeChangeInterceptor = getBean(LocaleChangeInterceptor.class,
                        localeChangeInterceptorId);
                localeChangeInterceptor.handle(getTranslet(), localeResolver);
            }
        }
        return localeResolver;
    }

}
