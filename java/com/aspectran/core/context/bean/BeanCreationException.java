package com.aspectran.core.context.bean;

import com.aspectran.core.context.rule.BeanRule;


public class BeanCreationException extends BeanException {

	/** @serial */
	private static final long serialVersionUID = 8126208342749350818L;

	/**
	 * Instantiates a new bean creation exception.
	 *
	 * @param beanRule the bean rule
	 */
	public BeanCreationException(BeanRule beanRule) {
		this(beanRule, null);
	}

	/**
	 * Instantiates a new bean creation exception.
	 *
	 * @param beanRule the bean rule
	 * @param cause the root cause
	 */
	public BeanCreationException(BeanRule beanRule, Throwable cause) {
		super("Cannot create a bean " + beanRule, cause);
	}
}
