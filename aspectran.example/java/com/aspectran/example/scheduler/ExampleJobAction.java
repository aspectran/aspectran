package com.aspectran.example.scheduler;

import java.util.LinkedHashMap;
import java.util.Map;

import com.aspectran.scheduler.activity.JobTranslet;

public class ExampleJobAction {

	public String countTo10(JobTranslet translet) {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 1; i <= 10; i++) {
			sb.append(i).append("\n");
		}
		
		return sb.toString();
	}
	
	public int[] countTo10AsArray(JobTranslet translet) {
		int arr[] = new int[10];
		
		for(int i = 0; i < arr.length; i++) {
			arr[i] = i + 1;
		}
		
		return arr;
	}
	
	public Map<String, Object> applyTheAdvice(JobTranslet translet) {
		String beforeAdviceResult = translet.getBeforeAdviceResult("helloAdvice");
		String afterAdviceResult = translet.getAfterAdviceResult("helloAdvice");
		
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		result.put("beforeAdviceResult", beforeAdviceResult);
		result.put("countTo10", countTo10AsArray(translet));
		result.put("sampleBean", translet.getBean("sampleBean"));
		result.put("afterAdviceResult", afterAdviceResult);
		
		return result;
	}
	
	public Object errorCaused(JobTranslet translet) {
		int i = 1 / 0;
		
		return i;
	}
	
}
