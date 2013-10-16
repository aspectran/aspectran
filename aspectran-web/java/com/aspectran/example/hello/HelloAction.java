package com.aspectran.example.hello;

import com.aspectran.web.activity.WebTranslet;

public class HelloAction {

	public String countTo100(WebTranslet translet) {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 1; i <= 100; i++) {
			sb.append(i).append("\n");
		}
		
		return sb.toString();
	}
	
}
