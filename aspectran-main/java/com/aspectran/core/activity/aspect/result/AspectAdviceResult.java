package com.aspectran.core.activity.aspect.result;

import java.util.HashMap;
import java.util.Map;

public class AspectAdviceResult {

	private Map<String, Object> beforeAdviceActionResult;
	
	private Map<String, Object> afterAdviceActionResult;
	
	private Map<String, Object> finallyAdviceActionResult;

	public Object getBeforeAdviceActionResult(String aspectId) {
		if(beforeAdviceActionResult == null)
			return null;
		
		return beforeAdviceActionResult.get(aspectId);
	}

	public void putBeforeAdviceActionResult(String aspectId, Object actionResult) {
		if(beforeAdviceActionResult == null)
			beforeAdviceActionResult = new HashMap<String, Object>();
			
		beforeAdviceActionResult.put(aspectId, actionResult);
	}

	public Object getAfterAdviceActionResult(String aspectId) {
		if(afterAdviceActionResult == null)
			return null;
		
		return afterAdviceActionResult.get(aspectId);
	}

	public void putAfterAdviceActionResult(String aspectId, Object actionResult) {
		if(afterAdviceActionResult == null)
			afterAdviceActionResult = new HashMap<String, Object>();
			
		afterAdviceActionResult.put(aspectId, actionResult);
	}

	public Object getFinallyAdviceActionResult(String aspectId) {
		if(finallyAdviceActionResult == null)
			return null;
		
		return finallyAdviceActionResult.get(aspectId);
	}

	public void putFinallyAdviceActionResult(String aspectId, Object actionResult) {
		if(finallyAdviceActionResult == null)
			finallyAdviceActionResult = new HashMap<String, Object>();
			
		finallyAdviceActionResult.put(aspectId, actionResult);
	}
	
}
