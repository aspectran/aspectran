/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.core.util.nodelet;

import com.aspectran.core.util.ArrayStack;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The NodeletParser is a callback based parser similar to SAX.  The big
 * difference is that rather than having a single callback for all nodes,
 * the NodeletParser has a number of callbacks mapped to various nodes.
 * The callback is called a Nodelet, and it is registered
 * with the NodeletParser against a specific XPath.
 */
public class NodeletParser {

    private static final Logger logger = LoggerFactory.getLogger(NodeletParser.class);

    private static final Map<String, String> EMPTY_ATTRIBUTES = Collections.emptyMap();

    private final Map<String, Nodelet> nodeletMap = new HashMap<>();

    private final Map<String, EndNodelet> endNodeletMap = new HashMap<>();

    private final ArrayStack<Object> objectStack = new ArrayStack<>();

    private final Object nodeParser;

    private boolean validating;

    private EntityResolver entityResolver;

    private NodeTracker nodeTracker;

    private String xpath;

    public NodeletParser(Object nodeParser) {
        this.nodeParser = nodeParser;
    }

    @SuppressWarnings("unchecked")
    public <N> N getNodeParser() {
        return (N)nodeParser;
    }

    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    public NodeTracker trackingLocation() {
        this.nodeTracker = new NodeTracker();
        return nodeTracker;
    }

