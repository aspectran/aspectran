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
package com.aspectran.core.activity.response.transform;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.transform.xml.ContentsInputSource;
import com.aspectran.core.activity.response.transform.xml.ContentsXMLReader;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.type.ContentType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class XslTransform.
 * 
 * Created: 2008. 03. 22 PM 5:51:58
 */
public class XslTransform extends TransformResponse implements Response {
	
	public static final String OUTPUT_METHOD_XML = "xml";

	public static final String OUTPUT_METHOD_HTML = "html";
	
	public static final String OUTPUT_METHOD_TEXT = "text";
	
	private final Log log = LogFactory.getLog(XslTransform.class);
	
	private boolean traceEnabled = log.isTraceEnabled();
	
	private boolean debugEnabled = log.isDebugEnabled();
	
	private final TemplateRule templateRule;
	
	private long templateLastModifiedTime;
	
	private Templates templates;
	
	private String contentType;
	
	private String outputEncoding;
	
	private boolean templateLoaded;

	/**
	 * Instantiates a new XslTransform.
	 * 
	 * @param transformRule the transform rule
	 */
	protected XslTransform(TransformRule transformRule) {
		super(transformRule);
		this.templateRule = transformRule.getTemplateRule();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Response#response(com.aspectran.core.activity.Activity)
	 */
	public void response(Activity activity) throws TransformResponseException {
		if(debugEnabled) {
			log.debug("response " + transformRule);
		}

		ResponseAdapter responseAdapter = activity.getResponseAdapter();
		
		if(responseAdapter == null)
			return;

		try {
			loadTemplate(activity.getApplicationAdapter());
			
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
				transformer.setOutputProperty(OutputKeys.INDENT, XmlTransform.OUTPUT_INDENT_YES);
				transformer.setOutputProperty(OutputKeys.METHOD, XmlTransform.OUTPUT_METHOD_XML);
				transformer.transform(new SAXSource(xreader, isource), new StreamResult(writer));
				log.trace(writer.toString());
			}
		} catch(Exception e) {
			throw new TransformResponseException(transformRule, e);
		}
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Response#getActionList()
	 */
	public ActionList getActionList() {
		return transformRule.getActionList();
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Response#getTemplateRule()
	 */
	public TemplateRule getTemplateRule() {
		return templateRule;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Response#newDerivedResponse()
	 */
	public Response newDerivedResponse() {
		TransformRule transformRule = getTransformRule();
		
		if(transformRule != null) {
			TransformRule newTransformRule = TransformRule.newDerivedTransformRule(transformRule);
			Response response = new XslTransform(newTransformRule);
			return response;
		}

		return this;
	}
	
	private void loadTemplate(ApplicationAdapter applicationAdapter) throws TransformerConfigurationException, IOException {
		String templateFile = templateRule.getFile();
		String templateResource = templateRule.getResource();
		String templateUrl = templateRule.getUrl();
		boolean noCache = templateRule.isNoCache();

		if(templateFile != null) {
			if(noCache) {
				File file = applicationAdapter.toRealPathAsFile(templateFile);
				templates = createTemplates(file);
				determineOutoutStyle();
			} else {
				File file = applicationAdapter.toRealPathAsFile(templateFile);
				long lastModifiedTime = file.lastModified();
				
				if(lastModifiedTime > templateLastModifiedTime) {
					synchronized(this) {
						lastModifiedTime = file.lastModified();
						if(lastModifiedTime > templateLastModifiedTime) {
							templates = createTemplates(file);
							determineOutoutStyle();
							templateLastModifiedTime = lastModifiedTime;
						}
					}
				}
			}
		} else if(templateResource != null) {
			if(noCache) {
				ClassLoader classLoader = applicationAdapter.getClassLoader();
				File file = new File(classLoader.getResource(templateResource).getFile());
				templates = createTemplates(file);
				determineOutoutStyle();
			} else {
				if(!templateLoaded) {
					synchronized(this) {
						if(!templateLoaded) {
							ClassLoader classLoader = applicationAdapter.getClassLoader();
							File file = new File(classLoader.getResource(templateResource).getFile());
							templates = createTemplates(file);
							determineOutoutStyle();
							templateLoaded = true;
						}
					}
				}
			}
		} else if(templateUrl != null) {
			if(noCache) {
				templates = createTemplates(new URL(templateUrl));
				determineOutoutStyle();
			} else {
				if(!templateLoaded) {
					synchronized(this) {
						if(!templateLoaded) {
							templates = createTemplates(new URL(templateUrl));
							determineOutoutStyle();
							templateLoaded = true;
						}
					}
				}
			}
		}
		
		if(templates == null)
			throw new TransformerConfigurationException("Template file is null.");
	}

	private void determineOutoutStyle() {
    	contentType = transformRule.getContentType();
		outputEncoding = getOutputEncoding(templates);;
		
		if(contentType == null)
			contentType = getContentType(templates);
		
		if(outputEncoding == null)
			outputEncoding = transformRule.getCharacterEncoding();
	}
	
    private Templates createTemplates(File templateFile) throws TransformerConfigurationException {
    	Source source = new StreamSource(templateFile);
    	Templates templates = createTemplates(source);
		return templates;
    }
    
    private Templates createTemplates(URL url) throws TransformerConfigurationException, IOException {
    	Source source = new StreamSource(getTemplateAsStream(url));
    	Templates templates = createTemplates(source);
		return templates;
    }

    private Templates createTemplates(Source source) throws TransformerConfigurationException {
    	TransformerFactory transFactory = TransformerFactory.newInstance();
    	transFactory.setAttribute("generate-translet", Boolean.TRUE);
    	Templates templates = transFactory.newTemplates(source);
    	return templates;
    }

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
    
    private String getOutputEncoding(Templates templates) {
    	Properties outputProperties = templates.getOutputProperties();
    	String outputEncoding = outputProperties.getProperty(OutputKeys.ENCODING);
    	
    	return outputEncoding;
    }

}
