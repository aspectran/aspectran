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
package com.aspectran.utils.nodelet;

import com.aspectran.utils.ArrayStack;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * A callback-based parser similar to SAX, but with callbacks mapped to specific XPath expressions.
 * <p>The {@code NodeletParser} registers {@link Nodelet}s (callbacks) with specific XPath patterns.
 * When the parser encounters a node matching a registered XPath, the corresponding Nodelet's
 * {@link Nodelet#process(Map)} method is invoked.</p>
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

    /**
     * Creates a new NodeletParser.
     * @param nodeParser the object that owns this parser (e.g., the main parser context)
     */
    public NodeletParser(Object nodeParser) {
        this.nodeParser = nodeParser;
    }

    /**
     * Returns the owner object of this parser.
     * @param <N> the type of the owner object
     * @return the owner object
     */
    @SuppressWarnings("unchecked")
    public <N> N getNodeParser() {
        return (N)nodeParser;
    }

    /**
     * Sets whether the parser should perform XML schema validation.
     * @param validating {@code true} to enable validation, {@code false} otherwise
     */
    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    /**
     * Sets the {@link EntityResolver} to be used by the underlying SAX parser.
     * @param entityResolver the entity resolver
     */
    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    /**
     * Enables location tracking during parsing.
     * <p>A new {@link NodeTracker} instance is created and used to track the current node's
     * name and source location (line/column number).</p>
     * @return the created {@link NodeTracker} instance
     */
    public NodeTracker trackingLocation() {
        this.nodeTracker = new NodeTracker();
        return nodeTracker;
    }

    /**
     * Returns the {@link NodeTracker} instance used by this parser.
     * @return the node tracker
     */
    public NodeTracker getNodeTracker() {
        return nodeTracker;
    }

    /**
     * Returns the current XPath being processed by the parser.
     * @return the current XPath
     */
    public String getXpath() {
        return xpath;
    }

    /**
     * Sets the current XPath for subsequent nodelet registrations.
     * @param xpath the XPath to set
     */
    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    /**
     * Registers a {@link Nodelet} to process start elements and their attributes for the current XPath.
     * @param nodelet the nodelet to register
     */
    public void addNodelet(Nodelet nodelet) {
        nodeletMap.put(xpath, nodelet);
    }

    /**
     * Registers an {@link EndNodelet} to process end elements, text, and CDATA for the current XPath.
     * @param nodelet the end nodelet to register
     */
    public void addEndNodelet(EndNodelet nodelet) {
        endNodeletMap.put(xpath, nodelet);
    }

    /**
     * Adds nodelets defined within a {@link SubnodeParser} for the current XPath.
     * @param subnodeParser the subnode parser containing nodelet definitions
     */
    public void addNodelet(SubnodeParser subnodeParser) {
        addNodelet(xpath, subnodeParser);
    }

    /**
     * Adds nodelets defined within a {@link SubnodeParser} for a specified XPath.
     * @param xpath the XPath for which to add nodelets
     * @param subnodeParser the subnode parser containing nodelet definitions
     */
    public void addNodelet(String xpath, @NonNull SubnodeParser subnodeParser) {
        subnodeParser.parse(xpath, this);
        setXpath(xpath);
    }

    /**
     * Pushes an object onto the internal object stack.
     * This stack is used to manage context objects during parsing.
     * @param object the object to push
     */
    public void pushObject(Object object) {
        objectStack.push(object);
    }

    /**
     * Pops an object from the top of the internal object stack.
     * @param <T> the expected type of the object
     * @return the object popped from the stack
     */
    @SuppressWarnings("unchecked")
    public <T> T popObject() {
        return (T)objectStack.pop();
    }

    /**
     * Peeks at the object on the top of the internal object stack without removing it.
     * @param <T> the expected type of the object
     * @return the object at the top of the stack
     */
    @SuppressWarnings("unchecked")
    public <T> T peekObject() {
        return (T)objectStack.peek();
    }

    /**
     * Peeks at an object at a specific depth from the top of the internal object stack.
     * @param <T> the expected type of the object
     * @param n the depth from the top (0 for top, 1 for next, etc.)
     * @return the object at the specified depth
     */
    @SuppressWarnings("unchecked")
    public <T> T peekObject(int n) {
        return (T)objectStack.peek(n);
    }

    /**
     * Peeks at an object of a specific type from the internal object stack.
     * It searches the stack from top to bottom for the first object assignable to the target type.
     * @param <T> the expected type of the object
     * @param target the target class type
     * @return the object of the specified type, or {@code null} if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T peekObject(Class<?> target) {
        return (T)objectStack.peek(target);
    }

    /**
     * Clears all objects from the internal object stack.
     */
    public void clearObjectStack() {
        objectStack.clear();
    }

    /**
     * Begins parsing an XML document from the provided {@link Reader}.
     * @param reader the reader for the XML document
     * @throws NodeletException if an error occurs during parsing
     */
    public void parse(Reader reader) throws NodeletException {
        parse(new InputSource(reader));
    }

    /**
     * Begins parsing an XML document from the provided {@link InputStream}.
     * @param inputStream the input stream for the XML document
     * @throws NodeletException if an error occurs during parsing
     */
    public void parse(InputStream inputStream) throws NodeletException {
        parse(new InputSource(inputStream));
    }

    /**
     * Begins parsing an XML document from the provided {@link InputSource}.
     * @param inputSource the input source for the XML document
     * @throws NodeletException if an error occurs during parsing
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

                    if (!textBuffer.isEmpty()) {
                        textBuffer.delete(0, textBuffer.length());
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (nodeTracker != null) {
                        nodeTracker.restoreStateFrom(path.getNodeTracker());
                        nodeTracker.setLocation(locator.getLineNumber(), locator.getColumnNumber());
                    }

                    String pathString = path.toString();
                    path.remove();

                    String text = null;
                    if (!textBuffer.isEmpty()) {
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
                trackerList.add(nodeTracker.createSnapshot());
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
