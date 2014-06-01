package com.aspectran.core.activity.aspect.result;

import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.var.rule.AspectAdviceRule;
import com.aspectran.core.var.type.AspectAdviceType;

public class AspectAdviceResult implements Cloneable {
	
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
