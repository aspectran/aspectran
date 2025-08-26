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

import org.apache.jasper.servlet.JspServlet;

/**
 * The default JSP servlet.
 *
 * <p>Created: 2025-01-23</p>
 */
public class DefaultJspServlet extends TowServlet {

    /**
     * Instantiates a new default jsp servlet.
     */
    public DefaultJspServlet() {
        super("Default JSP Servlet", JspServlet.class);
        setMappings(new String[] {
            "*.jsp",
            "*.jspf",
            "*.jspx",
            "*.xsp",
            "*.JSP",
            "*.JSPF",
            "*.JSPX",
            "*.XSP"
        });
    }

}
