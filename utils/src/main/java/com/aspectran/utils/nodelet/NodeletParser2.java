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
import com.aspectran.utils.annotation.jsr305.Nullable;
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
public class NodeletParser2 {

    private static final Logger logger = LoggerFactory.getLogger(NodeletParser2.class);

    private static final Map<String, String> EMPTY_ATTRIBUTES = Collections.emptyMap();

    private final ArrayStack<Object> objectStack = new ArrayStack<>();

    private final NodeletGroup nodeletGroup;

    private boolean validating;

    private EntityResolver entityResolver;

    private NodeTracker nodeTracker;

    public NodeletParser2(NodeletGroup nodeletGroup) {
        this.nodeletGroup = nodeletGroup;
    }

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
     * @return the node tracker
     */
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
//        } catch (SAXParseException e) {
//            throw new NodeletException("Error parsing XML" + e);
        } catch (Exception e) {
            throw new NodeletException("Error parsing XML", e);
        }
    }

    private static class MountInfo {
        int index;
        String path;
        NodeletGroup group;
        static MountInfo of(int index, String path, NodeletGroup group) {
            MountInfo info = new MountInfo();
            info.index = index;
            info.path = path;
            info.group = group;
            return info;
        }
    }

    /**
     * Inner helper class that assists with building XPath paths.
     */
    private static class QNamePath {
        private final List<String> nameList = new ArrayList<>();
        private final List<NodeTracker> trackerList = new ArrayList<>();
        private final NodeTracker nodeTracker;
        private String xpath;
        private int mountIndex;
//        private int mountDepth = -1;
        private String mountXpath;

        private QNamePath(NodeTracker NodeTracker) {
            this.nodeTracker = NodeTracker;
        }

        public void add(String node) {
            if (nodeTracker != null) {
                trackerList.add(nodeTracker.createSnapshot());
            }
            nameList.add(node);
            xpath = null;
            mountXpath = null;
//            if (mountDepth > -1) {
//                mountDepth++;
//            }
        }

        public void remove() {
            int index = nameList.size() - 1;
            nameList.remove(index);
            xpath = null;
            mountXpath = null;
//            if (mountDepth > -1) {
//                mountDepth--;
//            }
            if (nodeTracker != null) {
                trackerList.remove(index);
            }
        }

//        public int size() {
//            return nameList.size();
//        }

        public int mount() {
            mountIndex = nameList.size() - 1;
            mountXpath = null;
            return mountIndex;
//            mountDepth = 0;
//            mountXpath = null;
//            return getMountXpath();
        }

        public void remount(int index) {
            mountIndex = index;
            mountXpath = null;
        }

        public void unmount() {
            mountIndex = 0;
            mountXpath = null;
//            mountDepth = -1;
//            xpath = null;
        }

        public boolean isMounted() {
            return (mountIndex > 0);
        }

//        public int getMountDepth() {
//            return mountDepth;
//        }

        @Nullable
        public String getMountXpath() {
            if (mountXpath == null) {
                if (nameList.isEmpty()) {
                    return null;
                }
                if (nameList.size() == 1) {
                    mountXpath = nameList.get(0);
                } else {
                    StringBuilder sb = new StringBuilder(32);
                    for (int i = mountIndex; i < nameList.size(); i++) {
                        sb.append("/").append(nameList.get(i));
                    }
                    mountXpath = sb.toString();
                }
            }
            return mountXpath;
        }

        @Nullable
        public String findTriggerName() {
            return (nameList.size() > 2 ? nameList.get(nameList.size() - 2) : null);
        }

        public NodeTracker getNodeTracker() {
            return trackerList.get(trackerList.size() - 1);
        }

        @Override
        public String toString() {
            if (xpath != null) {
                return xpath;
            }
            StringBuilder sb = new StringBuilder(64);
            for (String name : nameList) {
                sb.append("/").append(name);
            }
            xpath = sb.toString();
            return xpath;
        }
    }

    private class DefaultContentHandler extends DefaultHandler {
        private final QNamePath path = new QNamePath(getNodeTracker());
        private final StringBuilder textBuffer = new StringBuilder();
//        private final ArrayStack<NodeletGroup> groupStack = new ArrayStack<>();
        private final ArrayStack<MountInfo> mountStack = new ArrayStack<>();
        private Locator locator;

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        @Override
        public void startDocument() throws SAXException {
//            groupStack.push(nodeletGroup);
            Nodelet nodelet = nodeletGroup.getNodelet("/");
            if (nodelet != null) {
                try {
                    nodelet.process(EMPTY_ATTRIBUTES);
                } catch (Exception e) {
                    throw new SAXException("Error processing the nodelet of the document", e);
                }
            }
        }

        @Override
        public void endDocument() throws SAXException {
            EndNodelet nodelet = nodeletGroup.getEndNodelet("/");
            if (nodelet != null) {
                try {
                    nodelet.process(null);
                } catch (Exception e) {
                    throw new SAXException("Error processing the end nodelet of the document", e);
                }
            }
//            groupStack.pop();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            path.add(qName);
            String xpath = path.toString();

            if (nodeTracker != null) {
                nodeTracker.setName(qName);
                nodeTracker.setPath(xpath);
                nodeTracker.setLocation(locator.getLineNumber(), locator.getColumnNumber());
            }

            Nodelet nodelet;
            if (mountStack.isEmpty()) {
                nodelet = nodeletGroup.getNodelet(xpath);
            } else {
                MountInfo mountInfo = mountStack.peek();
                nodelet = mountInfo.group.getNodelet(path.getMountXpath());
            }

            if (nodelet == null) {
                String triggerName = path.findTriggerName();
                if (triggerName != null) {
                    String mountPath = NodeletGroup.makeMountPath(triggerName, qName);
                    NodeletGroup mountedGroup = nodeletGroup.getMountedGroup(mountPath);
                    if (mountedGroup != null) {
                        int mountIndex = path.mount();
                        MountInfo mountInfo = MountInfo.of(mountIndex, mountPath, mountedGroup);
                        mountStack.push(mountInfo);
                        nodelet = mountedGroup.getNodelet(path.getMountXpath());
                    }
                }
            }

            if (nodelet != null) {
                try {
                    nodelet.process(parseAttributes(attributes));
                } catch (Exception e) {
                    throw new SAXParseException("Error processing nodelet \"" + path + "\"" , locator, e);
//                    if (nodeTracker != null) {
//                        throw new SAXException("Error processing nodelet at start element " + nodeTracker, e);
//                    } else {
//                        throw new SAXException("Error processing nodelet at start element <" + qName + ">", e);
//                    }
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

            String xpath = path.toString();

            EndNodelet endNodelet;
            if (mountStack.isEmpty()) {
                endNodelet = nodeletGroup.getEndNodelet(xpath);
            } else {
                MountInfo mountInfo = mountStack.peek();
                endNodelet = mountInfo.group.getEndNodelet(path.getMountXpath());
                if (endNodelet == null) {
                    throw new SAXParseException("End nodelet not found for \"" + path.getMountXpath() + "\"", locator);
                }
                String triggerName = path.findTriggerName();
                if (triggerName != null) {
                    String mountPath = NodeletGroup.makeMountPath(triggerName, qName);
                    if (mountPath.equals(mountInfo.path)) {
                        mountStack.pop();
                        if (mountStack.isEmpty()) {
                            path.unmount();
                        } else {
                            path.remount(mountStack.peek().index);
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
                    throw new SAXParseException("Error processing end nodelet \"" + path + "\"" , locator, e);
//                    if (nodeTracker != null) {
//                        throw new SAXException("Error processing nodelet at end element " + nodeTracker, e);
//                    } else {
//                        throw new SAXException("Error processing nodelet at end element <" + qName + ">", e);
//                    }
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