    public NodeTracker getNodeTracker() {
        return nodeTracker;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    /**
     * Registers a nodelet to process attributes for the specified XPath.
     * It supports the following XPaths:
     * <ul>
     * <li> Element Path - /rootElement/childElement/theElement </li>
     * </ul>
     * @param nodelet the nodelet for processing start elements and attributes
     */
    public void addNodelet(Nodelet nodelet) {
        nodeletMap.put(xpath, nodelet);
    }

    /**
     * Registers the nodelet to process the end elements of the specified XPath
     * and the text and CDATA data collected.
     * @param nodelet the nodelet for processing end elements, text and CDATA data collected
     */
    public void addEndNodelet(EndNodelet nodelet) {
        endNodeletMap.put(xpath, nodelet);
    }

    /**
     * Adds the nodelet.
     * @param subnodeParser the subnode parser
     */
    public void addNodelet(SubnodeParser subnodeParser) {
        addNodelet(xpath, subnodeParser);
    }

    /**
     * Add nodelets through the subnode parser.
     * @param xpath the xpath
     * @param subnodeParser the subnode parser
     */
    public void addNodelet(String xpath, SubnodeParser subnodeParser) {
        subnodeParser.parse(xpath, this);
        setXpath(xpath);
    }

    public void pushObject(Object object) {
        objectStack.push(object);
    }

    @SuppressWarnings("unchecked")
    public <T> T popObject() {
        return (T)objectStack.pop();
    }

    @SuppressWarnings("unchecked")
    public <T> T peekObject() {
        return (T)objectStack.peek();
    }

    @SuppressWarnings("unchecked")
    public <T> T peekObject(int n) {
        return (T)objectStack.peek(n);
    }

    @SuppressWarnings("unchecked")
    public <T> T peekObject(Class<?> target) {
        return (T)objectStack.peek(target);
    }

    /**
     * Clear object stack.
     */
    public void clearObjectStack() {
        objectStack.clear();
    }

    /**
     * Begins parsing from the provided Reader.
     * @param reader the reader
     * @throws NodeletException the nodelet exception
     */
    public void parse(Reader reader) throws NodeletException {
        parse(new InputSource(reader));
    }

    /**
     * Begins parsing from the provided InputStream.
     * @param inputStream the input stream
     * @throws NodeletException the nodelet exception
     */
    public void parse(InputStream inputStream) throws NodeletException {
        parse(new InputSource(inputStream));
    }

    /**
     * Begins parsing from the provided InputSource.
     * @param inputSource the input source
     * @throws NodeletException the nodelet exception
     */
    public void parse(InputSource inputSource) throws NodeletException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(validating);
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setEntityResolver(entityResolver);
            reader.setContentHandler(new DefaultHandler() {
                private Locator locator;
                private final Path path = new Path(getNodeTracker());
                private final StringBuilder textBuffer = new StringBuilder();

                @Override
                public void setDocumentLocator(Locator locator) {
                    // Save the locator, so that it can be used later for line tracking when traversing nodes.
                    this.locator = locator;
                }

                @Override
                public void startDocument() throws SAXException {
                    Nodelet nodelet = nodeletMap.get("/");
                    if (nodelet != null) {
                        try {
                            nodelet.process(EMPTY_ATTRIBUTES);
                        } catch (Exception e) {
                            throw new SAXException("Error processing nodelet at the beginning of document", e);
                        }
                    }
                }

                @Override
                public void endDocument() throws SAXException {
                    EndNodelet nodelet = endNodeletMap.get("/");
                    if (nodelet != null) {
                        try {
                            nodelet.process(null);
                        } catch (Exception e) {
                            throw new SAXException("Error processing nodelet at the end of document", e);
                        }
                    }
                }

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes)
                        throws SAXException {
                    if (nodeTracker != null) {
                        nodeTracker.setName(qName);
                        nodeTracker.setLocation(locator.getLineNumber(), locator.getColumnNumber());
                    }

                    path.add(qName);
                    String pathString = path.toString();

                    Map<String, String> attrs = parseAttributes(attributes);
                    Nodelet nodelet = nodeletMap.get(pathString);
                    if (nodelet != null) {
                        try {
                            nodelet.process(attrs);
                        } catch (Exception e) {
                            if (nodeTracker != null) {
                                throw new SAXException("Error processing nodelet at start element " + nodeTracker, e);
                            } else {
                                throw new SAXException("Error processing nodelet at start element <" + qName + ">", e);
                            }
                        }
                    }

                    if (textBuffer.length() > 0) {
                        textBuffer.delete(0, textBuffer.length());
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (nodeTracker != null) {
                        nodeTracker.setClonedNodeTracker(path.getNodeTracker());
                        nodeTracker.setLocation(locator.getLineNumber(), locator.getColumnNumber());
                    }

                    String pathString = path.toString();
                    path.remove();

                    String text = null;
                    if (textBuffer.length() > 0) {
                        text = textBuffer.toString();
                        textBuffer.delete(0, textBuffer.length());
                    }

                    EndNodelet nodelet = endNodeletMap.get(pathString);
                    if (nodelet != null) {
                        try {
                            nodelet.process(text);
                        } catch (Exception e) {
                            if (nodeTracker != null) {
                                throw new SAXException("Error processing nodelet at end element " + nodeTracker, e);
                            } else {
                                throw new SAXException("Error processing nodelet at end element <" + qName + ">", e);
                            }
                        }
                    }
                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    textBuffer.append(ch, start, length);
                }

                private Map<String, String> parseAttributes(Attributes attributes) {
                    if (attributes == null) {
                        return EMPTY_ATTRIBUTES;
                    }
                    Map<String, String> attr = new HashMap<>();
                    for (int i = 0; i < attributes.getLength(); i++) {
                        attr.put(attributes.getQName(i), attributes.getValue(i));
                    }
                    return attr;
                }
            });
            reader.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(SAXParseException e) throws SAXException {
                    logger.error(e.toString());
                    throw e;
                }

                @Override
                public void fatalError(SAXParseException e) throws SAXException {
                    logger.error(e.toString());
                    throw e;
                }

                @Override
                public void warning(SAXParseException e) throws SAXException {
                    logger.warn(e.toString());
                }
            });
            reader.parse(inputSource);
        } catch (SAXParseException e) {
            throw new NodeletException("Error parsing XML; " + e);
        } catch (Exception e) {
            throw new NodeletException("Error parsing XML", e);
        }
    }

    /**
     * Inner helper class that assists with building XPath paths.
     */
    private static class Path {

        private final List<String> nodeList = new ArrayList<>();

        private final List<NodeTracker> trackerList = new ArrayList<>();

        private final NodeTracker nodeTracker;

        private String path;

        private Path(NodeTracker NodeTracker) {
            this.nodeTracker = NodeTracker;
        }

        public void add(String node) {
            nodeList.add(node);
            path = null;
            if (nodeTracker != null) {
                trackerList.add(nodeTracker.clone());
            }
        }

        public void remove() {
            int index = nodeList.size() - 1;
            nodeList.remove(index);
            path = null;
            if (nodeTracker != null) {
                trackerList.remove(index);
            }
        }

        public NodeTracker getNodeTracker() {
            return trackerList.get(trackerList.size() - 1);
        }

        @Override
        public String toString() {
            if (path != null) {
                return path;
            }
            StringBuilder sb = new StringBuilder(128);
            for (String name : nodeList) {
                sb.append("/").append(name);
            }
            path = sb.toString();
            return path;
        }

    }

}
