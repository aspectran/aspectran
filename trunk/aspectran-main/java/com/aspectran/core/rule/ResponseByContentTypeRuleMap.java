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
package com.aspectran.core.rule;

import java.util.Iterator;
import java.util.LinkedHashMap;


/**
 * <p>
 * Created: 2009. 03. 09 오후 23:48:09
 * </p>
 */
public class ResponseByContentTypeRuleMap extends LinkedHashMap<String, ResponseByContentTypeRule> implements Iterable<ResponseByContentTypeRule> {

	/** @serial */
	static final long serialVersionUID = -8447972570153335744L;
	
	private ResponseByContentTypeRule responseByContentTypeRule;
	
	public ResponseByContentTypeRule getResponseByContentTypeRule() {
		return responseByContentTypeRule;
	}

	public ResponseByContentTypeRule putResponseByContentTypeRule(ResponseByContentTypeRule responseByContentTypeRule) {
		String exceptionType = responseByContentTypeRule.getExceptionType();
		
		if(exceptionType == null)
			this.responseByContentTypeRule = responseByContentTypeRule;
		
		return put(exceptionType, responseByContentTypeRule);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<ResponseByContentTypeRule> iterator() {
		return this.values().iterator();
	}

}
