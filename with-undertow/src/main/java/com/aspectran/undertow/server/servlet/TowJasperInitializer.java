package com.aspectran.undertow.server.servlet;

import org.apache.jasper.servlet.JasperInitializer;
import org.apache.jasper.servlet.TldScanner;

import javax.servlet.ServletContext;

/**
 * Initializer for the Jasper JSP Engine.
 */
public class TowJasperInitializer extends JasperInitializer {

    private String[] jarsToScan;

    public TowJasperInitializer() {
    }

    public void setJarsToScan(String[] jarsToScan) {
        this.jarsToScan = jarsToScan;
    }

    @Override
    protected TldScanner newTldScanner(ServletContext context, boolean namespaceAware,
                                       boolean validate, boolean blockExternal) {
        TowTldScanner tldScanner = new TowTldScanner(context, namespaceAware, validate, blockExternal);
        if (jarsToScan != null) {
            tldScanner.setJarsToScan(jarsToScan);
        }
        return tldScanner;
    }

}
