package com.aspectran.undertow.server.handlers.servlet;

import org.apache.jasper.servlet.JasperInitializer;
import org.apache.jasper.servlet.TldScanner;

import javax.servlet.ServletContext;

/**
 * Initializer for the Jasper JSP Engine.
 */
public class TowJasperInitializer extends JasperInitializer {

    private String tldResourcePath;

    public TowJasperInitializer() {
    }

    public void setTldResourcePath(String tldResourcePath) {
        this.tldResourcePath = tldResourcePath;
    }

    @Override
    protected TldScanner newTldScanner(ServletContext context, boolean namespaceAware,
                                       boolean validate, boolean blockExternal) {
        TowTldScanner tldScanner = new TowTldScanner(context, namespaceAware, validate, blockExternal);
        if (tldResourcePath != null) {
            tldScanner.setTldResourcePath(tldResourcePath);
        }
        return tldScanner;
    }

}
