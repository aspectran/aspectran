/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.activity.response.transform;

import java.io.StringWriter;
import java.io.Writer;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.transform.apon.ContentsAponAssembler;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.util.apon.AponSerializer;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AponTransform.
 * 
 * Created: 2008. 03. 22 PM 5:51:58
 */
public class AponTransform extends TransformResponse implements Response {
	
	private final Log log = LogFactory.getLog(AponTransform.class);
	
	private final boolean traceEnabled = log.isTraceEnabled();
	
	private final boolean debugEnabled = log.isDebugEnabled();
	
	private boolean pretty;
	
	/**
	 * Instantiates a new AponTransform.
	 * 
	 * @param transformRule the transform rule
	 */
	public AponTransform(TransformRule transformRule) {
		super(transformRule);
		
		this.pretty = transformRule.isPretty();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Response#response(com.aspectran.core.activity.Activity)
	 */
	public void response(Activity activity) throws TransformResponseException {
		if(debugEnabled) {
			log.debug("response " + transformRule);
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

			Parameters parameters = ContentsAponAssembler.assemble(processResult);
			AponSerializer serializer = new AponSerializer(output, pretty);
			serializer.write(parameters);
			serializer.flush();

			if(traceEnabled) {
				Writer stringWriter = new StringWriter();
				AponSerializer serializer2 = new AponSerializer(output, true);
				serializer2.write(parameters);
				serializer2.flush();
				log.trace(stringWriter.toString());
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
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Response#newDerivedResponse()
	 */
	public Response newDerivedResponse() {
		return this;
	}
	
}
