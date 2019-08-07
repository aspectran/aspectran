package com.aspectran.undertow.server.servlet;

import io.undertow.servlet.api.FilterInfo;

import javax.servlet.Filter;
import java.util.Map;

/**
 * <p>Created: 2019-08-05</p>
 */
public class TowFilter extends FilterInfo {

    @SuppressWarnings("unchecked")
    public TowFilter(String name, String filterClass) throws ClassNotFoundException {
        this(name, (Class<? extends Filter>)TowFilter.class.getClassLoader().loadClass(filterClass));
    }

    public TowFilter(String name, Class<? extends Filter> filterClass) {
        super(name, filterClass);
    }

    public void setInitParams(Map<String, String> initParams) {
        if (initParams != null) {
            for (Map.Entry<String, String> entry : initParams.entrySet()) {
                addInitParam(entry.getKey(), entry.getValue());
            }
        }
    }

}
