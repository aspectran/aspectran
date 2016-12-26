package com.aspectran.core.context.bean;

/**
 * <p>Created: 2016. 12. 27.</p>
 */
public class InstantiatedBean {

	private Object bean;

	public InstantiatedBean(Object bean) {
		this.bean = bean;
	}

	public Object getBean() {
		return bean;
	}

}
