package com.aspectran.undertow.server.handlers.servlet;

import io.undertow.servlet.api.FilterMappingInfo;

import javax.servlet.DispatcherType;

/**
 * <p>Created: 2019-08-05</p>
 */
public class TowFilterUrlMapping extends FilterMappingInfo {

    public TowFilterUrlMapping(String filterName, String mapping) {
        this(filterName, mapping, DispatcherType.REQUEST);
    }

    public TowFilterUrlMapping(String filterName, String mapping, DispatcherType dispatcher) {
        super(filterName, MappingType.URL, mapping, dispatcher);
    }

}
