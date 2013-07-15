package com.aspectran.util.xml;

import java.util.Properties;

import org.w3c.dom.CharacterData;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeletUtils {

	public static boolean getBooleanAttribute(Properties attribs, String name, boolean def) {
		String value = attribs.getProperty(name);
		
		if(value == null) {
			return def;
		} else {
			return Boolean.parseBoolean(value);
		}
	}

	public static int getIntAttribute(Properties attribs, String name, int def) {
		String value = attribs.getProperty(name);
		
		if(value == null) {
			return def;
		} else {
			return Integer.parseInt(value);
		}
	}

	public static Properties parseAttributes(Node n) {
		return parseAttributes(n, null);
	}

	public static Properties parseAttributes(Node n, Properties variables) {
		NamedNodeMap attributeNodes = n.getAttributes();
		
		if(attributeNodes == null)
			return NodeletParser.EMPTY_ATTRIBUTES;

		Properties attributes = new Properties();

		for(int i = 0; i < attributeNodes.getLength(); i++) {
			Node attribute = attributeNodes.item(i);
			String value = attribute.getNodeValue();

			if(variables != null) {
				value = parsePropertyTokens(attribute.getNodeValue(), variables);
			}
			
			attributes.put(attribute.getNodeName(), value);
		}
		
		return attributes;
	}

	public static String parsePropertyTokens(String string, Properties variables) {
		final String OPEN = "${";
		final String CLOSE = "}";

		String newString = string;
		
		if(newString != null && variables != null) {
			int start = newString.indexOf(OPEN);
			int end = newString.indexOf(CLOSE);

			while(start > -1 && end > start) {
				String prepend = newString.substring(0, start);
				String append = newString.substring(end + CLOSE.length());
				String propName = newString.substring(start + OPEN.length(), end);
				String propValue = variables.getProperty(propName);
				
				if(propValue == null) {
					newString = prepend + propName + append;
				} else {
					newString = prepend + propValue + append;
				}
				
				start = newString.indexOf(OPEN);
				end = newString.indexOf(CLOSE);
			}
		}
		
		return newString;
	}
	
	public static String getNodeValue(Node node) {
		NodeList children = node.getChildNodes();
		int childrenLength = children.getLength();
		
		if(childrenLength == 0) {
			String value = node.getNodeValue();
			
			if(value != null)
				return value.trim();

			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < childrenLength; i++) {
			Node child = children.item(i);
			
			if(child.getNodeType() == Node.CDATA_SECTION_NODE ||
					child.getNodeType() == Node.TEXT_NODE) {
				String data = ((CharacterData)child).getData();
				sb.append(data);
			}
		}
		
		return sb.toString().trim();
	}
}
