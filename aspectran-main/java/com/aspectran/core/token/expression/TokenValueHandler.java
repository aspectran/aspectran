/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.core.token.expression;

import com.aspectran.core.type.TokenType;

/**
 * <p>Created: 2008. 05. 02 오후 2:59:23</p>
 */
public interface TokenValueHandler {

	/**
	 * Handle.
	 * 
	 * @param tokenType the token type
	 * @param value the value
	 * 
	 * @return the object
	 */
	public Object handle(TokenType tokenType, Object value);
}
