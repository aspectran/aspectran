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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.type.TransformType;

/**
 * <p>
 * Created: 2008. 03. 22 오후 5:51:58
 * </p>
 */
public class JSONTransformRule extends TransformRule {
	
	/**
	 * Instantiates a new jSON transform rule.
	 */
	public JSONTransformRule() {
		super.transformType = TransformType.JSON_TRANSFORM;
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.context.rule.TransformRule#toString()
	 */
	@Override
	public String toString() {
		return super.toString();
	}
}
