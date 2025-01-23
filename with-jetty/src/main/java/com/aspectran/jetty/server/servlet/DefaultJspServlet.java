package com.aspectran.jetty.server.servlet;

import org.apache.jasper.servlet.JspServlet;

/**
 * <p>Created: 2025-01-23</p>
 */
public class DefaultJspServlet extends JettyServlet {

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
