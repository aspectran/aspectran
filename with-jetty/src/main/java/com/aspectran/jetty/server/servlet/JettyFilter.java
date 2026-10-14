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
package com.aspectran.jetty.server.servlet;

import com.aspectran.utils.ClassUtils;
import jakarta.servlet.Filter;
import org.eclipse.jetty.ee10.servlet.FilterHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A {@link FilterHolder} that holds a {@link Filter} and its mappings.
 *
 * ex)
 * <pre>{@code
 *   <bean class="com.aspectran.jetty.server.servlet.TowFilter">
 *     <arguments>
 *       <item>activityFilter</item>
 *       <item>com.aspectran.web.servlet.filter.WebActivityFilter</item>
 *     </arguments>
 *     <properties>
 *       <item name="initParams" type="map">
 *         <entry name="bypasses">
 *           /assets/**
 *           /favicon.ico
 *           /robots.txt
 *           /ads.txt
 *         </entry>
 *       </item>
 *       <item name="servletMappings" type="array">
 *         <bean class="com.aspectran.jetty.server.servlet.TowFilterMapping">
 *           <arguments>
 *             <item>webActivityServlet</item>
 *           </arguments>
 *         </bean>
 *       </item>
 *     </properties>
 *   </bean>
 * }</pre>
 *
 * <p>Created: 4/23/24</p>
 */
public class JettyFilter extends FilterHolder {

    private JettyFilterUrlMapping[] urlMappings;

    private JettyFilterServletMapping[] servletMappings;

    /**
     * Creates a new filter with the specified name and filter class name.
     * @param name the filter name
     * @param filterClass the filter class name
     * @throws ClassNotFoundException if the class is not found
     */
    public JettyFilter(String name, String filterClass) throws ClassNotFoundException {
        this(name, ClassUtils.loadClass(filterClass));
    }

    /**
     * Creates a new filter with the specified name and filter class.
     * @param name the filter name
     * @param filterClass the filter class
     */
    public JettyFilter(String name, Class<? extends Filter> filterClass) {
        super();
        setName(name);
        setHeldClass(filterClass);
    }

    /**
     * Returns the URL pattern mappings for this filter.
     * @return the URL mappings
     */
    public JettyFilterUrlMapping[] getUrlMappings() {
        return urlMappings;
    }

    /**
     * Sets the URL patterns to which this filter applies.
     * @param mappingUrls the mapping urls
     */
    public void setMappingUrls(String[] mappingUrls) {
        if (mappingUrls != null) {
            List<JettyFilterMapping> mappingList = new ArrayList<>(mappingUrls.length);
            for (String url : mappingUrls) {
                mappingList.add(new JettyFilterMapping(url));
            }
            setUrlMappings(mappingList.toArray(new JettyFilterMapping[0]));
        }
    }

    /**
     * Sets the URL pattern mappings for this filter.
     * @param jettyFilterMappings the filter mappings
     */
    public void setUrlMappings(JettyFilterMapping[] jettyFilterMappings) {
        if (jettyFilterMappings != null) {
            List<JettyFilterUrlMapping> urlMappingList;
            if (urlMappings != null) {
                urlMappingList = new ArrayList<>(servletMappings.length + jettyFilterMappings.length);
                urlMappingList.addAll(Arrays.asList(urlMappings));
            } else {
                urlMappingList = new ArrayList<>(jettyFilterMappings.length);
            }
            for (JettyFilterMapping mapping : jettyFilterMappings) {
                urlMappingList.addAll(JettyFilterUrlMapping.of(getName(), mapping));
            }
            urlMappings = urlMappingList.toArray(new JettyFilterUrlMapping[0]);
        }
    }

    /**
     * Returns the servlet name mappings for this filter.
     * @return the servlet mappings
     */
    public JettyFilterServletMapping[] getServletMappings() {
        return servletMappings;
    }

    /**
     * Sets the servlet names to which this filter applies.
     * @param mappingServlets the mapping servlets
     */
    public void setMappingServlets(String[] mappingServlets) {
        if (mappingServlets != null) {
            List<JettyFilterMapping> mappingList = new ArrayList<>(mappingServlets.length);
            for (String servletName : mappingServlets) {
                mappingList.add(new JettyFilterMapping(servletName));
            }
            setServletMappings(mappingList.toArray(new JettyFilterMapping[0]));
        }
    }

    /**
     * Sets the servlet name mappings for this filter.
     * @param jettyFilterMappings the filter mappings
     */
    public void setServletMappings(JettyFilterMapping[] jettyFilterMappings) {
        if (jettyFilterMappings != null) {
            List<JettyFilterServletMapping> servletMappingList;
            if (servletMappings != null) {
                servletMappingList = new ArrayList<>(servletMappings.length + jettyFilterMappings.length);
                servletMappingList.addAll(Arrays.asList(servletMappings));
            } else {
                servletMappingList = new ArrayList<>(jettyFilterMappings.length);
            }
            for (JettyFilterMapping jettyFilterMapping : jettyFilterMappings) {
                servletMappingList.addAll(JettyFilterServletMapping.of(getName(), jettyFilterMapping));
            }
            servletMappings = servletMappingList.toArray(new JettyFilterServletMapping[0]);
        }
    }

    /**
     * Sets the initialization parameters for this filter.
     * @param initParams the init params
     */
    public void setInitParams(Map<String, String> initParams) {
        if (initParams != null) {
            for (Map.Entry<String, String> entry : initParams.entrySet()) {
                setInitParameter(entry.getKey(), entry.getValue());
            }
        }
    }

}
