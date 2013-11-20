/*
 *  Copyright (c) 2010 Jeong Ju Ho, All rights reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.rule.TransformRule;

/**
 * <p>
 * Created: 2008. 06. 24 오전 4:07:58
 * </p>
 */
public class CustomTransform extends AbstractTransform implements Responsible {

	private final Logger logger = LoggerFactory.getLogger(CustomTransform.class);

	private boolean debugEnabled = logger.isDebugEnabled();

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
	public void response(CoreActivity activity) throws TransformResponseException {
		try {
			if(debugEnabled) {
				logger.debug("response " + transformRule);
			}
		} catch(Exception e) {
			throw new TransformResponseException("Custom Transformation error: " + transformRule, e);
		}
	}
}
