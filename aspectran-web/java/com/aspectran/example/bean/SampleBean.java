package com.aspectran.example.bean;

import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.context.bean.ablility.InitializableBean;
import com.aspectran.web.activity.WebTranslet;

public class SampleBean implements InitializableBean, DisposableBean {

	@Override
	public void initialize() throws Exception {
		System.out.println("initialize sample bean.");
	}

	@Override
	public void destroy() throws Exception {
		System.out.println("destroy sample bean.");
	}
	
	public String hello(WebTranslet translet) {
		return "Hello~";
	}

}
