package com.aspectran.example.bean;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.context.bean.ablility.InitializableBean;

public class SampleBean implements InitializableBean, DisposableBean {

	@Override
	public void initialize() throws Exception {
		System.out.println("initialize sample bean.");
	}

	@Override
	public void destroy() throws Exception {
		System.out.println("destroy sample bean.");
	}
	
	public String hello(Translet translet) {
		return "Hello~";
	}
	
	public String toString() {
		return "Hello! I'm a SampleBean.";
	}

}
