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
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.undertow.adapter.TowRequestAdapter;
import com.aspectran.undertow.adapter.TowResponseAdapter;
import com.aspectran.undertow.adapter.TowSessionAdapter;
import com.aspectran.undertow.service.TowService;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.web.activity.request.WebRequestBodyParser;
import com.aspectran.web.adapter.WebRequestAdapter;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.MediaType;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;

import java.io.UnsupportedEncodingException;

import static com.aspectran.web.activity.request.WebRequestBodyParser.MAX_REQUEST_SIZE_SETTING_NAME;

/**
 * <p>Created: 2019-07-27</p>
 */
public class TowActivity extends CoreActivity {

    private final HttpServerExchange exchange;

    private String requestName;

    private MethodType requestMethod;

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
    public Mode getMode() {
        return Mode.WEB;
    }

    public HttpServerExchange getExchange() {
        return exchange;
    }

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public MethodType getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(MethodType requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getFullRequestName() {
        if (requestMethod != null && requestName != null) {
            return requestMethod + " " + requestName;
        } else {
            return (requestName != null ? requestName : StringUtils.EMPTY);
        }
    }

    public void prepare() throws TransletNotFoundException, ActivityPrepareException {
        Assert.state(requestName != null, "requestName is not set");
        Assert.state(requestMethod != null, "requestMethod is not set");
        prepare(requestName, requestMethod);
    }

    @Override
    protected void prepare(String requestName, MethodType requestMethod, TransletRule transletRule)
            throws ActivityPrepareException{
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
                String requestEncoding = getDefinitiveRequestEncoding();
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
                String responseEncoding = getDefinitiveResponseEncoding();
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
    public WebRequestAdapter getRequestAdapter() {
        return (WebRequestAdapter)super.getRequestAdapter();
    }

    @Override
    protected void parseRequest() throws ActivityTerminatedException, RequestParseException {
        if (getParentActivity() == null) {
            getRequestAdapter().preparse();
        } else {
            getRequestAdapter().preparse((WebRequestAdapter)getParentActivity().getRequestAdapter());
        }

        MediaType mediaType = getRequestAdapter().getMediaType();
        if (mediaType != null) {
            if (WebRequestBodyParser.isMultipartForm(getRequestAdapter().getRequestMethod(), mediaType)) {
                WebRequestBodyParser.parseMultipartFormData(this);
            } else if (WebRequestBodyParser.isURLEncodedForm(mediaType)) {
                WebRequestBodyParser.parseURLEncodedFormData(getRequestAdapter());
            }
        }

        super.parseRequest();
    }

}
