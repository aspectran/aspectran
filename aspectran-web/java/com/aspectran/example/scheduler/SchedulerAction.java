package com.aspectran.example.scheduler;

import java.util.LinkedHashMap;
import java.util.Map;

import com.aspectran.web.activity.WebTranslet;

public class SchedulerAction {

	public String countTo10(WebTranslet translet) {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 1; i <= 10; i++) {
			sb.append(i).append("\n");
		}
		
		return sb.toString();
	}
	
	public int[] countTo10AsArray(WebTranslet translet) {
		int arr[] = new int[10];
		
		for(int i = 0; i < arr.length; i++) {
			arr[i] = i + 1;
		}
		
		return arr;
	}
	
	public Object applyTheAdvice(WebTranslet translet) {
		int i = 1 / 0;
		System.out.println("================================");
		System.out.println(i);
		Object beforeAdviceResult = translet.getBeforeAdviceResult("helloAdvice");
		Object afterAdviceResult = translet.getAfterAdviceResult("helloAdvice");
		
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		result.put("beforeAdviceResult", beforeAdviceResult);
		result.put("countTo10", countTo10AsArray(translet));
		result.put("sampleBean", translet.getBean("sampleBean"));
		result.put("afterAdviceResult", afterAdviceResult);
		
		return result;
	}
	
}
