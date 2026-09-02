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
package com.aspectran.netty.activity;

import com.aspectran.core.activity.ActivityPrepareException;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.netty.adapter.NettyRequestAdapter;
import com.aspectran.netty.adapter.NettyResponseAdapter;
import com.aspectran.netty.adapter.NettySessionAdapter;
import com.aspectran.netty.service.NettyService;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.web.activity.request.WebRequestBodyParser;
import com.aspectran.web.adapter.WebRequestAdapter;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.MediaType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.jspecify.annotations.NonNull;

import java.io.UnsupportedEncodingException;

import static com.aspectran.web.activity.request.WebRequestBodyParser.MAX_REQUEST_SIZE_SETTING_NAME;

/**
 * A {@link CoreActivity} implementation for the embedded Netty server.
 * <p>Adapts Aspectran's core execution pipeline to Netty's {@link ChannelHandlerContext}
 * and {@link FullHttpRequest}, enabling full Aspectran request processing without the Servlet API.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public class NettyActivity extends CoreActivity {

    private final NettyService nettyService;

    private final ChannelHandlerContext ctx;

    private final FullHttpRequest request;

    private String requestName;

    private MethodType requestMethod;

    public NettyActivity(@NonNull NettyService nettyService, ChannelHandlerContext ctx, FullHttpRequest request) {
        super(nettyService.getActivityContext(), nettyService.getContextPath());
        this.nettyService = nettyService;
        this.ctx = ctx;
        this.request = request;
    }

    @Override
    public Mode getMode() {
        return Mode.WEB;
    }

    public NettyService getNettyService() {
        return nettyService;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return ctx;
    }

    public FullHttpRequest getRequest() {
        return request;
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
            throws ActivityPrepareException {
        // Check for HTTP POST with X-HTTP-Method-Override header
        if (requestMethod == MethodType.POST) {
            String method = request.headers().get(HttpHeaders.X_METHOD_OVERRIDE);
            if (method != null) {
                MethodType hiddenRequestMethod = MethodType.resolve(method);
                if (hiddenRequestMethod != null) {
                    requestMethod = hiddenRequestMethod;
                }
            }
        }
        super.prepare(requestName, requestMethod, transletRule);
    }

    @Override
    protected void adapt() throws AdapterException {
        try {
            NettyResponseAdapter responseAdapter = new NettyResponseAdapter(ctx, request, this);
            if (getPendingActivity() == null) {
                String responseEncoding = getDefinitiveResponseEncoding();
                if (responseEncoding != null) {
                    responseAdapter.setEncoding(responseEncoding);
                }
            }
            setResponseAdapter(responseAdapter);

            if (nettyService.isSessionAdaptable()) {
                if (getPendingActivity() == null) {
                    NettySessionAdapter sessionAdapter = new NettySessionAdapter(ctx, request, responseAdapter, this);
                    setSessionAdapter(sessionAdapter);
                } else if (getPendingActivity().hasSessionAdapter()) {
                    setSessionAdapter(getPendingActivity().getSessionAdapter());
                }
            }

            NettyRequestAdapter requestAdapter = new NettyRequestAdapter(getTranslet().getRequestMethod(), ctx, request);
            requestAdapter.setContextPath(getContextPath());
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
        } catch (Exception e) {
            throw new AdapterException("Failed to adapt for Netty activity", e);
        }

        setFlashMapManager(nettyService.getFlashMapManager());
        setLocaleResolver(nettyService.getLocaleResolver());

        super.adapt();
    }

    @Override
    public WebRequestAdapter getRequestAdapter() {
        return (WebRequestAdapter) super.getRequestAdapter();
    }

    @Override
    protected void parseRequest() throws ActivityTerminatedException, RequestParseException {
        if (getPendingActivity() == null) {
            getRequestAdapter().preparse();
        } else {
            getRequestAdapter().preparse((WebRequestAdapter) getPendingActivity().getRequestAdapter());
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

    @Override
    protected void saveCurrentActivity() {
        super.saveCurrentActivity();
        if (isOriginalActivity() && hasSessionAdapter() &&
                getSessionAdapter() instanceof NettySessionAdapter sessionAdapter) {
            sessionAdapter.access();
        }
    }

    @Override
    protected void removeCurrentActivity() {
        if (isOriginalActivity() && hasSessionAdapter() &&
                getSessionAdapter() instanceof NettySessionAdapter sessionAdapter) {
            sessionAdapter.complete();
        }
        super.removeCurrentActivity();
    }

}
