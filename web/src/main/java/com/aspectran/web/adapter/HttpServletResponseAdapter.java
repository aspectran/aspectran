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
package com.aspectran.web.adapter;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.response.RedirectTarget;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.activity.response.transform.TransformResponse;
import com.aspectran.core.adapter.AbstractResponseAdapter;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.type.FormatType;
import com.aspectran.web.support.util.SendRedirectBasedOnXForwardedProtocol;
import com.aspectran.web.support.util.WebUtils;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;

/**
 * An adapter that wraps a {@link jakarta.servlet.http.HttpServletResponse},
 * exposing it as a {@link com.aspectran.core.adapter.ResponseAdapter} for the Aspectran framework.
 * <p>This class acts as a bridge between the Jakarta Servlet API and the Aspectran core,
 * allowing the framework to write to the servlet response in a consistent, abstracted manner.</p>
 *
 * @since 2011. 3. 13.
 */
public class HttpServletResponseAdapter extends AbstractResponseAdapter {

    /**
     * The setting name for determining whether to rewrite the protocol of a redirect URL to HTTPS.
     * This is relevant when a web application server uses HTTP, but a front-end proxy server (like Nginx)
     * receives HTTPS requests. If set to {@code true}, the redirect URL's protocol will be changed to HTTPS.
     * This setting is typically injected into the current {@code Activity} via an Aspect configuration,
     * for example:
     * <pre>
     *     &lt;aspect id="webTransletSettings"&gt;
     *         &lt;joinpoint&gt;
     *             pointcut: {
     *                 +: /**
     *             }
     *         &lt;/joinpoint&gt;
     *         &lt;settings&gt;
     *             &lt;setting name="characterEncoding" value="utf-8"/&gt;
     *             &lt;setting name="viewDispatcher" value="jspViewDispatcher"/&gt;
     *             &lt;setting name="proxyProtocolAware" value="true"/&gt;
     *         &lt;/settings&gt;
     *     &lt;/aspect&gt;
     * </pre>
     */
    public static final String PROXY_PROTOCOL_AWARE_SETTING_NAME = "proxyProtocolAware";

    private final Activity activity;

    private boolean precommitDone;

    private String reservedRedirectLocation;

    /**
     * Creates a new {@code HttpServletResponseAdapter}.
     * @param response the native {@link HttpServletResponse} to wrap
     * @param activity the current activity
     */
    public HttpServletResponseAdapter(HttpServletResponse response, Activity activity) {
        super(response);
        this.activity = activity;
    }

    @Override
    public String getHeader(String name) {
        return getHttpServletResponse().getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return getHttpServletResponse().getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return getHttpServletResponse().getHeaderNames();
    }

    @Override
    public boolean containsHeader(String name) {
        return getHttpServletResponse().containsHeader(name);
    }

    @Override
    public void setHeader(String name, String value) {
        getHttpServletResponse().setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        getHttpServletResponse().addHeader(name, value);
    }

    @Override
    public String getEncoding() {
        return getHttpServletResponse().getCharacterEncoding();
    }

    @Override
    public void setEncoding(String encoding) {
        getHttpServletResponse().setCharacterEncoding(encoding);
    }

    @Override
    public String getContentType() {
        return getHttpServletResponse().getContentType();
    }

    @Override
    public void setContentType(String contentType) {
        getHttpServletResponse().setContentType(contentType);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation performs a pre-commit check before returning the stream.</p>
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        precommit();
        return getHttpServletResponse().getOutputStream();
    }

    /**
     * {@inheritDoc}
     * <p>This implementation performs a pre-commit check before returning the writer.</p>
     */
    @Override
    public Writer getWriter() throws IOException {
        precommit();
        return getHttpServletResponse().getWriter();
    }

    /**
     * {@inheritDoc}
     * <p>If a redirect location has been reserved, this method sends the redirect.</p>
     */
    @Override
    public void commit() throws IOException {
        if (reservedRedirectLocation != null) {
            getHttpServletResponse().sendRedirect(reservedRedirectLocation);
            reservedRedirectLocation = null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link HttpServletResponse#reset()}.</p>
     */
    @Override
    public void reset() {
        getHttpServletResponse().reset();
        reservedRedirectLocation = null;
    }

    /**
     * {@inheritDoc}
     * <p>This implementation reserves the redirect location. The actual redirect is
     * sent when {@link #commit()} is called.</p>
     */
    @Override
    public void redirect(String location) throws IOException {
        boolean proxyProtocolAware = Boolean.parseBoolean(activity.getSetting(PROXY_PROTOCOL_AWARE_SETTING_NAME));
        if (proxyProtocolAware) {
            location = SendRedirectBasedOnXForwardedProtocol.getLocation(activity.getTranslet(), location);
        }
        reservedRedirectLocation = location;
    }

    /**
     * {@inheritDoc}
     * <p>This implementation builds a redirect URL and reserves it.</p>
     */
    @Override
    public RedirectTarget redirect(RedirectRule redirectRule) throws IOException {
        RedirectTarget redirectTarget = WebUtils.getRedirectTarget(redirectRule, activity);
        String path = redirectTarget.getLocation();
        String url = getHttpServletResponse().encodeRedirectURL(path);
        redirect(url);
        return redirectTarget;
    }

    @Override
    public int getStatus() {
        return getHttpServletResponse().getStatus();
    }

    @Override
    public void setStatus(int status) {
        getHttpServletResponse().setStatus(status);
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link HttpServletResponse#encodeURL(String)}.</p>
     */
    @Override
    public String transformPath(String path) {
        return getHttpServletResponse().encodeURL(path);
    }

    /**
     * Returns the underlying {@link HttpServletResponse}.
     * @return the native servlet response
     */
    private HttpServletResponse getHttpServletResponse() {
        return getAdaptee();
    }

    /**
     * Performs a pre-commit action, which may involve executing a transform response
     * before the main response is written.
     * @throws IOException if an error occurs during the response
     */
    private void precommit() throws IOException {
        if (!precommitDone) {
            precommitDone = true;
            Response response = activity.getDeclaredResponse();
            if (response instanceof TransformResponse transformResponse) {
                FormatType formatType = transformResponse.getFormatType();
                if (formatType == null) {
                    try {
                        response.respond(activity);
                    } catch (ResponseException e) {
                        throw new IOException("Error during pre-commit", e);
                    }
                }
            }
        }
    }

}
