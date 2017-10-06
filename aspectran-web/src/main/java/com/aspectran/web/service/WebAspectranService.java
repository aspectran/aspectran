/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.web.service;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.component.translet.TransletNotFoundException;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.config.AspectranConfig;
import com.aspectran.core.context.builder.config.ContextConfig;
import com.aspectran.core.context.builder.config.WebConfig;
import com.aspectran.core.service.AspectranService;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.BasicAspectranService;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.web.activity.WebActivity;
import com.aspectran.web.adapter.WebApplicationAdapter;
import com.aspectran.web.startup.servlet.WebActivityServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * The Class WebAspectranService.
 */
public class WebAspectranService extends BasicAspectranService {

    private static final Log log = LogFactory.getLog(WebAspectranService.class);

    /**
     * ServletContext attribute name used to obtain the root WebAspectranService object.
     */
    public static final String ROOT_WEB_ASPECTRAN_SERVICE_ATTRIBUTE = WebAspectranService.class.getName() + ".ROOT";

    /**
     * The prefix of the ServletContext property name used to get the standalone WebAspectranService object.
     */
    public static final String STANDALONE_WEB_ASPECTRAN_SERVICE_ATTRIBUTE_PREFIX = WebAspectranService.class.getName() + ".STANDALONE:";

    private static final String ASPECTRAN_CONFIG_PARAM = "aspectran:config";

    private static final String DEFAULT_ROOT_CONTEXT = "/WEB-INF/aspectran/config/root-config.xml";

    private String uriDecoding;

    private DefaultServletHttpRequestHandler defaultServletHttpRequestHandler;

    private long pauseTimeout = -1L;

    private WebAspectranService(ServletContext servletContext) {
        super(new WebApplicationAdapter(servletContext));
    }

    private WebAspectranService(AspectranService rootAspectranService) {
        super(rootAspectranService);
    }

    protected void setUriDecoding(String uriDecoding) {
        this.uriDecoding = uriDecoding;
    }

    /**
     * Processes the actual dispatching to the activity.
     *
     * @param request current HTTP servlet request
     * @param response current HTTP servlet response
     * @throws IOException if an input or output error occurs while the activity is handling the HTTP request
     */
    public void serve(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestUri = request.getRequestURI();

        if (uriDecoding != null) {
            requestUri = URLDecoder.decode(requestUri, uriDecoding);
        }

        if (!isExposable(requestUri)) {
            if (log.isDebugEnabled()) {
                log.debug("Unexposable translet [" + requestUri + "] in WebAspectranService");
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Request URI: " + requestUri);
        }

        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (log.isDebugEnabled()) {
                    log.debug("AspectranService has been paused, so did not respond to the request URI \"" + requestUri + "\"");
                }
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            } else {
                pauseTimeout = 0L;
            }
        }

        Activity activity = null;

