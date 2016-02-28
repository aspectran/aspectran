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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.transform.apon.ContentsAponAssembler;
import com.aspectran.core.adapter.ResponseAdapter;
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
	
	private static final Log log = LogFactory.getLog(AponTransform.class);
	
	private static final boolean traceEnabled = log.isTraceEnabled();
	
	private static final boolean debugEnabled = log.isDebugEnabled();

	private final String characterEncoding;

	private final String contentType;

	private boolean pretty;
	
	/**
	 * Instantiates a new AponTransform.
	 * 
	 * @param transformRule the transform rule
	 */
	public AponTransform(TransformRule transformRule) {
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
			if(this.characterEncoding != null) {
				responseAdapter.setCharacterEncoding(this.characterEncoding);
			} else {
				String characterEncoding = activity.determineResponseCharacterEncoding();
				if(characterEncoding != null)
					responseAdapter.setCharacterEncoding(characterEncoding);
			}

			if(contentType != null) {
				responseAdapter.setContentType(contentType);
			}

			Writer writer = responseAdapter.getWriter();
			ProcessResult processResult = activity.getProcessResult();

			Parameters parameters = ContentsAponAssembler.assemble(processResult);
			AponSerializer serializer = new AponSerializer(writer, pretty);
			serializer.write(parameters);
			serializer.flush();

			if(traceEnabled) {
				Writer stringWriter = new StringWriter();
				AponSerializer serializer2 = new AponSerializer(stringWriter, true);
				serializer2.write(parameters);
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
		Response response = new AponTransform(transformRule);
		return response;
	}
	
}
