package com.aspectran.core.context.bean;


public class BeanNotFoundException extends BeanException {

	/** @serial */
	private static final long serialVersionUID = 1866105813455720749L;

	/**
	 * Instantiates a new bean not found exception.
	 *
	 * @param beanId the bean id
	 */
	public BeanNotFoundException(String beanId) {
		this(beanId, null);
	}

	/**
	 * Instantiates a new bean not found exception.
	 *
	 * @param beanId the bean id
	 * @param cause the root cause
	 */
	public BeanNotFoundException(String beanId, Throwable cause) {
		super("No bean named '" + beanId + "' is defined");
	}
}
