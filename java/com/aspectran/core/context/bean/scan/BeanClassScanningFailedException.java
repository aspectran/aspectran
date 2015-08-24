package com.aspectran.core.context.bean.scan;

import com.aspectran.core.context.bean.BeanException;

public class BeanClassScanningFailedException extends BeanException {

	/** @serial */
	private static final long serialVersionUID = -1301450076259511066L;

	/**
	 * Simple constructor.
	 */
	public BeanClassScanningFailedException() {
	}

	/**
	 * Constructor to create exception with a message.
	 * 
	 * @param msg A message to associate with the exception
	 */
	public BeanClassScanningFailedException(String msg) {
		super(msg);
	}

	/**
	 * Constructor to create exception to wrap another exception.
	 * 
	 * @param cause The real cause of the exception
	 */
	public BeanClassScanningFailedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor to create exception to wrap another exception and pass a
	 * message.
	 * 
	 * @param msg The message
	 * @param cause The real cause of the exception
	 */
	public BeanClassScanningFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
