package com.aspectran.core.context.bean;

import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.type.ScopeType;

/**
 * The Class UnsupportedBeanScopeException.
 */
public class UnsupportedBeanScopeException extends BeanException {

	/** @serial */
	private static final long serialVersionUID = -5350555208208267662L;

	/**
	 * Create a new BeanInstantiationException.
	 * @param beanClass the offending bean class
	 * @param msg the detail message
	 */
	public UnsupportedBeanScopeException(ScopeType scopeType, BeanRule beanRule) {
		super(scopeType + " scope is not defined. beanRule " + beanRule);
	}

}
