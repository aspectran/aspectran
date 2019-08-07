package com.aspectran.undertow.server.servlet;

import io.undertow.servlet.api.ServletInfo;

import javax.servlet.Servlet;
import java.util.Map;

/**
 * <p>Created: 2019-08-05</p>
 */
public class TowServlet extends ServletInfo {

    @SuppressWarnings("unchecked")
    public TowServlet(String name, String servletClass) throws ClassNotFoundException {
        this(name, (Class<? extends Servlet>)TowServlet.class.getClassLoader().loadClass(servletClass));
    }

    public TowServlet(String name, Class<? extends Servlet> servletClass) {
        super(name, servletClass);
    }

    public void setMappings(String[] mappings) {
        if (mappings != null) {
            for (String mapping : mappings) {
                addMapping(mapping);
            }
        }
    }

    public void setInitParams(Map<String, String> initParams) {
        if (initParams != null) {
            for (Map.Entry<String, String> entry : initParams.entrySet()) {
                addInitParam(entry.getKey(), entry.getValue());
            }
        }
    }

}
