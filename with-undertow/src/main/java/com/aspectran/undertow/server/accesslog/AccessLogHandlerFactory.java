package com.aspectran.undertow.server.accesslog;

import com.aspectran.core.component.bean.aware.ClassLoaderAware;
import com.aspectran.core.util.StringUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.accesslog.AccessLogReceiver;

/**
 * <p>Created: 2019-08-18</p>
 */
public class AccessLogHandlerFactory implements ClassLoaderAware {

    private ClassLoader classLoader;

    private String category;

    private String formatString;

    private HttpHandler handler;

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setFormatString(String formatString) {
        this.formatString = formatString;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setHandler(HttpHandler handler) {
        this.handler = handler;
    }

    public AccessLogHandler createAccessLogHandler() {
        if (handler == null) {
            throw new IllegalStateException("The next handler is not specified");
        }
        AccessLogReceiver accessLogReceiver = new AspectranAccessLogReceiver(category);
        String formatString = (StringUtils.hasText(this.formatString) ? this.formatString : "combined");
        return new AccessLogHandler(handler, accessLogReceiver, formatString, classLoader);
    }

}
