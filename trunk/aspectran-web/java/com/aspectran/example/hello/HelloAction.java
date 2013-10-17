package com.aspectran.example.hello;

import com.aspectran.web.activity.WebTranslet;

public class HelloAction {

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
	
}
