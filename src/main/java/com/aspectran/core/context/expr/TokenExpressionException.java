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
package com.aspectran.core.context.expr;

import com.aspectran.core.context.AspectranRuntimeException;
import com.aspectran.core.context.expr.token.Token;

/**
 * The Class TokenExpressionException.
 */
public class TokenExpressionException extends AspectranRuntimeException {

	/** @serial */
	private static final long serialVersionUID = 2303202652519466514L;

	private Token token;

	/**
	 * Instantiates a new token expression exception.
	 *
	 * @param msg the detail message
	 * @param token the token
	 */
	public TokenExpressionException(String msg, Token token) {
		super(msg + " " + token);
		this.token = token;
	}

	/**
	 * Instantiates a new token expression exception.
	 *
	 * @param msg the detail message
	 * @param token the token
	 * @param cause the root cause
	 */
	public TokenExpressionException(String msg, Token token, Throwable cause) {
		super(msg + " " + token, cause);
		this.token = token;
	}

	/**
	 * Gets the token.
	 *
	 * @return the token
	 */
	public Token getToken() {
		return this.token;
	}

}
