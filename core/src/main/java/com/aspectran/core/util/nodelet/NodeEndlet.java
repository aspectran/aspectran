package com.aspectran.core.util.nodelet;

/**
 * A nodelet is a sort of callback or event handler that can be registered
 * to handle an XPath event registered with the NodeParser.
 * In particular, nodelets for processing end elements, text, and CDATA data
 * are called NodeEndlet.
 *
 * <p>Created: 2017. 11. 2.</p>
 */
public interface NodeEndlet {

    /**
     * For a registered XPath, the NodeletParser will call the Nodelet's
     * process method for processing.
     *
     * @param text the text and CDATA data collected
     * @throws Exception if an error occurs while processing the nodelet
     */
    void process(String text) throws Exception;

}
