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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.AspectranConstants;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.io.Writer;

/**
 * The Class TextTransform.
 * 
 * <p>Created: 2008. 03. 22 오후 5:51:58</p>
 */
public class TextTransform extends TransformResponse implements Response {

	private final Log log = LogFactory.getLog(TextTransform.class);

	private final boolean debugEnabled = log.isDebugEnabled();

	private final TemplateRule templateRule;

	private final String contentType;
	
	private final String outputEncoding;

	/**
	 * Instantiates a new TextTransform.
	 * 
	 * @param transformRule the transform rule
	 */
	public TextTransform(TransformRule transformRule) {
		super(transformRule);
		this.templateRule = transformRule.getTemplateRule();
		this.contentType = transformRule.getContentType();
		this.outputEncoding = transformRule.getCharacterEncoding();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#response(com.aspectran.core.activity.CoreActivity)
	 */
	public void response(Activity activity) throws TransformResponseException {
		if(debugEnabled) {
			log.debug("response " + transformRule);
		}
		
		ResponseAdapter responseAdapter = activity.getResponseAdapter();
		
		if(responseAdapter == null)
			return;

		try {
			if(contentType != null)
				responseAdapter.setContentType(contentType);

			if(outputEncoding != null)
				responseAdapter.setCharacterEncoding(outputEncoding);

			Writer writer = responseAdapter.getWriter();

			if(templateRule != null) {
				activity.getTemplateProcessor().process(templateRule, activity, writer);
			} else {
				ProcessResult processResult = activity.getProcessResult();
				
				if(processResult != null) {
					int chunks = 0;
					
					for(ContentResult contentResult : processResult) {
						for(ActionResult actionResult : contentResult) {
							Object resultValue = actionResult.getResultValue();
							if(resultValue != null) {
								if(chunks++ > 0)
									writer.write(AspectranConstants.LINE_SEPARATOR);

								writer.write(resultValue.toString());
							}
						}
					}

					writer.flush();
				}
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
			Response response = new TextTransform(newTransformRule);
			return response;
		}
		
		return this;
	}

}
