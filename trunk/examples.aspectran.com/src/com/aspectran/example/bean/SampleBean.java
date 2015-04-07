package com.aspectran.example.bean;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.context.bean.ablility.InitializableBean;
import com.aspectran.core.util.apon.GenericParameters;
import com.aspectran.core.util.apon.Parameters;

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
	
	public Parameters getParameters() {
		Parameters p = new GenericParameters();
		p.putValue("p1", "동해물과");
		p.putValue("p2", "백두산이");
		p.putValue("p3", "마르고 닳도록");
		return p;
	}

}
