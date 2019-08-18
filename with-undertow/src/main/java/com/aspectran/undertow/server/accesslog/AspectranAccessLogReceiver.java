package com.aspectran.undertow.server.accesslog;

import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import io.undertow.server.handlers.accesslog.AccessLogReceiver;

/**
 * Access log receiver that logs messages at INFO level.
 *
 * <p>Created: 2019-08-18</p>
 */
public class AspectranAccessLogReceiver implements AccessLogReceiver {

    private static final String DEFAULT_CATEGORY = "io.undertow.accesslog";

    private final Log log;

    public AspectranAccessLogReceiver() {
        this.log = LogFactory.getLog(DEFAULT_CATEGORY);
    }

    public AspectranAccessLogReceiver(String category) {
        if (StringUtils.hasText(category)) {
            this.log = LogFactory.getLog(category);
        } else {
            this.log = LogFactory.getLog(DEFAULT_CATEGORY);
        }
    }

    @Override
    public void logMessage(String message) {
        log.info(message);
    }

}
