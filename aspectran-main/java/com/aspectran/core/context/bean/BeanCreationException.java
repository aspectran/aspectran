package com.aspectran.core.context.bean;

import com.aspectran.core.context.rule.BeanRule;


public class BeanCreationException extends BeanException {

	/** @serial */
	static final long serialVersionUID = -4615273809502623416L;

	/**
	 * Create a new BeanInstantiationException.
	 * @param beanClass the offending bean class
	 * @param msg the detail message
	 */
	public BeanCreationException(BeanRule beanRule) {
		this(beanRule, null);
	}

	/**
	 * Create a new BeanInstantiationException.
	 * @param beanClass the offending bean class
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public BeanCreationException(BeanRule beanRule, Throwable cause) {
		super("Cannot create a bean " + beanRule, cause);
	}
}
