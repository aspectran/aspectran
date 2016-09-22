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

import java.io.Writer;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class TextTransform.
 * 
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class TextTransform extends TransformResponse {

	private static final Log log = LogFactory.getLog(TextTransform.class);

	private final String templateId;

	private final TemplateRule templateRule;

	private final String characterEncoding;

	private final String contentType;
	
	/**
	 * Instantiates a new TextTransform.
	 * 
	 * @param transformRule the transform rule
	 */
	public TextTransform(TransformRule transformRule) {
		super(transformRule);

		this.templateId = transformRule.getTemplateId();
		this.templateRule = transformRule.getTemplateRule();
		this.characterEncoding = transformRule.getCharacterEncoding();
		this.contentType = transformRule.getContentType();
	}

	@Override
	public void response(Activity activity) throws TransformResponseException {
		ResponseAdapter responseAdapter = activity.getResponseAdapter();
		if (responseAdapter == null) {
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("response " + transformRule);
		}

		try {
			if (this.characterEncoding != null) {
				responseAdapter.setCharacterEncoding(this.characterEncoding);
			} else {
				String characterEncoding = activity.getTranslet().getResponseCharacterEncoding();
				if (characterEncoding != null) {
					responseAdapter.setCharacterEncoding(characterEncoding);
				}
			}

			if (contentType != null) {
				responseAdapter.setContentType(contentType);
			}

			Writer writer = responseAdapter.getWriter();

			if (templateId != null) {
				activity.getTemplateProcessor().process(templateId, activity);
			} else if (templateRule != null) {
				activity.getTemplateProcessor().process(templateRule, activity);
			} else {
				ProcessResult processResult = activity.getProcessResult();
				if (processResult != null) {
					int chunks = 0;
					for (ContentResult contentResult : processResult) {
						for (ActionResult actionResult : contentResult) {
							Object resultValue = actionResult.getResultValue();
							if (resultValue != null) {
								if (chunks++ > 0) {
									writer.write(ActivityContext.LINE_SEPARATOR);
								}
								writer.write(resultValue.toString());
							}
						}
					}
				}
				writer.flush();
			}
		} catch (Exception e) {
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
		return new TextTransform(transformRule);
	}

}
