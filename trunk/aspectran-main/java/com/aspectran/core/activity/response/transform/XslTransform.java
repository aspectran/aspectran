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
package com.aspectran.core.activity.response.transform;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.activity.response.transform.xml.ContentsInputSource;
import com.aspectran.core.activity.response.transform.xml.ContentsXMLReader;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.AspectranContextConstant;
import com.aspectran.core.rule.TransformRule;
import com.aspectran.core.type.ContentType;

/**
 * <p>
 * Created: 2008. 03. 22 오후 5:51:58
 * </p>
 */
public class XslTransform extends AbstractTransform implements Responsible {
	
	public static final String OUTPUT_METHOD_XML = "xml";

	public static final String OUTPUT_METHOD_HTML = "html";
	
	public static final String OUTPUT_METHOD_TEXT = "text";
	
	private final Log log = LogFactory.getLog(XslTransform.class);
	
	private boolean traceEnabled = log.isTraceEnabled();
	
	private boolean debugEnabled = log.isDebugEnabled();
	
	private long templateLastModifiedTime;
	
	private File templateFile;
	
	private Templates templates;
	
	private boolean templateNoCache;
	
	private String contentType;
	
	private String outputEncoding;
	
	/**
	 * Instantiates a new xSL transformer.
	 * 
	 * @param transformRule the transform rule
	 */
	protected XslTransform(TransformRule transformRule) {
		super(transformRule);
		this.templateFile = transformRule.getTemplateFile();
		this.templateNoCache = (transformRule.getTemplateNoCache() == Boolean.TRUE);
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.activity.response.Responsible#response(org.jhlabs.translets.activity.Activity)
	 */
	public void response(AspectranActivity activity) throws TransformResponseException {
		try {
			ResponseAdapter responseAdapter = activity.getResponseAdapter();
			
			Templates templates;
			String contentType;
			String outputEncoding;

			if(templateNoCache) {
				templates = createXsltTemplates(templateFile);
				contentType = transformRule.getContentType();
				outputEncoding = getOutputEncoding(templates);;

				if(contentType == null)
					contentType = getContentType(templates);
				
				if(outputEncoding == null)
					outputEncoding = transformRule.getCharacterEncoding();
			} else {
				templates = getTemplates();
				contentType = this.contentType;
				outputEncoding = this.outputEncoding;
			}

			if(contentType != null)
				responseAdapter.setContentType(contentType);

			if(outputEncoding != null)
				responseAdapter.setCharacterEncoding(outputEncoding);
			
			Writer output = responseAdapter.getWriter();
			ProcessResult processResult = activity.getProcessResult();

			ContentsXMLReader xreader = new ContentsXMLReader();
			ContentsInputSource isource = new ContentsInputSource(processResult);
			
			Transformer transformer = templates.newTransformer();
			transformer.transform(new SAXSource(xreader, isource), new StreamResult(output));
		
			if(traceEnabled) {
				StringWriter writer = new StringWriter();
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, XmlTransform.OUTPUT_INDENT);
				transformer.setOutputProperty(OutputKeys.METHOD, XmlTransform.OUTPUT_METHOD);
				transformer.transform(new SAXSource(xreader, isource), new StreamResult(writer));
				log.trace("XML Source: " + AspectranContextConstant.LINE_SEPARATOR + writer.toString());
			}
			
			if(debugEnabled) {
				log.debug("XSL Transform response ok.");
			}
		} catch(Exception e) {
			throw new TransformResponseException("XSL Transformation error: " + transformRule, e);
		}
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#getActionList()
	 */
	public ActionList getActionList() {
		return transformRule.getActionList();
	}
	
	/**
	 * Gets the templates.
	 * 
	 * @return the templates
	 * 
	 * @throws TransformerConfigurationException the transformer configuration exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Templates getTemplates() throws TransformerConfigurationException, IOException {
		if(templateFile == null)
			throw new TransformerConfigurationException("Template file is null.");
		
		long lastModifiedTime = templateFile.lastModified();
		
		if(lastModifiedTime > templateLastModifiedTime) {
			synchronized(this) {
				lastModifiedTime = templateFile.lastModified();
				
				if(lastModifiedTime > templateLastModifiedTime) {
					templates = createXsltTemplates(templateFile);
					templateLastModifiedTime = lastModifiedTime;
					
					String contentType = transformRule.getContentType();
					String outputEncoding = getOutputEncoding(templates);;

					if(contentType == null)
						contentType = getContentType(templates);
					
					if(outputEncoding == null)
						outputEncoding = transformRule.getCharacterEncoding();
				}
			}
		}

		return templates;
	}
	
    /**
     * Creates the xslt templates.
     * 
     * @param templateFile the template file
     * 
     * @return the templates
     * 
     * @throws TransformerConfigurationException the transformer configuration exception
     */
    private Templates createXsltTemplates(File templateFile) throws TransformerConfigurationException {
    	TransformerFactory transFactory = TransformerFactory.newInstance();
    	transFactory.setAttribute("generate-translet", Boolean.TRUE);
    	return transFactory.newTemplates(new StreamSource(templateFile));
    }
    
    /**
     * Gets the content type.
     *
     * @param templates the templates
     * @return the content type
     */
    private String getContentType(Templates templates) {
		Properties outputProperties = templates.getOutputProperties();
		String outputMethod = outputProperties.getProperty(OutputKeys.METHOD);
		String contentType = null;

		if(outputMethod != null) {
			if(outputMethod.equalsIgnoreCase(XslTransform.OUTPUT_METHOD_XML))
				contentType = ContentType.TEXT_XML.toString();
			else if(outputMethod.equalsIgnoreCase(XslTransform.OUTPUT_METHOD_HTML))
				contentType = ContentType.TEXT_HTML.toString();
			else if(outputMethod.equalsIgnoreCase(XslTransform.OUTPUT_METHOD_TEXT))
				contentType = ContentType.TEXT_PLAIN.toString();
		}
		
		return contentType;
    }
    
    /**
     * Gets the output encoding.
     *
     * @param templates the templates
     * @return the output encoding
     */
    private String getOutputEncoding(Templates templates) {
    	Properties outputProperties = templates.getOutputProperties();
    	String outputEncoding = outputProperties.getProperty(OutputKeys.ENCODING);
    	
    	return outputEncoding;
    }

//	/**
//	 * Gets the output properties.
//	 * 
//	 * @return the output properties
//	 * 
//	 * @throws ResponseException the response exception
//	 */
//	public Properties getOutputProperties() throws ResponseException {
//		try {
//			Templates templates = getTemplates();
//			return templates.getOutputProperties();
//		} catch(TransformerConfigurationException e) {
//			throw new ResponseException("XSL Transformation error: " + e.getMessageAndLocation(), e);
//		} catch(IOException e) {
//			throw new ResponseException("XSL Transformation error: " + e.getMessage(), e);
//		}
//	}
}
