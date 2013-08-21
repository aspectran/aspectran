package com.aspectran.core.context.bean;

import com.aspectran.core.rule.BeanRule;


public class BeanDestroyFailedException extends BeanException {

	/** @serial */
	static final long serialVersionUID = -4615273809502623416L;

	/**
	 * Create a new BeanInstantiationException.
	 * @param beanClass the offending bean class
	 * @param msg the detail message
	 */
	public BeanDestroyFailedException(BeanRule beanRule) {
		this(beanRule, null);
	}

	/**
	 * Create a new BeanInstantiationException.
	 * @param beanClass the offending bean class
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public BeanDestroyFailedException(BeanRule beanRule, Throwable cause) {
		super("Cannot destroy a bean " + beanRule, cause);
	}
}
