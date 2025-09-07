/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.undertow.server.servlet;

import com.aspectran.utils.ClassUtils;
import io.undertow.servlet.api.FilterInfo;
import jakarta.servlet.Filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Represents a filter that can be added to a deployment.
 *
 * <p>The following example shows how to configure a {@code WebActivityFilter} using {@code TowFilter}.
 * The {@code initParams} property is used to set the initialization parameters for the filter.
 * In this case, the {@code bypasses} parameter is configured to specify a list of URL patterns
 * that should be excluded from filtering. When a request URI matches one of these patterns,
 * the {@code WebActivityFilter} will bypass its main logic and pass the request directly to
 * the default servlet, which is useful for serving static resources like images, CSS, and
 * JavaScript files efficiently.
 * </p>
 * <pre>{@code
 *   <bean class="com.aspectran.undertow.server.servlet.TowFilter">
 *     <argument>activityFilter</argument>
 *     <argument>com.aspectran.web.servlet.filter.WebActivityFilter</argument>
 *     <property name="initParams" type="map">
 *       <entry name="bypasses">
 *         /assets/**
 *         /favicon.ico
 *         /robots.txt
 *         /ads.txt
 *       </entry>
 *     </property>
 *     <property name="servletMappings" type="array">
 *       <bean class="com.aspectran.undertow.server.servlet.TowFilterMapping">
 *         <argument>webActivityServlet</item>
 *       </bean>
 *     </property>
 *   </bean>
 * }</pre>
 *
 * <p>Created: 2019-08-05</p>
 */
public class TowFilter extends FilterInfo {

    private TowFilterUrlMapping[] urlMappings;

    private TowFilterServletMapping[] servletMappings;

    /**
     * Creates a new filter with the specified name and filter class name.
     * @param name the filter name
     * @param filterClass the filter class name
     * @throws ClassNotFoundException if the class is not found
     */
    public TowFilter(String name, String filterClass) throws ClassNotFoundException {
        this(name, ClassUtils.loadClass(filterClass));
    }

    /**
     * Creates a new filter with the specified name and filter class.
     * @param name the filter name
     * @param filterClass the filter class
     */
    public TowFilter(String name, Class<? extends Filter> filterClass) {
        super(name, filterClass);
    }

    /**
     * Returns the URL pattern mappings for this filter.
     * @return the URL mappings
     */
    public TowFilterUrlMapping[] getUrlMappings() {
        return urlMappings;
    }

    /**
     * Sets the URL patterns to which this filter applies.
     * @param mappingUrls the mapping urls
     */
    public void setMappingUrls(String[] mappingUrls) {
        if (mappingUrls != null) {
            List<TowFilterMapping> mappingList = new ArrayList<>(mappingUrls.length);
            for (String url : mappingUrls) {
                mappingList.add(new TowFilterMapping(url));
            }
            setUrlMappings(mappingList.toArray(new TowFilterMapping[0]));
        }
    }

    /**
     * Sets the URL pattern mappings for this filter.
     * @param towFilterMappings the filter mappings
     */
    public void setUrlMappings(TowFilterMapping[] towFilterMappings) {
        if (towFilterMappings != null) {
            List<TowFilterUrlMapping> urlMappingList;
            if (urlMappings != null) {
                urlMappingList = new ArrayList<>(servletMappings.length + towFilterMappings.length);
                urlMappingList.addAll(Arrays.asList(urlMappings));
            } else {
                urlMappingList = new ArrayList<>(towFilterMappings.length);
            }
            for (TowFilterMapping mapping : towFilterMappings) {
                urlMappingList.addAll(TowFilterUrlMapping.of(getName(), mapping));
            }
            urlMappings = urlMappingList.toArray(new TowFilterUrlMapping[0]);
        }
    }

    /**
     * Returns the servlet name mappings for this filter.
     * @return the servlet mappings
     */
    public TowFilterServletMapping[] getServletMappings() {
        return servletMappings;
    }

    /**
     * Sets the servlet names to which this filter applies.
     * @param mappingServlets the mapping servlets
     */
    public void setMappingServlets(String[] mappingServlets) {
        if (mappingServlets != null) {
            List<TowFilterMapping> mappingList = new ArrayList<>(mappingServlets.length);
            for (String servletName : mappingServlets) {
                mappingList.add(new TowFilterMapping(servletName));
            }
            setServletMappings(mappingList.toArray(new TowFilterMapping[0]));
        }
    }

    /**
     * Sets the servlet name mappings for this filter.
     * @param towFilterMappings the filter mappings
     */
    public void setServletMappings(TowFilterMapping[] towFilterMappings) {
        if (towFilterMappings != null) {
            List<TowFilterServletMapping> servletMappingList;
            if (servletMappings != null) {
                servletMappingList = new ArrayList<>(servletMappings.length + towFilterMappings.length);
                servletMappingList.addAll(Arrays.asList(servletMappings));
            } else {
                servletMappingList = new ArrayList<>(towFilterMappings.length);
            }
            for (TowFilterMapping towFilterMapping : towFilterMappings) {
                servletMappingList.addAll(TowFilterServletMapping.of(getName(), towFilterMapping));
            }
            servletMappings = servletMappingList.toArray(new TowFilterServletMapping[0]);
        }
    }

    /**
     * Sets the initialization parameters for this filter.
     * @param initParams the init params
     */
    public void setInitParams(Map<String, String> initParams) {
        if (initParams != null) {
            for (Map.Entry<String, String> entry : initParams.entrySet()) {
                addInitParam(entry.getKey(), entry.getValue());
            }
        }
    }

}
