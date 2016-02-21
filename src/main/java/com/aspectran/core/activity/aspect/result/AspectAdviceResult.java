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
package com.aspectran.core.activity.aspect.result;

import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.type.AspectAdviceType;

/**
 * The Class AspectAdviceResult.
 */
public class AspectAdviceResult {
	
	private Map<String, Object> aspectAdviceBeanMap;

	private Map<String, Object> beforeAdviceResultMap;
	
	private Map<String, Object> afterAdviceResultMap;
	
	private Map<String, Object> finallyAdviceResultMap;

	public Object getAspectAdviceBean(String aspectId) {
		if(aspectAdviceBeanMap == null)
			return null;
		
		return aspectAdviceBeanMap.get(aspectId);
	}
	
	public void putAspectAdviceBean(String aspectId, Object adviceBean) {
		if(aspectAdviceBeanMap == null)
			aspectAdviceBeanMap = new HashMap<String, Object>();
			
		aspectAdviceBeanMap.put(aspectId, adviceBean);
	}

	public Object getBeforeAdviceResult(String aspectId) {
		if(beforeAdviceResultMap == null)
			return null;
		
		return beforeAdviceResultMap.get(aspectId);
	}

	public void putBeforeAdviceResult(String aspectId, Object actionResult) {
		if(beforeAdviceResultMap == null)
			beforeAdviceResultMap = new HashMap<String, Object>();
			
		beforeAdviceResultMap.put(aspectId, actionResult);
	}

	public Object getAfterAdviceResult(String aspectId) {
		if(afterAdviceResultMap == null)
			return null;
		
		return afterAdviceResultMap.get(aspectId);
	}

	public void putAfterAdviceResult(String aspectId, Object actionResult) {
		if(afterAdviceResultMap == null)
			afterAdviceResultMap = new HashMap<String, Object>();
			
		afterAdviceResultMap.put(aspectId, actionResult);
	}

	public Object getFinallyAdviceResult(String aspectId) {
		if(finallyAdviceResultMap == null)
			return null;
		
		return finallyAdviceResultMap.get(aspectId);
	}

	public void putFinallyAdviceResult(String aspectId, Object actionResult) {
		if(finallyAdviceResultMap == null)
			finallyAdviceResultMap = new HashMap<String, Object>();
			
		finallyAdviceResultMap.put(aspectId, actionResult);
	}
	
	public Object getAdviceResult(AspectAdviceType aspectAdviceType, String aspectId) {
		if(aspectAdviceType == AspectAdviceType.BEFORE)
			getBeforeAdviceResult(aspectId);
		else if(aspectAdviceType == AspectAdviceType.AFTER)
			getAfterAdviceResult(aspectId);
		else if(aspectAdviceType == AspectAdviceType.FINALLY)
			getFinallyAdviceResult(aspectId);
		else
			throw new UnsupportedOperationException("Unknown aspect advice type.");
		
		return null;
	}
	
	public void putAdviceResult(AspectAdviceRule aspectAdviceRule, Object adviceActionResult) {
		if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.BEFORE)
			putBeforeAdviceResult(aspectAdviceRule.getAspectId(), adviceActionResult);
		else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AFTER)
			putAfterAdviceResult(aspectAdviceRule.getAspectId(), adviceActionResult);
		else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.FINALLY)
			putFinallyAdviceResult(aspectAdviceRule.getAspectId(), adviceActionResult);
		else
			throw new UnsupportedOperationException("Unknown aspect advice type.");
	}
	
}
