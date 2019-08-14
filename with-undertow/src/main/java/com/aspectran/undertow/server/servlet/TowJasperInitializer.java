package com.aspectran.undertow.server.servlet;

import com.aspectran.core.component.bean.aware.ClassLoaderAware;
import org.apache.jasper.servlet.JasperInitializer;
import org.apache.jasper.servlet.TldScanner;

import javax.servlet.ServletContext;

/**
 * Initializer for the Jasper JSP Engine.
 */
public class TowJasperInitializer extends JasperInitializer implements ClassLoaderAware {

    private ClassLoader classLoader;

    private String[] jarsToScan;

    public TowJasperInitializer() {
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setJarsToScan(String[] jarsToScan) {
        this.jarsToScan = jarsToScan;
    }

    @Override
    protected TldScanner newTldScanner(ServletContext context, boolean namespaceAware,
                                       boolean validate, boolean blockExternal) {
        TowTldScanner tldScanner = new TowTldScanner(context, namespaceAware, validate, blockExternal);
        if (classLoader != null) {
            tldScanner.setClassLoader(classLoader);
        }
        if (jarsToScan != null) {
            tldScanner.setJarsToScan(jarsToScan);
        }
        return tldScanner;
    }

}
