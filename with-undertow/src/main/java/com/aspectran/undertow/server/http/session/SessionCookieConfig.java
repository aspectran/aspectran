package com.aspectran.undertow.server.http.session;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;

import java.util.Map;

/**
 * Encapsulation of session cookie configuration.
 * This removes the need for the session manager to
 * know about cookie configuration.
 */
public class SessionCookieConfig {

    private static final Log log = LogFactory.getLog(SessionCookieConfig.class);

    public static final String DEFAULT_SESSION_ID = "JSESSIONID";

    private String cookieName = DEFAULT_SESSION_ID;
    
    private String path = "/";
    
    private String domain;
    
    private boolean discard;
    
    private boolean secure;
    
    private boolean httpOnly;
    
    private int maxAge = -1;
    
    private String comment;

    public void setSessionId(HttpServerExchange exchange, String sessionId) {
        Cookie cookie = new CookieImpl(cookieName, sessionId)
                .setPath(path)
                .setDomain(domain)
                .setDiscard(discard)
                .setSecure(secure)
                .setHttpOnly(httpOnly)
                .setComment(comment);
        if (maxAge > 0) {
            cookie.setMaxAge(maxAge);
        }
        exchange.setResponseCookie(cookie);
        if (log.isTraceEnabled()) {
            log.trace("Setting session cookie session id " + sessionId + " on " + exchange);
        }
    }

    public void clearSession(HttpServerExchange exchange, String sessionId) {
        Cookie cookie = new CookieImpl(cookieName, sessionId)
                .setPath(path)
                .setDomain(domain)
                .setDiscard(discard)
                .setSecure(secure)
                .setHttpOnly(httpOnly)
                .setMaxAge(0);
        exchange.setResponseCookie(cookie);
        if (log.isTraceEnabled()) {
            log.trace("Clearing session cookie session id " + sessionId + " on " + exchange);
        }
    }

    public String findSessionId(HttpServerExchange exchange) {
        Map<String, Cookie> cookies = exchange.getRequestCookies();
        if (cookies != null) {
            Cookie sessionId = cookies.get(cookieName);
            if (sessionId != null) {
                if (log.isTraceEnabled()) {
                    log.trace("Found session cookie session id " + sessionId + " on " + exchange);
                }
                return sessionId.getValue();
            }
        }
        return null;
    }

    public String getCookieName() {
        return cookieName;
    }

    public SessionCookieConfig setCookieName(String cookieName) {
        this.cookieName = cookieName;
        return this;
    }

    public String getPath() {
        return path;
    }

    public SessionCookieConfig setPath(String path) {
        this.path = path;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public SessionCookieConfig setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public boolean isDiscard() {
        return discard;
    }

    public SessionCookieConfig setDiscard(boolean discard) {
        this.discard = discard;
        return this;
    }

    public boolean isSecure() {
        return secure;
    }

    public SessionCookieConfig setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public SessionCookieConfig setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public SessionCookieConfig setMaxAge(int maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public SessionCookieConfig setComment(String comment) {
        this.comment = comment;
        return this;
    }
    
}
