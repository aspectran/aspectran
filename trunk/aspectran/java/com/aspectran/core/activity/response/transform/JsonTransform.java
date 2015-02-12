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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.activity.response.transform.json.ContentsJsonWriter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.util.json.JsonWriter;

/**
 * <p>
 * Created: 2008. 03. 22 오후 5:51:58
 * </p>
 */
public class JsonTransform extends AbstractTransform implements Responsible {
	
	private final Logger logger = LoggerFactory.getLogger(JsonTransform.class);
	
	private final boolean traceEnabled = logger.isTraceEnabled();
	
	private final boolean debugEnabled = logger.isDebugEnabled();
	
	private boolean pretty;
	
	/**
	 * Instantiates a new JSON transformer.
	 * 
	 * @param transformRule the transform rule
	 */
	public JsonTransform(TransformRule transformRule) {
		super(transformRule);
		
		this.pretty = transformRule.isPretty();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#response(com.aspectran.core.activity.CoreActivity)
	 */
	public void response(CoreActivity activity) throws TransformResponseException {
		if(debugEnabled) {
			logger.debug("response " + transformRule);
		}
		
		try {
			ResponseAdapter responseAdapter = activity.getResponseAdapter();
			
			String contentType = transformRule.getContentType();
			String outputEncoding = transformRule.getCharacterEncoding();

			if(contentType != null)
				responseAdapter.setContentType(contentType);

			if(outputEncoding != null)
				responseAdapter.setCharacterEncoding(outputEncoding);
			
			Writer output = responseAdapter.getWriter();
			ProcessResult processResult = activity.getProcessResult();

			JsonWriter jsonWriter = new ContentsJsonWriter(output, pretty);
			jsonWriter.write(processResult);
			jsonWriter.flush();
			
			if(traceEnabled) {
				StringWriter stringWriter = new StringWriter();
				JsonWriter jsonWriter2 = new ContentsJsonWriter(stringWriter, true);
				jsonWriter2.write(processResult);
				jsonWriter2.flush();
				logger.trace("JSON Source: " + AspectranConstant.LINE_SEPARATOR + stringWriter.toString());
			}
		} catch(Exception e) {
			throw new TransformResponseException("JSON Transformation error: " + transformRule, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#getActionList()
	 */
	public ActionList getActionList() {
		return transformRule.getActionList();
	}

}
