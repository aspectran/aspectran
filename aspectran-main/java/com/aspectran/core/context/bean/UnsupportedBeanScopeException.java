package com.aspectran.core.context.bean;

import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.type.ScopeType;


public class UnsupportedBeanScopeException extends BeanException {

	/** @serial */
	static final long serialVersionUID = -5688264166039484239L;

	/**
	 * Create a new BeanInstantiationException.
	 * @param beanClass the offending bean class
	 * @param msg the detail message
	 */
	public UnsupportedBeanScopeException(ScopeType scopeType, BeanRule beanRule) {
		super(scopeType + "Application scope is not defined. beanRule " + beanRule);
	}

}
