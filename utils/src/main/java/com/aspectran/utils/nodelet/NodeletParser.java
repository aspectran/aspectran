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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A callback-based XML parser that is similar to SAX, but maps callbacks to specific
 * XPath expressions. The {@code NodeletParser} uses a {@link NodeletGroup} to define
 * a set of parsing rules. When the parser encounters an XML element matching a
 * registered XPath, it invokes the corresponding {@link Nodelet#process(Map)} method.
 * This approach allows for a more structured and modular way to handle complex
 * XML documents compared to traditional SAX parsing.
 */
public class NodeletParser {

    private static final Logger logger = LoggerFactory.getLogger(NodeletParser.class);

    private static final Map<String, String> EMPTY_ATTRIBUTES = Collections.emptyMap();

    private final ArrayStack<Object> objectStack = new ArrayStack<>();

    private final NodeletGroup nodeletGroup;

    private boolean validating;

    private EntityResolver entityResolver;

    private NodeTracker nodeTracker;

    /**
     * Constructs a new NodeletParser with the specified set of parsing rules.
     * @param nodeletGroup the root group of nodelets that defines the parsing behavior
     */
    public NodeletParser(NodeletGroup nodeletGroup) {
        this.nodeletGroup = nodeletGroup;
    }

    /**
     * Returns the object stack used by the parser.
     * This stack can be used by nodelets to pass objects between parent and child element handlers.
     * @return the object stack
     */
    public ArrayStack<Object> getObjectStack() {
        return objectStack;
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
     * @return the node tracker, or null if not enabled
     */
    @Nullable
    public NodeTracker getNodeTracker() {
        return nodeTracker;
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
            reader.setContentHandler(new DefaultContentHandler());
            reader.setErrorHandler(new DefaultErrorHandler());
            reader.parse(inputSource);
        } catch (Exception e) {
            throw new NodeletException("Error parsing XML", e);
        }
    }

    private class DefaultContentHandler extends DefaultHandler {
        private final NodeletPath path = new NodeletPath();
        private final StringBuilder textBuffer = new StringBuilder();
        private final ArrayStack<MountStatus> mountStatusStack = new ArrayStack<>();
        private Locator locator;

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        @Override
        public void startDocument() throws SAXException {
            Nodelet nodelet = nodeletGroup.getNodelet(path.toString());
            if (nodelet != null) {
                try {
                    nodelet.process(EMPTY_ATTRIBUTES);
                } catch (Exception e) {
                    throw new SAXParseException("Error processing nodelet: " + path, locator, e);
                }
            }
        }

        @Override
        public void endDocument() throws SAXException {
            EndNodelet nodelet = nodeletGroup.getEndNodelet(path.toString());
            if (nodelet != null) {
                try {
                    nodelet.process(null);
                } catch (Exception e) {
                    throw new SAXParseException("Error processing nodelet: " + path, locator, e);
                }
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            path.add(qName);

            if (nodeTracker != null) {
                nodeTracker.setXpath(path.toString());
                nodeTracker.setLocation(locator.getLineNumber(), locator.getColumnNumber());
            }

            Nodelet nodelet;
            if (mountStatusStack.isEmpty()) {
                nodelet = nodeletGroup.getNodelet(path.toString());
            } else {
                MountStatus mountStatus = mountStatusStack.peek();
                nodelet = mountStatus.getNodelet(path.getMountXpath());
            }

            // If no nodelet is found, try to find and activate a mounted nodelet group.
            if (nodelet == null) {
                String triggerName = path.findTriggerName();
                if (triggerName != null) {
                    String mountPath = NodeletGroup.makeMountPath(triggerName, qName);
                    NodeletGroup mountedGroup = nodeletGroup.getMountedGroup(mountPath);
                    if (mountedGroup != null) {
                        int mountIndex = path.mount();
                        MountStatus mountStatus = MountStatus.of(mountIndex, mountPath, mountedGroup);
                        mountStatusStack.push(mountStatus);
                        nodelet = mountStatus.getNodelet(path.getMountXpath());
                    }
                }
            }

            if (nodelet != null) {
                try {
                    nodelet.process(parseAttributes(attributes));
                } catch (Exception e) {
                    throw new SAXParseException("Error processing nodelet: " + path, locator, e);
                }
            }

            if (!textBuffer.isEmpty()) {
                textBuffer.delete(0, textBuffer.length());
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            String xpath = path.toString();

            EndNodelet endNodelet;
            if (mountStatusStack.isEmpty()) {
                endNodelet = nodeletGroup.getEndNodelet(xpath);
            } else {
                MountStatus mountStatus = mountStatusStack.peek();
                endNodelet = mountStatus.getEndNodelet(path.getMountXpath());
                if (endNodelet == null) {
                    throw new SAXParseException("End nodelet not found for \"" + path.getMountXpath() + "\"", locator);
                }
                // If in a mounted state, check if it's time to unmount.
                String triggerName = path.findTriggerName();
                if (triggerName != null) {
                    String mountPath = NodeletGroup.makeMountPath(triggerName, qName);
                    if (mountPath.equals(mountStatus.getPath())) {
                        mountStatusStack.pop();
                        if (mountStatusStack.isEmpty()) {
                            path.unmount();
                        } else {
                            path.remount(mountStatusStack.peek().getIndex());
                        }
                    }
                }
            }

            if (endNodelet != null) {
                String text = null;
                if (!textBuffer.isEmpty()) {
                    text = textBuffer.toString();
                    textBuffer.delete(0, textBuffer.length());
                }

                try {
                    endNodelet.process(text);
                } catch (Exception e) {
                    throw new SAXParseException("Error processing end nodelet: " + path, locator, e);
                }
            }

            path.remove();
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
    }

    private static class DefaultErrorHandler implements ErrorHandler {
        @Override
        public void error(@NonNull SAXParseException e) throws SAXException {
            logger.error(e.toString());
            throw e;
        }

        @Override
        public void fatalError(@NonNull SAXParseException e) throws SAXException {
            logger.error(e.toString());
            throw e;
        }

        @Override
        public void warning(@NonNull SAXParseException e) throws SAXException {
            logger.warn(e.toString());
        }
    }

}
