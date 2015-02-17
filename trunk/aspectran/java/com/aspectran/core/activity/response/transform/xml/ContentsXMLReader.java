/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.core.activity.response.transform.xml;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

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

/**
 * <p>Created: 2008. 05. 26 오후 2:03:15</p>
 */
public class ContentsXMLReader implements XMLReader {

	private static final String CONTENTS_TAG = "contents";

	private static final String CONTENT_TAG = "content";

	private static final String RESULT_TAG = "result";

	private static final String INCLUDE_TAG = "include";

	private static final String ROWS_TAG = "rows";

	private static final String ROW_TAG = "row";

	private static final String EMPTY_TAG = "empty";

	private static final String ID_STRING = "id";

	protected ContentHandler handler;

	private AttributesImpl nullAttrs;

	private AttributesImpl contentAttrs;

	private AttributesImpl resultsAttrs;

	/**
	 * Instantiates a new contents xml reader.
	 */
	public ContentsXMLReader() {
		nullAttrs = new AttributesImpl();
		contentAttrs = new AttributesImpl();
		resultsAttrs = new AttributesImpl();
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
	 * Output string.
	 * 
	 * @param s the s
	 * 
	 * @throws SAXException the SAX exception
	 */
	protected void outputString(String s) throws SAXException {
		handler.characters(s.toCharArray(), 0, s.length());
	}

	/**
	 * Output ignorable whitespace.
	 * 
	 * @param s the s
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

			if(processResult == null || processResult.isEmpty()) {
				handler.startElement(StringUtils.EMPTY, EMPTY_TAG, EMPTY_TAG, nullAttrs);
				handler.endElement(StringUtils.EMPTY, EMPTY_TAG, EMPTY_TAG);
			} else {
				handler.startElement(StringUtils.EMPTY, CONTENTS_TAG, CONTENTS_TAG, nullAttrs);
				parse(processResult);
				handler.endElement(StringUtils.EMPTY, CONTENTS_TAG, CONTENTS_TAG);
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
			if(contentResult.getContentId() == null)
				contentAttrs.clear();
			else
				contentAttrs.addAttribute(null, ID_STRING, ID_STRING, null, contentResult.getContentId());

			handler.startElement(StringUtils.EMPTY, CONTENT_TAG, CONTENT_TAG, contentAttrs);

			for(ActionResult actionResult : contentResult) {
				if(actionResult.getActionId() == null)
					resultsAttrs.clear();
				else
					resultsAttrs.addAttribute(null, ID_STRING, ID_STRING, null, actionResult.getActionId());

				Object resultValue = actionResult.getResultValue();

				if(resultValue instanceof ProcessResult) {
					handler.startElement(StringUtils.EMPTY, INCLUDE_TAG, INCLUDE_TAG, resultsAttrs);
					parse((ProcessResult)resultValue);
					handler.endElement(StringUtils.EMPTY, INCLUDE_TAG, INCLUDE_TAG);
				} else {
					handler.startElement(StringUtils.EMPTY, RESULT_TAG, RESULT_TAG, resultsAttrs);
					parse(resultValue);
					handler.endElement(StringUtils.EMPTY, RESULT_TAG, RESULT_TAG);
				}
			}

			handler.endElement(StringUtils.EMPTY, CONTENT_TAG, CONTENT_TAG);
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
		if(object == null)
			return;

		if(object instanceof ProcessResult) {
			parse((ProcessResult)object);
		} else if(object instanceof String ||
					object instanceof Number ||
					object instanceof Boolean ||
					object instanceof Date) {
			outputString(object.toString());
		} else if(object instanceof Map<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)object;

			for(Map.Entry<String, Object> entry : map.entrySet()) {
				handler.startElement(StringUtils.EMPTY, entry.getKey(), entry.getKey(), nullAttrs);
				parse(entry.getValue());
				handler.endElement(StringUtils.EMPTY, entry.getKey(), entry.getKey());
			}
		} else if(object instanceof Collection<?>) {
			@SuppressWarnings("unchecked")
			Iterator<Object> list = ((Collection<Object>)object).iterator();
			handler.startElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG, nullAttrs);

			while(list.hasNext()) {
				handler.startElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG, nullAttrs);
				parse(list.next());
				handler.endElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG);
			}

			handler.endElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG);
		} else if(object.getClass().isArray()) {
			handler.startElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG, nullAttrs);

			int len = Array.getLength(object);
			for(int i = 0; i < len; i++) {
				handler.startElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG, nullAttrs);
				parse(Array.get(object, i));
				handler.endElement(StringUtils.EMPTY, ROW_TAG, ROW_TAG);
			}

			handler.endElement(StringUtils.EMPTY, ROWS_TAG, ROWS_TAG);
		} else if(object != null) {
			String[] readableProperyNames = BeanUtils.getReadablePropertyNames(object);

			if(readableProperyNames != null && readableProperyNames.length > 0) {
				for(String name : readableProperyNames) {
					Object value = BeanUtils.getObject(object, name);

					if(object.equals(value))
						continue;

					handler.startElement(StringUtils.EMPTY, name, name, nullAttrs);
					parse(value);
					handler.endElement(StringUtils.EMPTY, name, name);
				}
			}
		}
	}
}
