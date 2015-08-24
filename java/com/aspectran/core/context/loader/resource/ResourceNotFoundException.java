/*******************************************************************************
 * Copyright (c) 2008 Jeong Ju Ho.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jeong Ju Ho - initial API and implementation
 ******************************************************************************/
package com.aspectran.core.context.loader.resource;

import java.io.IOException;


/**
 * 
 * <p>Created: 2014. 12. 21 오후 12:43:55</p>
 * 
 * @author Gulendol
 * 
 */
public class ResourceNotFoundException extends IOException {

	/** @serial */
	private static final long serialVersionUID = 3099804359641288149L;

	/**
	 * Simple constructor
	 */
	public ResourceNotFoundException() {
	}

	public ResourceNotFoundException(String resourceName) {
		super("no such resource: " + resourceName);
	}
	
}
