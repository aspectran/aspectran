/*
 * Copyright 2008-2017 Juho Jeong
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The NodeletParser is a callback based parser similar to SAX.  The big
 * difference is that rather than having a single callback for all nodes,
 * the NodeletParser has a number of callbacks mapped to
 * various nodes.   The callback is called a Nodelet and it is registered
 * with the NodeletParser against a specific XPath.
 */
public class NodeletParser {

    private final Log log = LogFactory.getLog(NodeletParser.class);

    protected final static Map<String, String> EMPTY_ATTRIBUTES = new HashMap<String, String>();

    private Map<String, Nodelet> nodeletMap = new HashMap<String, Nodelet>();

    private boolean validating;

    private EntityResolver entityResolver;

    /**
     * Registers a nodelet for the specified XPath. Current XPaths supported
     * are:
     * <ul>
     * <li> Text Path - /rootElement/childElement/text()
     * <li> Attribute Path  - /rootElement/childElement/@theAttribute
     * <li> Element Path - /rootElement/childElement/theElement
     * <li> All Elements Named - //theElement
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
     * <li> Text Path - /rootElement/childElement/text()
     * <li> Attribute Path  - /rootElement/childElement/@theAttribute
     * <li> Element Path - /rootElement/childElement/theElement
     * <li> All Elements Named - //theElement
     * </ul>
     *
     * @param prefix the prefix xpath
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
        try {
            Document doc = createDocument(reader);
            parse(doc.getLastChild());
        } catch (Exception e) {
            throw new NodeletException("Error parsing XML.", e);
        }
    }

    /**
     * Begins parsing from the provided InputStream.
     *
     * @param inputStream the input stream
     * @throws NodeletException the nodelet exception
     */
    public void parse(InputStream inputStream) throws NodeletException {
        try {
            Document doc = createDocument(inputStream);
            parse(doc.getLastChild());
        } catch (Exception e) {
            throw new NodeletException("Error parsing XML.", e);
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
            processNodelet(node, "//" + elementName);
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
        } else if (node instanceof Text) {
            // Text
            path.add("text()");
            processNodelet(node, path.toString());
            /*
            processNodelet(node, "//text()");
            */
            path.remove();
        }
    }

    private void processNodelet(Node node, String pathString) {
        Nodelet nodelet = nodeletMap.get(pathString);

        if (nodelet != null) {
            try {
                Map<String, String> attributes;
                String text;

                if (!pathString.endsWith("end()")) {
                    attributes = parseAttributes(node);
                    text = getNodeValue(node);

                    if (log.isTraceEnabled()) {
                        StringBuilder sb = new StringBuilder(pathString);
                        if (attributes != null && attributes.size() > 0) {
                            sb.append(" ").append(attributes);
                        }
                        if (text != null && text.length() > 0) {
                            sb.append(" ").append(text);
                        }
                        log.trace(sb.toString());
                    }
                } else {
                    attributes = null;
                    text = null;
                }

                nodelet.process(node, attributes, text);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing XPath '" + pathString + "'. Cause: " + e, e);
            }
        }
    }

    private Map<String, String> parseAttributes(Node node) {
        NamedNodeMap attributeNodes = node.getAttributes();

        if (attributeNodes == null) {
            return EMPTY_ATTRIBUTES;
        }

        Map<String, String> attributes = new HashMap<>();

        for (int i = 0; i < attributeNodes.getLength(); i++) {
            Node attribute = attributeNodes.item(i);
            String value = attribute.getNodeValue();

            attributes.put(attribute.getNodeName(), value);
        }

        return attributes;
    }

    private String getNodeValue(Node node) {
        NodeList children = node.getChildNodes();
        int childrenLength = children.getLength();

        if (childrenLength == 0) {
            String value = node.getNodeValue();
            return (value != null ? value.trim() : null);
        }

        StringBuilder sb = null;

        for (int i = 0; i < childrenLength; i++) {
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

    /**
     * Creates a JAXP Document from a reader.
     *
     * @param reader the reader
     * @return the document
     * @throws ParserConfigurationException the parser configuration exception
     * @throws FactoryConfigurationError the factory configuration error
     * @throws SAXException the sax exception
     * @throws IOException if an I/O error has occurred
     */
    private Document createDocument(Reader reader) throws ParserConfigurationException, FactoryConfigurationError,
            SAXException, IOException {
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
            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void warning(SAXParseException exception) throws SAXException {
            }
        });

        return builder.parse(new InputSource(reader));
    }

    /**
     * Creates a JAXP Document from an InputStream.
     *
     * @param inputStream the input stream
     * @return the document
     * @throws ParserConfigurationException the parser configuration exception
     * @throws FactoryConfigurationError the factory configuration error
     * @throws SAXException the sax exception
     * @throws IOException if an I/O error has occurred
     */
    private Document createDocument(InputStream inputStream) throws ParserConfigurationException,
            FactoryConfigurationError, SAXException, IOException {
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
            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void warning(SAXParseException exception) throws SAXException {
            }
        });

        return builder.parse(new InputSource(inputStream));
    }

    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    public void setEntityResolver(EntityResolver resolver) {
        this.entityResolver = resolver;
    }

    /**
     * Inner helper class that assists with building XPath paths.
     * <p/>
     * Note:  Currently this is a bit slow and could be optimized.
     */
    private static class Path {

        private List<String> nodeList = new ArrayList<String>();

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