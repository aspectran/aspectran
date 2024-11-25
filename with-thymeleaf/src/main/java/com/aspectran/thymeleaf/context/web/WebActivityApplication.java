package com.aspectran.thymeleaf.context.web;

import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import jakarta.servlet.ServletContext;
import org.thymeleaf.web.servlet.IServletWebApplication;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

public class WebActivityApplication implements IServletWebApplication {

    private final ServletContext servletContext;

    WebActivityApplication(@NonNull ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return this.servletContext.getAttributeNames();
    }

    @Override
    public Object getAttributeValue(String name) {
        return this.servletContext.getAttribute(name);
    }

    @Override
    public void setAttributeValue(String name, Object value) {
        this.servletContext.setAttribute(name, value);
    }


    @Override
    public InputStream getResourceAsStream(String path) {
        Assert.notNull(path, "Path cannot be null");
        return this.servletContext.getResourceAsStream(path);
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        Assert.notNull(path, "Path cannot be null");
        return this.servletContext.getResource(path);
    }

    @Override
    public Object getNativeServletContextObject() {
        return this.servletContext;
    }

}
