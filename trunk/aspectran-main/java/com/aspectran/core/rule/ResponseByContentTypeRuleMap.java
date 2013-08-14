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
		
		if(exceptionType == null) {
			this.responseByContentTypeRule = responseByContentTypeRule;
			return responseByContentTypeRule;
		}
		
		return put(exceptionType, responseByContentTypeRule);
	}
	
	public ResponseByContentTypeRule getResponseByContentTypeRule(Exception ex) {
		ResponseByContentTypeRule responseByContentTypeRule = null;
		int deepest = Integer.MAX_VALUE;
		
		for(Iterator<ResponseByContentTypeRule> iter = iterator(); iter.hasNext();) {
			ResponseByContentTypeRule rbctr = iter.next();
			int depth = getMatchedDepth(rbctr.getExceptionType(), ex);

			if(depth >= 0 && depth < deepest) {
				deepest = depth;
				responseByContentTypeRule = rbctr;
			}
		}
		
		if(responseByContentTypeRule == null)
			return this.responseByContentTypeRule;
		
		return responseByContentTypeRule;
	}
	
	private int getMatchedDepth(String exceptionType, Exception ex) {
		return getMatchedDepth(exceptionType, ex.getClass(), 0);
	}

	private int getMatchedDepth(String exceptionType, Class<?> exceptionClass, int depth) {
		if(exceptionClass.getName().indexOf(exceptionType) != -1)
			return depth;

		if(exceptionClass.equals(Throwable.class))
			return -1;
		
		return getMatchedDepth(exceptionType, exceptionClass.getSuperclass(), depth + 1);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<ResponseByContentTypeRule> iterator() {
		return this.values().iterator();
	}

}
