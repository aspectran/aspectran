/*
 * Copyright (c) 2008-present The Aspectran Project
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
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.web.activity.request.WebRequestBodyParser;
import com.aspectran.web.adapter.HttpServletRequestAdapter;
import com.aspectran.web.adapter.HttpServletResponseAdapter;
import com.aspectran.web.adapter.HttpSessionAdapter;
import com.aspectran.web.adapter.WebRequestAdapter;
import com.aspectran.web.service.WebService;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.MediaType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;

import java.io.UnsupportedEncodingException;

import static com.aspectran.web.activity.request.WebRequestBodyParser.MAX_REQUEST_SIZE_SETTING_NAME;

/**
 * Activity implementation used by the web service to process HTTP requests.
 * <p>This class extends {@link CoreActivity} and adapts the core Aspectran processing
 * pipeline to a Servlet-based web environment. It wraps the native {@link HttpServletRequest}
 * and {@link HttpServletResponse} objects, providing web-specific request and response
 * adapters, and handling concerns like URI decoding, static resource handling, and
 * asynchronous request processing.</p>
 *
 * @since 2008-04-28
 */
public class WebActivity extends CoreActivity {

    private final WebService webService;

    private final String reverseContextPath;

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private String requestName;

    private MethodType requestMethod;

    private boolean async;

    private Long timeout;

    /**
     * Instantiates a new WebActivity.
     * @param webService the {@link WebService} instance
     * @param contextPath the context path of the web application
     * @param reverseContextPath the reverse context path for URL generation
     * @param request the HTTP request
     * @param response the HTTP response
     */
    public WebActivity(@NonNull WebService webService, String contextPath, String reverseContextPath,
                       HttpServletRequest request, HttpServletResponse response) {
        super(webService.getActivityContext(), contextPath);
        this.webService = webService;
        this.reverseContextPath = reverseContextPath;
        this.request = request;
        this.response = response;
    }

    @Override
    public Mode getMode() {
        return Mode.WEB;
    }

    @Override
    public String getReverseContextPath() {
        return reverseContextPath;
    }

    /**
     * Returns the underlying {@link HttpServletRequest} object.
     * @return the HTTP servlet request
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Returns the underlying {@link HttpServletResponse} object.
     * @return the HTTP servlet response
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     * Returns the name of the request being processed by this activity.
     * @return the request name
     */
    public String getRequestName() {
        return requestName;
    }

    /**
     * Sets the name of the request to be processed by this activity.
     * @param requestName the request name
     */
    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    /**
     * Returns the method type of the request being processed by this activity.
     * @return the request method type
     */
    public MethodType getRequestMethod() {
        return requestMethod;
    }

    /**
     * Sets the method type of the request to be processed by this activity.
     * @param requestMethod the request method type
     */
    public void setRequestMethod(MethodType requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * Returns a human-readable representation of the request, combining
     * method, reverse context path, and name (e.g., "GET /context/path/to/translet").
     * @return the combined request information
     */
    public String getFullRequestName() {
        StringBuilder sb = new StringBuilder();
        if (requestMethod != null) {
            sb.append(requestMethod).append(" ");
        }
        if (StringUtils.hasLength(reverseContextPath)) {
            sb.append(reverseContextPath);
        }
        if (requestName != null) {
            sb.append(requestName);
        }
        return sb.toString();
    }

    /**
     * Returns whether this activity is configured for asynchronous execution.
     * @return {@code true} if asynchronous execution is enabled, {@code false} otherwise
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * Returns the timeout for asynchronous execution in milliseconds.
     * @return the timeout in milliseconds, or {@code null} if no timeout is set
     */
    public Long getTimeout() {
        return timeout;
    }

    /**
     * Prepares this activity for execution by validating and applying the
     * configured request name and method.
     * @throws TransletNotFoundException if the target request cannot be found
     * @throws ActivityPrepareException if preparation fails for any reason
     */
    public void prepare() throws TransletNotFoundException, ActivityPrepareException {
        Assert.state(requestName != null, "requestName is not set");
        Assert.state(requestMethod != null, "requestMethod is not set");
        prepare(requestName, requestMethod);
    }

    @Override
    protected void prepare(String requestName, MethodType requestMethod, @NonNull TransletRule transletRule)
            throws ActivityPrepareException {
        this.async = transletRule.isAsync();
        this.timeout = transletRule.getTimeout();

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
            if (webService.isSessionAdaptable()) {
                SessionAdapter sessionAdapter = new HttpSessionAdapter(request);
                setSessionAdapter(sessionAdapter);
            }

            HttpServletRequestAdapter requestAdapter = new HttpServletRequestAdapter(
                    getTranslet().getRequestMethod(), request);
            if (getPendingActivity() == null) {
                String maxRequestSizeSetting = getSetting(MAX_REQUEST_SIZE_SETTING_NAME);
                if (StringUtils.hasLength(maxRequestSizeSetting)) {
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

            ResponseAdapter responseAdapter = new HttpServletResponseAdapter(response, this);
            if (getPendingActivity() == null) {
                String responseEncoding = getDefinitiveResponseEncoding();
                if (responseEncoding != null) {
                    responseAdapter.setEncoding(responseEncoding);
                }
            }
            setResponseAdapter(responseAdapter);
        } catch (Exception e) {
            throw new AdapterException("Failed to adapt for the web activity", e);
        }

        setFlashMapManager(webService.getFlashMapManager());
        setLocaleResolver(webService.getLocaleResolver());

        super.adapt();
    }

    @Override
    public WebRequestAdapter getRequestAdapter() {
        return (WebRequestAdapter)super.getRequestAdapter();
    }

    @Override
    protected void parseRequest() throws RequestParseException, ActivityTerminatedException {
        if (getPendingActivity() == null) {
            getRequestAdapter().preparse();
        } else {
            getRequestAdapter().preparse((WebRequestAdapter)getPendingActivity().getRequestAdapter());
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