        try {
            activity = new WebActivity(getActivityContext(), request, response);
            activity.prepare(requestUri, request.getMethod());
            activity.perform();
        } catch (TransletNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("Unregistered translet [" + requestUri + "] in WebAspectranService");
            }
            try {
                if (!defaultServletHttpRequestHandler.handle(request, response)) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Exception e2) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                log.error(e.getMessage(), e2);
            }
        } catch (RequestMethodNotAllowedException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage());
            }
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        } catch (ActivityTerminatedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Translet did not complete and terminated: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("An error occurred while processing a Web Activity", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (activity != null) {
                activity.finish();
            }
        }
    }

    /**
     * Sets the default servlet http request handler.
     *
     * @param servletContext the servlet context
     * @param defaultServletName the default servlet name
     */
    private void setDefaultServletHttpRequestHandler(ServletContext servletContext, String defaultServletName) {
        defaultServletHttpRequestHandler = new DefaultServletHttpRequestHandler(servletContext);
        if (defaultServletName != null) {
            defaultServletHttpRequestHandler.setDefaultServletName(defaultServletName);
        }
    }

    /**
     * Returns a new instance of WebAspectranService.
     *
     * @param servletContext the servlet context
     * @return the web aspectran service
     * @throws AspectranServiceException the aspectran service exception
     */
    public static WebAspectranService create(ServletContext servletContext) throws AspectranServiceException {
        String aspectranConfigParam = servletContext.getInitParameter(ASPECTRAN_CONFIG_PARAM);
        if (aspectranConfigParam == null) {
            log.warn("No specified servlet context initialization parameter for instantiating WebAspectranService");
        }

        WebAspectranService aspectranService = create(servletContext, aspectranConfigParam);

        servletContext.setAttribute(ROOT_WEB_ASPECTRAN_SERVICE_ATTRIBUTE, aspectranService);

        if (log.isDebugEnabled()) {
            log.debug("The WebAspectranService attribute in ServletContext has been created; " +
                    ROOT_WEB_ASPECTRAN_SERVICE_ATTRIBUTE + ": " + aspectranService);
        }

        return aspectranService;
    }

    /**
     * Returns a new instance of WebAspectranService.
     *
     * @param servletContext the servlet context
     * @param rootAspectranService the root aspectran service
     * @return the web aspectran service
     * @throws AspectranServiceException the aspectran service exception
     */
    public static WebAspectranService create(ServletContext servletContext, AspectranService rootAspectranService)
            throws AspectranServiceException {
        WebAspectranService webAspectranService = new WebAspectranService(rootAspectranService);
        AspectranConfig aspectranConfig = rootAspectranService.getAspectranConfig();
        if (aspectranConfig != null) {
            WebConfig webConfig = aspectranConfig.getWebConfig();
            String defaultServletName = null;
            if (webConfig != null) {
                webAspectranService.setUriDecoding(webConfig.getString(WebConfig.uriDecoding));
                defaultServletName = webConfig.getString(WebConfig.defaultServletName);
                webAspectranService.setExposals(webConfig.getStringArray(WebConfig.exposals));
            }
            webAspectranService.setDefaultServletHttpRequestHandler(servletContext, defaultServletName);
        }
        setServiceStateListener(webAspectranService);
        return webAspectranService;
    }

    /**
     * Returns a new instance of WebAspectranService.
     *
     * @param servlet the web activity servlet
     * @return the web aspectran service
     * @throws AspectranServiceException the aspectran service exception
     */
    public static WebAspectranService create(WebActivityServlet servlet) throws AspectranServiceException {
        ServletContext servletContext = servlet.getServletContext();
        ServletConfig servletConfig = servlet.getServletConfig();

        String aspectranConfigParam = servletConfig.getInitParameter(ASPECTRAN_CONFIG_PARAM);
        if (aspectranConfigParam == null) {
            log.warn("No specified servlet initialization parameter for instantiating WebAspectranService");
        }

        WebAspectranService aspectranService = create(servletContext, aspectranConfigParam);

        String attrName = STANDALONE_WEB_ASPECTRAN_SERVICE_ATTRIBUTE_PREFIX + servlet.getServletName();
        servletContext.setAttribute(attrName, aspectranService);

        if (log.isDebugEnabled()) {
            log.debug("The WebAspectranService attribute in ServletContext has been created; " +
                    attrName + ": " + aspectranService);
        }

        return aspectranService;
    }

    /**
     * Returns a new instance of WebAspectranService.
     *
     * @param servlet the servlet
     * @param rootWebAspectranService the root aspectran service
     * @return the web aspectran service
     * @throws AspectranServiceException the aspectran service exception
     */
    public static WebAspectranService create(WebActivityServlet servlet, WebAspectranService rootWebAspectranService)
            throws AspectranServiceException {
        ServletContext servletContext = servlet.getServletContext();
        ServletConfig servletConfig = servlet.getServletConfig();
        String aspectranConfigParam = servletConfig.getInitParameter(ASPECTRAN_CONFIG_PARAM);
        if (aspectranConfigParam != null) {
            WebAspectranService webAspectranService = create(servletContext, aspectranConfigParam);
            servletContext.setAttribute(STANDALONE_WEB_ASPECTRAN_SERVICE_ATTRIBUTE_PREFIX + servlet.getServletName(), webAspectranService);
            return webAspectranService;
        } else {
            return rootWebAspectranService;
        }
    }

    /**
     * Returns a new instance of WebAspectranService.
     *
     * @param servletContext the servlet context
     * @param aspectranConfigParam the parameter for aspectran configuration
     * @return the web aspectran service
     * @throws AspectranServiceException the aspectran service exception
     */
    private static WebAspectranService create(ServletContext servletContext, String aspectranConfigParam)
            throws AspectranServiceException {
        AspectranConfig aspectranConfig;
        if (aspectranConfigParam != null) {
            aspectranConfig = new AspectranConfig(aspectranConfigParam);
        } else {
            aspectranConfig = new AspectranConfig();
        }

        ContextConfig contextConfig = aspectranConfig.touchContextConfig();
        String rootConfigLocation = contextConfig.getString(ContextConfig.root);
        if (rootConfigLocation == null || rootConfigLocation.isEmpty()) {
            contextConfig.putValue(ContextConfig.root, DEFAULT_ROOT_CONTEXT);
        }

        WebAspectranService webAspectranService = new WebAspectranService(servletContext);
        webAspectranService.prepare(aspectranConfig);

        WebConfig webConfig = aspectranConfig.getWebConfig();
        String defaultServletName = null;
        if (webConfig != null) {
            webAspectranService.setUriDecoding(webConfig.getString(WebConfig.uriDecoding));
            defaultServletName = webConfig.getString(WebConfig.defaultServletName);
            webAspectranService.setExposals(webConfig.getStringArray(WebConfig.exposals));
        }

        webAspectranService.setDefaultServletHttpRequestHandler(servletContext, defaultServletName);

        setServiceStateListener(webAspectranService);

        return webAspectranService;
    }

    private static void setServiceStateListener(final WebAspectranService webAspectranService) {
        webAspectranService.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                webAspectranService.pauseTimeout = 0L;
            }

            @Override
            public void restarted() {
                started();
            }

            @Override
            public void paused(long millis) {
                if (millis < 0L) {
                    throw new IllegalArgumentException("Pause timeout in milliseconds needs to be set to a value of greater than 0");
                }
                webAspectranService.pauseTimeout = System.currentTimeMillis() + millis;
            }

            @Override
            public void paused() {
                webAspectranService.pauseTimeout = -1L;
            }

            @Override
            public void resumed() {
                started();
            }

            @Override
            public void stopped() {
                paused();
            }
        });
    }

    /**
     * Find the root ActivityContext for this web aspectran service.
     *
     * @param servletContext ServletContext to find the web aspectran service for
     * @return the ActivityContext for this web aspectran service
     */
    public static ActivityContext getActivityContext(ServletContext servletContext) {
        ActivityContext activityContext = getActivityContext(servletContext, ROOT_WEB_ASPECTRAN_SERVICE_ATTRIBUTE);
        if (activityContext == null) {
            throw new IllegalStateException("No Root WebAspectranService found: No AspectranServiceListener registered?");
        }
        return activityContext;
    }

    /**
     * Find the standalone ActivityContext for this web aspectran service.
     *
     * @param servlet the servlet
     * @return the ActivityContext for this web aspectran service
     */
    public static ActivityContext getActivityContext(HttpServlet servlet) {
        ServletContext servletContext = servlet.getServletContext();
        String attrName = STANDALONE_WEB_ASPECTRAN_SERVICE_ATTRIBUTE_PREFIX + servlet.getServletName();
        ActivityContext activityContext = getActivityContext(servletContext, attrName);
        if (activityContext == null) {
            return getActivityContext(servletContext);
        }
        return activityContext;
    }

    /**
     * Find the ActivityContext for this web aspectran service.
     *
     * @param servletContext ServletContext to find the web aspectran service for
     * @param attrName the name of the ServletContext attribute to look for
     * @return the ActivityContext for this web aspectran service
     */
    private static ActivityContext getActivityContext(ServletContext servletContext, String attrName) {
        Object attr = servletContext.getAttribute(attrName);
        if (attr == null) {
            return null;
        }
        if (!(attr instanceof WebAspectranService)) {
            throw new IllegalStateException("Context attribute [" + attr + "] is not of type [" + WebAspectranService.class.getName() + "]");
        }
        return ((AspectranService)attr).getActivityContext();
    }

}
