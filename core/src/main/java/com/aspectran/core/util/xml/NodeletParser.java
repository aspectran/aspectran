/*
 * Copyright (c) 2008-2017 The Aspectran Project
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
package com.aspectran.core.util.xml;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

/**
 * The NodeletParser is a callback based parser similar to SAX.  The big
 * difference is that rather than having a single callback for all nodes,
 * the NodeletParser has a number of callbacks mapped to
 * various nodes.   The callback is called a Nodelet and it is registered
 * with the NodeletParser against a specific XPath.
 */
public class NodeletParser {

    private final Log log = LogFactory.getLog(NodeletParser.class);

    protected final static Map<String, String> EMPTY_ATTRIBUTES = Collections.unmodifiableMap(new HashMap<>());

    private Map<String, Nodelet> nodeletMap = new HashMap<>();

    private boolean validating;

    private EntityResolver entityResolver;

    private LocationTracker locationTracker;

    /**
     * Registers a nodelet for the specified XPath. Current XPaths supported
     * are:
     * <ul>
     * <li> Element Path - /rootElement/childElement/theElement </li>
     * <li> Element Path - /rootElement/childElement/theElement/end() </li>
     * <!--
     * <li> Text Path - /rootElement/childElement/text() </li>
     * <li> Attribute Path  - /rootElement/childElement/@theAttribute </li>
     * <li> All Elements Named - //theElement </li>
     * -->
     * </ul>
     *
     * @param xpath the xpath
     * @param nodelet the nodelet
     */
    public void addNodelet(String xpath, Nodelet nodelet) {
        nodeletMap.put(xpath, nodelet);
    }

    /**
     * Registers a nodelet for the specified XPath. Current XPaths supported
     * are:
     * <ul>
     * <li> Element Path - /rootElement/childElement/theElement </li>
     * <li> Element Path - /rootElement/childElement/theElement/end() </li>
     * <!--
     * <li> Text Path - /rootElement/childElement/text() </li>
     * <li> Attribute Path  - /rootElement/childElement/@theAttribute </li>
     * <li> All Elements Named - //theElement </li>
     * -->
     * </ul>
     *
     * @param prefix the prefix of xpath
     * @param xpath the xpath
     * @param nodelet the nodelet
     */
    public void addNodelet(String prefix, String xpath, Nodelet nodelet) {
        addNodelet(prefix + xpath, nodelet);
    }

    /**
     * Adds the nodelet.
     *
     * @param xpath the xpath
     * @param nodeletAdder the nodelet adder
     */
    public void addNodelet(String xpath, NodeletAdder nodeletAdder) {
        nodeletAdder.process(xpath, this);
    }

    /**
     * Adds the nodelet.
     *
     * @param prefix the prefix
     * @param xpath the xpath
     * @param nodeletAdder the nodelet adder
     */
    public void addNodelet(String prefix, String xpath, NodeletAdder nodeletAdder) {
        addNodelet(prefix + xpath, nodeletAdder);
    }

    /**
     * Begins parsing from the provided Reader.
     *
     * @param reader the reader
     * @throws NodeletException the nodelet exception
     */
    public void parse(Reader reader) throws NodeletException {
        parse(new InputSource(reader));
    }

    /**
     * Begins parsing from the provided InputStream.
     *
     * @param inputStream the input stream
     * @throws NodeletException the nodelet exception
     */
    public void parse(InputStream inputStream) throws NodeletException {
        parse(new InputSource(inputStream));
    }

    /**
     * Begins parsing from the provided InputSource.
     *
     * @param inputSource the input source
     * @throws NodeletException the nodelet exception
     */
    public void parse(InputSource inputSource) throws NodeletException {
        try {
            Document doc = createDocument(inputSource);
            parse(doc.getLastChild());
        } catch (SAXParseException e) {
            throw new NodeletException("Error parsing XML; " + e);
        } catch (Exception e) {
            throw new NodeletException("Error parsing XML", e);
        }
    }

    /**
     * Begins parsing from the provided Node.
     *
     * @param node the node
     */
    public void parse(Node node) {
        processNodelet(node, "/");
        process(node, new Path());
    }

