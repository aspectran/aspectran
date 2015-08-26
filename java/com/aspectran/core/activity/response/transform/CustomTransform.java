/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.activity.response.transform;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class CustomTransform.
 * 
 * Created: 2008. 06. 24 오전 4:07:58
 */
public class CustomTransform extends TransformResponse implements Response {

	private final Log log = LogFactory.getLog(CustomTransform.class);

	private boolean debugEnabled = log.isDebugEnabled();

	/**
	 * Instantiates a new custom transformer.
	 * 
	 * @param transformRule the transform rule
	 */
	protected CustomTransform(TransformRule transformRule) {
		super(transformRule);
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#getActionList()
	 */
	public ActionList getActionList() {
		return transformRule.getActionList();
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#response(com.aspectran.core.activity.CoreActivity)
	 */
	public void response(Activity activity) throws TransformResponseException {
		try {
			if(debugEnabled) {
				log.debug("response " + transformRule);
			}
		} catch(Exception e) {
			throw new TransformResponseException(transformRule, e);
		}
	}
}
