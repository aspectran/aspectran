/**
 * Copyright 2008-2016 Juho Jeong
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
package com.aspectran.core.activity.response.transform.xml;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.util.BeanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.Parameter;
import com.aspectran.core.util.apon.ParameterValue;
import com.aspectran.core.util.apon.Parameters;

/**
 * Converts a ProcessResult object to a XML string.
 * 
 * <p>Created: 2008. 05. 26 PM 2:03:15</p>
 *
 * @author Juho Jeong
 */
public class ContentsXMLReader implements XMLReader {

	private static final String CONTENTS_TAG = "contents";

	private static final String CONTENT_TAG = "content";

	private static final String RESULT_TAG = "result";

	private static final String ROWS_TAG = "rows";

	private static final String ROW_TAG = "row";

	private static final String EMPTY_TAG = "empty";
	
	private static final Attributes NULL_ATTRS = new AttributesImpl();

	protected ContentHandler handler;

	/**
	 * Instantiates a new ContentsXMLReader.
	 */
	public ContentsXMLReader() {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.XMLReader#setContentHandler(org.xml.sax.ContentHandler)
	 */
	public void setContentHandler(ContentHandler handler) {
		this.handler = handler;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.XMLReader#getContentHandler()
	 */
	public ContentHandler getContentHandler() {
		return handler;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.XMLReader#setErrorHandler(org.xml.sax.ErrorHandler)
	 */
	public void setErrorHandler(ErrorHandler errorhandler) {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.XMLReader#getErrorHandler()
	 */
	public ErrorHandler getErrorHandler() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.XMLReader#parse(java.lang.String)
	 */
	public void parse(String s) throws IOException, SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.XMLReader#getDTDHandler()
	 */
	public DTDHandler getDTDHandler() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.XMLReader#getEntityResolver()
	 */
	public EntityResolver getEntityResolver() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.XMLReader#setEntityResolver(org.xml.sax.EntityResolver)
	 */
	public void setEntityResolver(EntityResolver entityresolver) {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.XMLReader#setDTDHandler(org.xml.sax.DTDHandler)
	 */
	public void setDTDHandler(DTDHandler dtdhandler) {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.XMLReader#getProperty(java.lang.String)
	 */
	public Object getProperty(String name) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.XMLReader#setProperty(java.lang.String, java.lang.Object)
	 */
	public void setProperty(String s, Object obj) {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.XMLReader#setFeature(java.lang.String, boolean)
	 */
	public void setFeature(String s, boolean flag) {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.XMLReader#getFeature(java.lang.String)
	 */
	public boolean getFeature(String name) {
		return false;
	}

	/**
	 * Output a string.
	 * 
	 * @param s the input string
	 * 
	 * @throws SAXException the SAX exception
	 */
	protected void outputString(String s) throws SAXException {
		handler.characters(s.toCharArray(), 0, s.length());
	}

	/**
	 * Output a ignorable whitespace string.
	 * 
	 * @param s the whitespace string
	 * 
	 * @throws SAXException the SAX exception
	 */
	protected void outputIgnorableWhitespace(String s) throws SAXException {
		handler.characters(s.toCharArray(), 0, s.length());
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
	 */
	public void parse(InputSource is) throws IOException, SAXException {
		if(handler == null)
			throw new SAXException("No XML ContentHandler");

		try {
			ContentsInputSource cis = (ContentsInputSource)is;
			ProcessResult processResult = cis.getProcessResult();

			handler.startDocument();

			if(processResult != null && !processResult.isEmpty()) {
				String contentsName = processResult.getName();
				
				if(!processResult.isOmittable()) {
					if(contentsName != null)
						handler.startElement(StringUtils.EMPTY, contentsName, contentsName, NULL_ATTRS);
					else
						handler.startElement(StringUtils.EMPTY, CONTENTS_TAG, CONTENTS_TAG, NULL_ATTRS);
				}
				
				parse(processResult);

				if(!processResult.isOmittable()) {
					if(contentsName != null)
						handler.endElement(StringUtils.EMPTY, contentsName, contentsName);
					else
						handler.endElement(StringUtils.EMPTY, CONTENTS_TAG, CONTENTS_TAG);
				}
			} else {
				handler.startElement(StringUtils.EMPTY, EMPTY_TAG, EMPTY_TAG, NULL_ATTRS);
				handler.endElement(StringUtils.EMPTY, EMPTY_TAG, EMPTY_TAG);
			}

			handler.endDocument();
		} catch(InvocationTargetException e) {
			throw new SAXException("Cannot parse process-result. Cause: " + e.toString());
		} catch(IOException e) {
			throw e;
		} catch(SAXException e) {
			throw e;
		}
	}

	/**
	 * Parses the result of processing.
	 * 
	 * @param processResult the content results list
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws SAXException the SAX exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	private void parse(ProcessResult processResult) throws IOException, SAXException, InvocationTargetException {
		for(ContentResult contentResult : processResult) {
			String contentName = contentResult.getName();
			
			if(!contentResult.isOmittable()) {
				if(contentName != null)
					handler.startElement(StringUtils.EMPTY, contentName, contentName, NULL_ATTRS);
				else
					handler.startElement(StringUtils.EMPTY, CONTENT_TAG, CONTENT_TAG, NULL_ATTRS);
			}

			for(ActionResult actionResult : contentResult) {
				String actionId = actionResult.getActionId();
				Object resultValue = actionResult.getResultValue();

				if(resultValue instanceof ProcessResult) {
					parse((ProcessResult)resultValue);
				} else {
					if(actionId != null)
						handler.startElement(StringUtils.EMPTY, actionId, actionId, NULL_ATTRS);
					else
						handler.startElement(StringUtils.EMPTY, RESULT_TAG, RESULT_TAG, NULL_ATTRS);

					parse(resultValue);

					if(actionId != null)
						handler.endElement(StringUtils.EMPTY, actionId, actionId);
					else
						handler.endElement(StringUtils.EMPTY, RESULT_TAG, RESULT_TAG);
				}
			}

			if(!contentResult.isOmittable()) {
				if(contentResult.getName() != null)
					handler.endElement(StringUtils.EMPTY, contentName, contentName);
				else
					handler.endElement(StringUtils.EMPTY, CONTENT_TAG, CONTENT_TAG);
			}
		}
	}

	/**
	 * Parses the object.
	 * 
	 * @param object the object
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws SAXException the SAX exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	private void parse(Object object) throws IOException, SAXException, InvocationTargetException {
		if(object == null) {
			return;
		} else if(object instanceof ProcessResult) {
			parse((ProcessResult)object);
		} else if(object instanceof String ||
					object instanceof Number ||
					object instanceof Boolean ||
					object instanceof Date) {
			outputString(object.toString());
		} else if(object instanceof Parameters) {
			Map<String, ParameterValue> params = ((Parameters)object).getParameterValueMap();
			for(Parameter p: params.values()) {
				String name = p.getName();
				Object value = p.getValue();
				checkCircularReference(object, value);

				handler.startElement(StringUtils.EMPTY, name, name, NULL_ATTRS);
				parse(value);
				handler.endElement(StringUtils.EMPTY, name, name);
			}
		} else if(object instanceof Map<?, ?>) {
			for(Map.Entry<Object, Object> entry : ((Map<Object, Object>)object).entrySet()) {
				String name = entry.getKey().toString();
				Object value = entry.getValue();
				checkCircularReference(object, value);

				handler.startElement(StringUtils.EMPTY, name, name, NULL_ATTRS);
				parse(value);
				handler.endElement(StringUtils.EMPTY, name, name);
			}
		} else if(object instanceof Collection<?>) {
			handler.startElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG, NULL_ATTRS);

			Iterator<Object> list = ((Collection<Object>)object).iterator();
			while(list.hasNext()) {
				Object value = list.next();
				checkCircularReference(object, value);

				handler.startElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG, NULL_ATTRS);
				parse(value);
				handler.endElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG);
			}

			handler.endElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG);
		} else if(object.getClass().isArray()) {
			handler.startElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG, NULL_ATTRS);

			int len = Array.getLength(object);
			for(int i = 0; i < len; i++) {
				Object value = Array.get(object, i);
				checkCircularReference(object, value);

				handler.startElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG, NULL_ATTRS);
				parse(value);
				handler.endElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG);
			}

			handler.endElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG);
		} else {
			String[] readableProperyNames = BeanUtils.getReadablePropertyNames(object);
			if(readableProperyNames != null && readableProperyNames.length > 0) {
				for(String name : readableProperyNames) {
					Object value = BeanUtils.getObject(object, name);
					checkCircularReference(object, value);

					handler.startElement(StringUtils.EMPTY, name, name, NULL_ATTRS);
					parse(value);
					handler.endElement(StringUtils.EMPTY, name, name);
				}
			}
		}
	}

	private void checkCircularReference(Object wrapper, Object member) {
		if(wrapper.equals(member)) {
			throw new IllegalArgumentException("XML Serialization Failure: A circular reference was detected while converting a member object [" + member + "] in [" + wrapper + "]");
		}
	}

}