    /**
     * A recursive method that walkes the DOM tree, registers XPaths and
     * calls Nodelets registered under those XPaths.
     *
     * @param node the node
     * @param path the path
     */
    private void process(Node node, Path path) {
        if (node instanceof Element) {
            // Element
            String elementName = node.getNodeName();
            path.add(elementName);
            processNodelet(node, path.toString());
            //processNodelet(node, "//" + elementName);
            /*
            // Attribute
            NamedNodeMap attributes = node.getAttributes();
            int n = attributes.getLength();
            for (int i = 0; i < n; i++) {
                Node att = attributes.item(i);
                String attrName = att.getNodeName();
                path.add("@" + attrName);
                processNodelet(att, path.toString());
                processNodelet(node, new StringBuilder("//@").append(attrName).toString());
                path.remove();
            }
            */
            // Children
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                process(children.item(i), path);
            }
            path.add("end()");
            processNodelet(node, path.toString());
            path.remove();
            path.remove();
        }/* else if (node instanceof Text) {
            // Text
            path.add("text()");
            processNodelet(node, path.toString());
            processNodelet(node, "//text()");
            path.remove();
        }*/
    }

    private void processNodelet(Node node, String pathString) {
        Nodelet nodelet = nodeletMap.get(pathString);

        if (nodelet != null) {
            try {
                Map<String, String> attrs;
                String text;

                if (!pathString.endsWith("end()")) {
                    attrs = parseAttributes(node);
                    text = getNodeValue(node);

                    if (log.isTraceEnabled()) {
                        StringBuilder sb = new StringBuilder(pathString);
                        if (attrs != null && attrs.size() > 0) {
                            sb.append(" ").append(attrs);
                        }
                        if (text != null && text.length() > 0) {
                            sb.append(" ").append(text);
                        }
                        log.trace(sb.toString());
                    }
                } else {
                    attrs = null;
                    text = null;
                }
                //System.out.println("***===============" + getLocationTracker() + pathString);
                nodelet.process(node, attrs, text);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing XPath '" + pathString + "'. Cause: " + e, e);
            }
        }
    }

    private Map<String, String> parseAttributes(Node node) {
        NamedNodeMap nodeAttr = node.getAttributes();
        if (nodeAttr == null) {
            return EMPTY_ATTRIBUTES;
        }

        Map<String, String> attr = new HashMap<>();
        for (int i = 0; i < nodeAttr.getLength(); i++) {
            Node attribute = nodeAttr.item(i);
            String value = attribute.getNodeValue();
            attr.put(attribute.getNodeName(), value);
        }
        return attr;
    }

    private String getNodeValue(Node node) {
        NodeList children = node.getChildNodes();
        int childrenLen = children.getLength();
        if (childrenLen == 0) {
            String value = node.getNodeValue();
            return (value != null ? value.trim() : null);
        }

        StringBuilder sb = null;
        for (int i = 0; i < childrenLen; i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.CDATA_SECTION_NODE
                    || child.getNodeType() == Node.TEXT_NODE) {
                String data = ((CharacterData)child).getData();
                if (data.length() > 0) {
                    if (sb == null) {
                        sb = new StringBuilder(data);
                    } else {
                        sb.append(data);
                    }
                }
            }
        }
        return (sb != null ? sb.toString() : null);
    }

    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    public void setEntityResolver(EntityResolver resolver) {
        this.entityResolver = resolver;
    }

    /**
     * Creates a JAXP Document from an InputSource.
     *
     * @param inputSource the input source
     * @return the document
     * @throws ParserConfigurationException the parser configuration exception
     * @throws FactoryConfigurationError if a problem with configuration with the Parser Factories exists
     * @throws SAXException the sax exception
     * @throws IOException if an I/O error has occurred
     */
    private Document createDocument(InputSource inputSource)
            throws ParserConfigurationException, FactoryConfigurationError, IOException, SAXException {
        Document doc;
        SAXParser parser;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(validating);
            parser = factory.newSAXParser();

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            docBuilder.setEntityResolver(entityResolver);
            doc = docBuilder.newDocument();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException("Can't create SAX parser / DOM builder.", e);
        }

        Stack<Element> elementStack = new Stack<>();
        StringBuilder textBuffer = new StringBuilder();
        DefaultHandler handler = new DefaultHandler() {
            private Locator locator;

            @Override
            public void setDocumentLocator(Locator locator) {
                this.locator = locator; // Save the locator, so that it can be used later for line tracking when traversing nodes.
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws SAXException {
                addTextIfNeeded();
                Element el = doc.createElement(qName);
                for (int i = 0; i < attributes.getLength(); i++) {
                    el.setAttribute(attributes.getQName(i), attributes.getValue(i));
                }
                if (locationTracker != null) {
                    locationTracker.setLineNumber(this.locator.getLineNumber());
                    locationTracker.setColumnNumber(this.locator.getColumnNumber());
                }
                //System.out.println("***********" + getLocationTracker() + qName);
                elementStack.push(el);
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                addTextIfNeeded();
                Element closedEl = elementStack.pop();
                if (elementStack.isEmpty()) { // Is this the root element?
                    doc.appendChild(closedEl);
                } else {
                    final Element parentEl = elementStack.peek();
                    parentEl.appendChild(closedEl);
                }
            }

            @Override
            public void characters(char ch[], int start, int length) throws SAXException {
                textBuffer.append(ch, start, length);
            }

            // Outputs text accumulated under the current node
            private void addTextIfNeeded() {
                if (textBuffer.length() > 0) {
                    Element el = elementStack.peek();
                    Node textNode = doc.createTextNode(textBuffer.toString());
                    el.appendChild(textNode);
                    textBuffer.delete(0, textBuffer.length());
                }
            }

            @Override
            public void error(SAXParseException e) throws SAXException {
                log.error(e.toString());
                throw e;
            }

            @Override
            public void fatalError(SAXParseException e) throws SAXException {
                log.error(e.toString());
                throw e;
            }

            @Override
            public void warning(SAXParseException e) throws SAXException {
                log.warn(e.toString());
            }
        };

        parser.parse(inputSource, handler);

        return doc;
    }

    /*
     * Creates a JAXP Document from an InputSource.
     *
     * @param inputSource the input source
     * @return the document
     * @throws ParserConfigurationException the parser configuration exception
     * @throws FactoryConfigurationError if a problem with configuration with the Parser Factories exists
     * @throws SAXException the sax exception
     * @throws IOException if an I/O error has occurred
     */
    /*
    private Document createDocument(InputSource inputSource)
            throws ParserConfigurationException, FactoryConfigurationError, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(validating);
        factory.setNamespaceAware(false);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(false);
        factory.setCoalescing(false);
        factory.setExpandEntityReferences(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(entityResolver);
        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void error(SAXParseException e) throws SAXException {
                throw e;
            }

            @Override
            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }

            @Override
            public void warning(SAXParseException e) throws SAXException {
            }
        });

        return builder.parse(inputSource);
    }
    */

    public LocationTracker createLocationTracker() {
        locationTracker = new LocationTracker();
        return locationTracker;
    }

    public LocationTracker getLocationTracker() {
        return locationTracker;
    }

    public class LocationTracker implements Cloneable {

        private int lineNumber;

        private int columnNumber;

        private LocationTracker() {
        }

        private LocationTracker(int lineNumber, int columnNumber) {
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        protected void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        public int getColumnNumber() {
            return columnNumber;
        }

        protected void setColumnNumber(int columnNumber) {
            this.columnNumber = columnNumber;
        }

        @Override
        public LocationTracker clone() {
            return new LocationTracker(lineNumber, columnNumber);
        }

        @Override
        public String toString() {
            return "[lineNumber: " + lineNumber + ", columnNumber: " + columnNumber + "]";
        }

    }

    /**
     * Inner helper class that assists with building XPath paths.
     * <p>
     * Note:  Currently this is a bit slow and could be optimized.
     * </p>
     */
    private static class Path {

        private List<String> nodeList = new ArrayList<>();

        private Path() {
        }

        @SuppressWarnings("unused")
        public Path(String path) {
            StringTokenizer parser = new StringTokenizer(path, "/", false);
            while (parser.hasMoreTokens()) {
                nodeList.add(parser.nextToken());
            }
        }

        public void add(String node) {
            nodeList.add(node);
        }

        public void remove() {
            nodeList.remove(nodeList.size() - 1);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            for (String name : nodeList) {
                sb.append("/");
                sb.append(name);
            }
            return sb.toString();
        }
    }

}