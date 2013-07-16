package com.aspectran.core.util.xml;

/**
 * A nodelet is a sort of callback or event handler that can be registered 
 * to handle an XPath event registered with the NodeParser.   
 */
public interface NodeletAdder {
    
    /**
     * For a registered XPath, the NodeletParser will call the Nodelet's
     * process method for processing.
     *
     * @param xpath the xpath
     * @param parser the nodelet parser
     */
    void process (String xpath, NodeletParser parser);
    
}
