package com.aspectran.example.aspect;

import com.aspectran.web.activity.WebTranslet;

public class HelloAdvice {

	public String _beforeCountTo10() {
		return "before count to 10";
	}
	
	public String beforeCountTo10(WebTranslet translet) {
		return "★before count to 10";
	}
	
	public String afterCountTo10(WebTranslet translet) {
		return "★after count to 10";
	}
	
}
