package com.aspectran.base.util.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

/**
 * The NodeletParser is a callback based parser similar to SAX.  The big
 * difference is that rather than having a single callback for all nodes,
 * the NodeletParser has a number of callbacks mapped to
 * various nodes.   The callback is called a Nodelet and it is registered
 * with the NodeletParser against a specific XPath.
 */
public class NodeletParser {
	
	private final Log log = LogFactory.getLog(NodeletParser.class);

	protected final static Properties EMPTY_ATTRIBUTES = new Properties();
	
	private Map<String, Nodelet> nodeletMap = new HashMap<String, Nodelet>();

	private boolean validation;

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
		addNodelet(new StringBuilder(prefix).append(xpath).toString(), nodelet);
	}

	/**
	 * Adds the nodelet.
	 *
	 * @param xpath the xpath
	 * @param nodeletAdder the nodelet adder
	 * @throws Exception the exception
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
	 * @throws Exception the exception
	 */
	public void addNodelet(String prefix, String xpath, NodeletAdder nodeletAdder) {
		addNodelet(new StringBuilder(prefix).append(xpath).toString(), nodeletAdder);
	}
	
	/**
	 * Begins parsing from the provided Reader.
	 */
	public void parse(Reader reader) throws NodeletException {
		try {
			Document doc = createDocument(reader);
			parse(doc.getLastChild());
		} catch(Exception e) {
			throw new NodeletException("Error parsing XML. Cause: " + e, e);
		}
	}

	/**
	 * Begins parsing from the provided InputStream.
	 */
	public void parse(InputStream inputStream) throws NodeletException {
		try {
			Document doc = createDocument(inputStream);
			parse(doc.getLastChild());
		} catch(Exception e) {
			throw new NodeletException("Error parsing XML. Cause: " + e, e);
		}
	}

	/**
	 * Begins parsing from the provided Node.
	 */
	public void parse(Node node) {
		Path path = new Path();
		processNodelet(node, "/");
		process(node, path);
	}

	/**
	 * A recursive method that walkes the DOM tree, registers XPaths and
	 * calls Nodelets registered under those XPaths.
	 */
	private void process(Node node, Path path) {
		if(node instanceof Element) {
			// Element
			String elementName = node.getNodeName();
			path.add(elementName);
			processNodelet(node, path.toString());
			processNodelet(node, new StringBuilder("//").append(elementName).toString());

			// Attribute
			NamedNodeMap attributes = node.getAttributes();
			int n = attributes.getLength();
			for(int i = 0; i < n; i++) {
				Node att = attributes.item(i);
				String attrName = att.getNodeName();
				path.add("@" + attrName);
				processNodelet(att, path.toString());
				processNodelet(node, new StringBuilder("//@").append(attrName).toString());
				path.remove();
			}

			// Children
			NodeList children = node.getChildNodes();
			for(int i = 0; i < children.getLength(); i++) {
				process(children.item(i), path);
			}
			path.add("end()");
			processNodelet(node, path.toString());
			path.remove();
			path.remove();
		} else if(node instanceof Text) {
			// Text
			path.add("text()");
			processNodelet(node, path.toString());
			processNodelet(node, "//text()");
			path.remove();
		}
	}

	private void processNodelet(Node node, String pathString) {
		Nodelet nodelet = nodeletMap.get(pathString);
		
		if(nodelet != null) {
			try {
				Properties attributes;
				String text;

				if(!pathString.endsWith("end()")) {
					attributes = NodeletUtils.parseAttributes(node);
					text = NodeletUtils.getNodeValue(node);

					if(log.isTraceEnabled()) {
						StringBuilder sb = new StringBuilder(pathString);
						
						if(attributes != null && attributes.size() > 0) {
							sb.append(" ").append(attributes);
						}
						
						if(text != null && text.length() > 0) {
							sb.append(" ").append(text);
						}
						
						log.trace(sb.toString());
					}
				} else {
					attributes = EMPTY_ATTRIBUTES;
					text = null;
				}

				nodelet.process(node, attributes, text);
			} catch(Exception e) {
				throw new RuntimeException("Error parsing XPath '" + pathString + "'. Cause: " + e, e);
			}
		}
	}

	/**
	 * Creates a JAXP Document from a reader.
	 */
	private Document createDocument(Reader reader) throws ParserConfigurationException, FactoryConfigurationError,
			SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(validation);

		factory.setNamespaceAware(false);
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(false);
		factory.setCoalescing(false);
		factory.setExpandEntityReferences(true);

		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setEntityResolver(entityResolver);
		builder.setErrorHandler(new ErrorHandler() {
			public void error(SAXParseException exception) throws SAXException {
				throw exception;
			}

			public void fatalError(SAXParseException exception) throws SAXException {
				throw exception;
			}

			public void warning(SAXParseException exception) throws SAXException {
			}
		});

		return builder.parse(new InputSource(reader));
	}

	/**
	 * Creates a JAXP Document from an InputStream.
	 */
	private Document createDocument(InputStream inputStream) throws ParserConfigurationException,
			FactoryConfigurationError, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(validation);

		factory.setNamespaceAware(false);
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(false);
		factory.setCoalescing(false);
		factory.setExpandEntityReferences(true);

		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setEntityResolver(entityResolver);
		builder.setErrorHandler(new ErrorHandler() {
			public void error(SAXParseException exception) throws SAXException {
				throw exception;
			}

			public void fatalError(SAXParseException exception) throws SAXException {
				throw exception;
			}

			public void warning(SAXParseException exception) throws SAXException {
			}
		});

		return builder.parse(new InputSource(inputStream));
	}

	public void setValidation(boolean validation) {
		this.validation = validation;
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

		public Path() {
		}

		@SuppressWarnings("unused")
		public Path(String path) {
			StringTokenizer parser = new StringTokenizer(path, "/", false);

			while(parser.hasMoreTokens()) {
				nodeList.add(parser.nextToken());
			}
		}

		public void add(String node) {
			nodeList.add(node);
		}

		public void remove() {
			nodeList.remove(nodeList.size() - 1);
		}

		public String toString() {
			StringBuilder sb = new StringBuilder(128);

			for(int i = 0; i < nodeList.size(); i++) {
				sb.append("/");
				sb.append(nodeList.get(i));
			}

			return sb.toString();
		}
	}
}