package com.aspectran.undertow.server.handlers.servlet;

import org.apache.jasper.servlet.TldScanner;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import java.io.IOException;

/**
 * Scans for and loads Tag Library Descriptors contained in a web application.
 */
public class TowTldScanner extends TldScanner {

    private String tldResourcePath;

    /**
     * Initialise with the application's ServletContext.
     *
     * @param context        the application's servletContext
     * @param namespaceAware should the XML parser used to parse TLD files be
     *                       configured to be name space aware
     * @param validation     should the XML parser used to parse TLD files be
     *                       configured to use validation
     * @param blockExternal  should the XML parser used to parse TLD files be
     *                       configured to be block references to external
     */
    public TowTldScanner(ServletContext context, boolean namespaceAware, boolean validation, boolean blockExternal) {
        super(context, namespaceAware, validation, blockExternal);
    }

    public void setTldResourcePath(String tldResourcePath) {
        this.tldResourcePath = tldResourcePath;
    }

    /**
     * Scan for TLDs in all places defined by the specification:
     * <ol>
     * <li>Tag libraries defined by the platform</li>
     * <li>Entries from &lt;jsp-config&gt; in web.xml</li>
     * <li>A resources under /WEB-INF</li>
     * <li>In jar files from /WEB-INF/lib</li>
     * <li>Additional entries from the container</li>
     * </ol>
     *
     * @throws IOException  if there was a problem scanning for or loading a TLD
     * @throws SAXException if there was a problem parsing a TLD
     */
    @Override
    public void scan() throws IOException, SAXException {
        if (tldResourcePath != null) {
            scanResourcePaths(tldResourcePath);
        } else {
            super.scan();
        }
    }

}
