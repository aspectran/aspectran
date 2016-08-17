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

import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.transform.xml.ContentsInputSource;
import com.aspectran.core.activity.response.transform.xml.ContentsXMLReader;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class XmlTransform.
 * 
 * Created: 2008. 03. 22 PM 5:51:58
 */
public class XmlTransform extends TransformResponse {

	static final String OUTPUT_METHOD_XML = "xml";

	static final String OUTPUT_INDENT_YES = "yes";

	private static final String INDENT_NUMBER_KEY = "indent-number";

	private static final Integer INDENT_NUMBER_VAL = 1;

	private static final Log log = LogFactory.getLog(XmlTransform.class);

	private static final boolean traceEnabled = log.isTraceEnabled();

	private static final boolean debugEnabled = log.isDebugEnabled();

	private final String characterEncoding;

	private final String contentType;

	private final boolean pretty;

	/**
	 * Instantiates a new XmlTransform.
	 * 
	 * @param transformRule the transform rule
	 */
	public XmlTransform(TransformRule transformRule) {
		super(transformRule);

		this.characterEncoding = transformRule.getCharacterEncoding();
		this.contentType = transformRule.getContentType();
		this.pretty = transformRule.isPretty();
	}

	@Override
	public void response(Activity activity) throws TransformResponseException {
		ResponseAdapter responseAdapter = activity.getResponseAdapter();
		if(responseAdapter == null)
			return;

		if(debugEnabled) {
			log.debug("response " + transformRule);
		}

		try {
			String characterEncoding;
			if(this.characterEncoding != null) {
				characterEncoding = this.characterEncoding;
			} else {
				characterEncoding = activity.determineResponseCharacterEncoding();
			}

			if(characterEncoding != null)
				responseAdapter.setCharacterEncoding(characterEncoding);

			if(contentType != null)
				responseAdapter.setContentType(contentType);

			Writer writer = responseAdapter.getWriter();
			ProcessResult processResult = activity.getProcessResult();

			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			if(pretty)
				transformerFactory.setAttribute(INDENT_NUMBER_KEY, INDENT_NUMBER_VAL);

			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, OUTPUT_METHOD_XML);

			if(characterEncoding != null)
				transformer.setOutputProperty(OutputKeys.ENCODING, characterEncoding);

			if(pretty)
				transformer.setOutputProperty(OutputKeys.INDENT, OUTPUT_INDENT_YES);

			ContentsXMLReader xreader = new ContentsXMLReader();
			ContentsInputSource isource = new ContentsInputSource(processResult);
			transformer.transform(new SAXSource(xreader, isource), new StreamResult(writer));

			if(traceEnabled) {
				StringWriter stringWriter = new StringWriter();
				transformer.transform(new SAXSource(xreader, isource), new StreamResult(stringWriter));
				stringWriter.close(); // forward compatibility
				log.trace(stringWriter.toString());
			}
		} catch(Exception e) {
			throw new TransformResponseException(transformRule, e);
		}
	}

	@Override
	public ActionList getActionList() {
		return transformRule.getActionList();
	}

	@Override
	public Response replicate() {
		TransformRule transformRule = getTransformRule().replicate();
		return new XmlTransform(transformRule);
	}

}
