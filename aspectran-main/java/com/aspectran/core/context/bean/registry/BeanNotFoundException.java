package com.aspectran.core.context.bean.registry;

import com.aspectran.core.context.bean.BeansException;

public class BeanNotFoundException extends BeansException {

	/** @serial */
	static final long serialVersionUID = -7128311266333981625L;

	/**
	 * Create a new BeanInstantiationException.
	 * @param beanClass the offending bean class
	 * @param msg the detail message
	 */
	public BeanNotFoundException(String beanId) {
		this(beanId, null);
	}

	/**
	 * Create a new BeanInstantiationException.
	 * @param beanClass the offending bean class
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public BeanNotFoundException(String beanId, Throwable cause) {
		super("No bean named '" + beanId + "' is defined");
	}
}
