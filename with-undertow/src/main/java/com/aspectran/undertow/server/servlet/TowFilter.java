/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
 * ex)
 * <pre>{@code
 *   <bean class="com.aspectran.undertow.server.servlet.TowFilter">
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
 *         <bean class="com.aspectran.undertow.server.servlet.TowFilterMapping">
 *           <arguments>
 *             <item>webActivityServlet</item>
 *           </arguments>
 *         </bean>
 *       </item>
 *     </properties>
 *   </bean>
 * }</pre>
 *
 * <p>Created: 2019-08-05</p>
 */
public class TowFilter extends FilterInfo {

    private TowFilterUrlMapping[] urlMappings;

    private TowFilterServletMapping[] servletMappings;

    public TowFilter(String name, String filterClass) throws ClassNotFoundException {
        this(name, ClassUtils.loadClass(filterClass));
    }

    public TowFilter(String name, Class<? extends Filter> filterClass) {
        super(name, filterClass);
    }

    public TowFilterUrlMapping[] getUrlMappings() {
        return urlMappings;
    }

    public void setMappingUrls(String[] mappingUrls) {
        if (mappingUrls != null) {
            List<TowFilterMapping> mappingList = new ArrayList<>(mappingUrls.length);
            for (String url : mappingUrls) {
                mappingList.add(new TowFilterMapping(url));
            }
            setUrlMappings(mappingList.toArray(new TowFilterMapping[0]));
        }
    }

    public void setUrlMappings(TowFilterMapping[] towFilterMappings) {
        if (towFilterMappings != null) {
            List<TowFilterUrlMapping> urlMappingList;
            if (this.urlMappings != null) {
                urlMappingList = new ArrayList<>(this.servletMappings.length + towFilterMappings.length);
                urlMappingList.addAll(Arrays.asList(this.urlMappings));
            } else {
                urlMappingList = new ArrayList<>(towFilterMappings.length);
            }
            for (TowFilterMapping mapping : towFilterMappings) {
                urlMappingList.addAll(TowFilterUrlMapping.of(getName(), mapping));
            }
            this.urlMappings = urlMappingList.toArray(new TowFilterUrlMapping[0]);
        }
    }

    public TowFilterServletMapping[] getServletMappings() {
        return servletMappings;
    }

    public void setMappingServlets(String[] mappingServlets) {
        if (mappingServlets != null) {
            List<TowFilterMapping> mappingList = new ArrayList<>(mappingServlets.length);
            for (String servletName : mappingServlets) {
                mappingList.add(new TowFilterMapping(servletName));
            }
            setServletMappings(mappingList.toArray(new TowFilterMapping[0]));
        }
    }

    public void setServletMappings(TowFilterMapping[] towFilterMappings) {
        if (towFilterMappings != null) {
            List<TowFilterServletMapping> servletMappingList;
            if (this.servletMappings != null) {
                servletMappingList = new ArrayList<>(this.servletMappings.length + towFilterMappings.length);
                servletMappingList.addAll(Arrays.asList(this.servletMappings));
            } else {
                servletMappingList = new ArrayList<>(towFilterMappings.length);
            }
            for (TowFilterMapping towFilterMapping : towFilterMappings) {
                servletMappingList.addAll(TowFilterServletMapping.of(getName(), towFilterMapping));
            }
            this.servletMappings = servletMappingList.toArray(new TowFilterServletMapping[0]);
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
