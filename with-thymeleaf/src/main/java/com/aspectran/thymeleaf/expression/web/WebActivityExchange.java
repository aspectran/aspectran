package com.aspectran.thymeleaf.expression.web;

import com.aspectran.core.activity.Activity;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.web.activity.WebActivity;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.IWebRequest;
import org.thymeleaf.web.IWebSession;

import java.security.Principal;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class WebActivityExchange implements IWebExchange {

    private final Activity activity;

    private final WebActivityRequest request;

    private final WebActivitySession session;

    private final WebActivityApplication application;

    public WebActivityExchange(Activity activity,
                               WebActivityRequest request,
                               WebActivitySession session,
                               WebActivityApplication application) {
        this.activity = activity;
        this.request = request;
        this.session = session;
        this.application = application;
    }

    @Override
    public IWebRequest getRequest() {
        return request;
    }

    @Override
    public IWebSession getSession() {
        return session;
    }

    @Override
    public IWebApplication getApplication() {
        return application;
    }

    @Override
    public Principal getPrincipal() {
        return activity.getRequestAdapter().getPrincipal();
    }

    @Override
    public Locale getLocale() {
        return activity.getRequestAdapter().getLocale();
    }

    @Override
    public String getContentType() {
        return activity.getResponseAdapter().getContentType();
    }

    @Override
    public String getCharacterEncoding() {
        return activity.getResponseAdapter().getEncoding();
    }

    @Override
    public boolean containsAttribute(String name) {
        return activity.getRequestAdapter().hasAttribute(name);
    }

    @Override
    public int getAttributeCount() {
        return activity.getRequestAdapter().getAttributeMap().size();
    }

    @Override
    public Set<String> getAllAttributeNames() {
        return Collections.unmodifiableSet(activity.getRequestAdapter().getAttributeNames());
    }

    @Override
    public Map<String, Object> getAttributeMap() {
        return Collections.unmodifiableMap(activity.getRequestAdapter().getAttributeMap());
    }

    @Override
    public Object getAttributeValue(String name) {
        return activity.getRequestAdapter().getAttribute(name);
    }

    @Override
    public void setAttributeValue(String name, Object value) {
        activity.getRequestAdapter().setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        activity.getRequestAdapter().removeAttribute(name);
    }

    @Override
    public String transformURL(String url) {
        return activity.getResponseAdapter().transformPath(url);
    }

    @NonNull
    public static WebActivityExchange buildExchange(Activity activity) {
        Assert.notNull(activity, "activity must not be null");
        if (activity instanceof WebActivity webActivity) {
            WebActivityRequest request = new WebActivityRequest(webActivity.getRequestAdapter());
            WebActivitySession session = new WebActivitySession(webActivity.getSessionAdapter());
            WebActivityApplication application = new WebActivityApplication(webActivity.getRequest().getServletContext());
            return new WebActivityExchange(activity, request, session, application);
        } else {
            throw new IllegalArgumentException("activity must be WebActivity");
        }
    }

}
