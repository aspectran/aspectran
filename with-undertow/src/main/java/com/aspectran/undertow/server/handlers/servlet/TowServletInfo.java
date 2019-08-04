package com.aspectran.undertow.server.handlers.servlet;

import io.undertow.servlet.api.ServletInfo;

import javax.servlet.Servlet;

/**
 * <p>Created: 2019-08-05</p>
 */
public class TowServletInfo extends ServletInfo {

    public TowServletInfo(String name, Class<? extends Servlet> servletClass) {
        super(name, servletClass);
    }

}
