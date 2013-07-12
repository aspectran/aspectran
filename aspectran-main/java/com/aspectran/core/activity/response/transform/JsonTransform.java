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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.base.adapter.ResponseAdapter;
import com.aspectran.base.context.ActivityContextConstant;
import com.aspectran.base.rule.TransformRule;
import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.activity.response.transform.json.ContentsJSONWriter;
import com.aspectran.core.translet.Translet;

/**
 * <p>
 * Created: 2008. 03. 22 오후 5:51:58
 * </p>
 */
public class JsonTransform extends AbstractTransform implements Responsible {
	
	private final Log log = LogFactory.getLog(JsonTransform.class);
	
	private final boolean traceEnabled = log.isTraceEnabled();
	
	private final boolean debugEnabled = log.isDebugEnabled();
	
	/**
	 * Instantiates a new jSON transformer.
	 * 
	 * @param transformRule the transform rule
	 */
	public JsonTransform(TransformRule transformRule) {
		super(transformRule);
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#response(org.jhlabs.translets.action.Translet)
	 */
	public void response(Activity activity) throws TransformResponseException {
		try {
			Translet translet = activity.getActivityTranslet();
			ResponseAdapter responseAdapter = translet.getResponseAdapter();
			
			String contentType = transformRule.getContentType();
			String outputEncoding = transformRule.getCharacterEncoding();

			if(contentType != null)
				responseAdapter.setContentType(contentType);

			if(outputEncoding == null)
				responseAdapter.setCharacterEncoding(outputEncoding);
			
			Writer output = responseAdapter.getWriter();
			ProcessResult processResult = translet.getProcessResult();

			boolean prettyWrite = (traceEnabled || debugEnabled);
			ContentsJSONWriter contentsJSONWriter = new ContentsJSONWriter(output, prettyWrite);
			contentsJSONWriter.write(processResult);
			
			if(traceEnabled) {
				StringWriter writer = new StringWriter();
				ContentsJSONWriter contentsJSONWriter2 = new ContentsJSONWriter(writer, true);
				contentsJSONWriter2.write(processResult);
				log.trace("JSON Source: " + ActivityContextConstant.LINE_SEPARATOR + writer.toString());
			}
			
			if(debugEnabled) {
				log.debug("JSON Transform response ok.");
			}
		} catch(Exception e) {
			throw new TransformResponseException("JSON Transformation error: " + transformRule, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#getActionList()
	 */
	public ActionList getActionList() {
		return transformRule.getActionList();
	}

}
