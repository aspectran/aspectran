/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.web.adapter;

import com.aspectran.core.adapter.AbstractApplicationAdapter;

import javax.servlet.ServletContext;
import java.util.Enumeration;

/**
 * The Class WebApplicationAdapter.
 * 
 * @since 2011. 3. 13.
 */
public class WebApplicationAdapter extends AbstractApplicationAdapter {

    /**
     * Instantiates a new WebApplicationAdapter.
     *
     * @param servletContext the servlet context
     */
    public WebApplicationAdapter(ServletContext servletContext) {
        super(servletContext);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T)((ServletContext)adaptee).getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object o) {
        ((ServletContext)adaptee).setAttribute(name, o);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return ((ServletContext)adaptee).getAttributeNames();
    }

    @Override
    public void removeAttribute(String name) {
        ((ServletContext)adaptee).removeAttribute(name);
    }

}
