package com.aspectran.core.context.bean;

import com.aspectran.core.context.rule.BeanRule;


public class BeanDestroyFailedException extends BeanException {

	/** @serial */
	private static final long serialVersionUID = -2416583532228763870L;

	/**
	 * Create a new BeanInstantiationException.
	 *
	 * @param beanRule the bean rule
	 */
	public BeanDestroyFailedException(BeanRule beanRule) {
		this(beanRule, null);
	}

	/**
	 * Create a new BeanInstantiationException.
	 *
	 * @param beanRule the bean rule
	 * @param cause the root cause
	 */
	public BeanDestroyFailedException(BeanRule beanRule, Throwable cause) {
		super("Cannot destroy a bean " + beanRule, cause);
	}
}
