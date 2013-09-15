package com.aspectran.core.activity.aspect.result;

import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.rule.AspectAdviceRule;
import com.aspectran.core.type.AspectAdviceType;

public class AspectAdviceResult {

	private Map<String, Object> beforeAdviceResult;
	
	private Map<String, Object> afterAdviceResult;
	
	private Map<String, Object> finallyAdviceResult;

	public Object getBeforeAdviceResult(String aspectId) {
		if(beforeAdviceResult == null)
			return null;
		
		return beforeAdviceResult.get(aspectId);
	}

	public void putBeforeAdviceResult(String aspectId, Object actionResult) {
		if(beforeAdviceResult == null)
			beforeAdviceResult = new HashMap<String, Object>();
			
		beforeAdviceResult.put(aspectId, actionResult);
	}

	public Object getAfterAdviceResult(String aspectId) {
		if(afterAdviceResult == null)
			return null;
		
		return afterAdviceResult.get(aspectId);
	}

	public void putAfterAdviceResult(String aspectId, Object actionResult) {
		if(afterAdviceResult == null)
			afterAdviceResult = new HashMap<String, Object>();
			
		afterAdviceResult.put(aspectId, actionResult);
	}

	public Object getFinallyAdviceResult(String aspectId) {
		if(finallyAdviceResult == null)
			return null;
		
		return finallyAdviceResult.get(aspectId);
	}

	public void putFinallyAdviceResult(String aspectId, Object actionResult) {
		if(finallyAdviceResult == null)
			finallyAdviceResult = new HashMap<String, Object>();
			
		finallyAdviceResult.put(aspectId, actionResult);
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
