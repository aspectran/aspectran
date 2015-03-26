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

import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.transform.xml.ContentsInputSource;
import com.aspectran.core.activity.response.transform.xml.ContentsXMLReader;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.context.rule.TransformRule;

/**
 * <p>
 * Created: 2008. 03. 22 오후 5:51:58
 * </p>
 */
public class XmlTransform extends TransformResponse implements Response {

	public static final String OUTPUT_METHOD_XML = "xml";
	
	public static final String OUTPUT_INDENT_YES = "yes";

	public static final String INDENT_NUMBER_KEY = "indent-number";

	public static final Integer INDENT_NUMBER_VAL = new Integer(1);

	private final Logger logger = LoggerFactory.getLogger(XmlTransform.class);

	private final boolean traceEnabled = logger.isTraceEnabled();

	private final boolean debugEnabled = logger.isDebugEnabled();
	
	private boolean pretty;

	/**
	 * Instantiates a new xML transformer.
	 * 
	 * @param transformRule the transform rule
	 */
	public XmlTransform(TransformRule transformRule) {
		super(transformRule);
		
		this.pretty = transformRule.isPretty();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#response(com.aspectran.core.activity.CoreActivity)
	 */
	public void response(Activity activity) throws TransformResponseException {
		ResponseAdapter responseAdapter = activity.getResponseAdapter();
		
		if(responseAdapter == null)
			return;

		if(debugEnabled) {
			logger.debug("response {}", transformRule);
		}
		
		try {
			String contentType = transformRule.getContentType();
			String outputEncoding = transformRule.getCharacterEncoding();

			if(contentType != null)
				responseAdapter.setContentType(contentType);

			if(outputEncoding != null)
				responseAdapter.setCharacterEncoding(outputEncoding);
			
			Writer output = responseAdapter.getWriter();
			ProcessResult processResult = activity.getProcessResult();

			String encoding = transformRule.getCharacterEncoding();
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			
			if(pretty)
				transformerFactory.setAttribute(INDENT_NUMBER_KEY, INDENT_NUMBER_VAL);

			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, OUTPUT_METHOD_XML);

			if(pretty)
				transformer.setOutputProperty(OutputKeys.INDENT, OUTPUT_INDENT_YES);
			
			if(encoding != null)
				transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
			
			ContentsXMLReader xreader = new ContentsXMLReader();
			ContentsInputSource isource = new ContentsInputSource(processResult);
			transformer.transform(new SAXSource(xreader, isource), new StreamResult(output));
			
			if(traceEnabled) {
				StringWriter stringWriter = new StringWriter();
				transformer.transform(new SAXSource(xreader, isource), new StreamResult(stringWriter));
				logger.trace("XML output: {}{}", AspectranConstant.LINE_SEPARATOR, stringWriter.toString());
			}
		} catch(Exception e) {
			throw new TransformResponseException(transformRule, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#getActionList()
	 */
	public ActionList getActionList() {
		return transformRule.getActionList();
	}

}
