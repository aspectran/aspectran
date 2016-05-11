/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.expr.token;

import com.aspectran.core.context.expr.TokenExpressionException;

/**
 * The Class UnknownTokenTypeException.
 */
public class UnknownTokenTypeException extends TokenExpressionException {

	/** @serial */
	private static final long serialVersionUID = -5611377627628325418L;

	/**
	 * Instantiates a new Unknown token type exception.
	 *
	 * @param token the token
	 */
	public UnknownTokenTypeException(Token token) {
		super("Unknown token type", token);
	}

}
