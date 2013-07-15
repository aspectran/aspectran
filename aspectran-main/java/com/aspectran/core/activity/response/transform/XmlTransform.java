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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.activity.SuperTranslet;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.activity.response.transform.xml.ContentsInputSource;
import com.aspectran.core.activity.response.transform.xml.ContentsXMLReader;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.builder.AspectranContextConstant;
import com.aspectran.core.rule.TransformRule;

/**
 * <p>
 * Created: 2008. 03. 22 오후 5:51:58
 * </p>
 */
public class XmlTransform extends AbstractTransform implements Responsible {

	public static final String OUTPUT_INDENT = "yes";

	public static final String OUTPUT_METHOD = "xml";
	
	private final Log log = LogFactory.getLog(XmlTransform.class);

	private final boolean traceEnabled = log.isTraceEnabled();

	private final boolean debugEnabled = log.isDebugEnabled();

	/**
	 * Instantiates a new xML transformer.
	 * 
	 * @param transformRule the transform rule
	 */
	public XmlTransform(TransformRule transformRule) {
		super(transformRule);
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#response(org.jhlabs.translets.action.Translet)
	 */
	public void response(AspectranActivity activity) throws TransformResponseException {
		try {
			SuperTranslet translet = (SuperTranslet)activity.getTransletInstance();
			ResponseAdapter responseAdapter = translet.getResponseAdapter();
			
			String contentType = transformRule.getContentType();
			String outputEncoding = transformRule.getCharacterEncoding();

			if(contentType != null)
				responseAdapter.setContentType(contentType);

			if(outputEncoding == null)
				responseAdapter.setCharacterEncoding(outputEncoding);
			
			Writer output = responseAdapter.getWriter();
			ProcessResult processResult = translet.getProcessResult();

			String encoding = transformRule.getCharacterEncoding();
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, OUTPUT_INDENT);
			transformer.setOutputProperty(OutputKeys.METHOD, OUTPUT_METHOD);
			
			if(encoding != null)
				transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
			
			ContentsXMLReader xreader = new ContentsXMLReader();
			ContentsInputSource isource = new ContentsInputSource(processResult);
			transformer.transform(new SAXSource(xreader, isource), new StreamResult(output));

			if(traceEnabled) {
				StringWriter writer = new StringWriter();
				transformer.transform(new SAXSource(xreader, isource), new StreamResult(writer));
				log.trace("XML Source: " + AspectranContextConstant.LINE_SEPARATOR + writer.toString());
			}
			
			if(debugEnabled) {
				log.debug("XML Transform response ok.");
			}
		} catch(Exception e) {
			throw new TransformResponseException("XML Transformation error: " + transformRule, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#getActionList()
	 */
	public ActionList getActionList() {
		return transformRule.getActionList();
	}

}
