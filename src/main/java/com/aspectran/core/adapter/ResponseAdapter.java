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
package com.aspectran.core.adapter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import com.aspectran.core.context.rule.RedirectResponseRule;

/**
 * The Interface ResponseAdapter.
 *
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public interface ResponseAdapter {

	/**
	 * Returns the adaptee object to provide response information.
	 *
	 * @param <T> the type of the adaptee
	 * @return the adaptee object
	 */
	<T> T getAdaptee();
	
	/**
	 * Returns the name of the character encoding (MIME charset) used for the body
	 * sent in this response.
	 *
	 * @return a {@code String} specifying the name of the character encoding,
	 * 			for example, UTF-8
	 */
	String getCharacterEncoding();
	
	/**
	 * Sets the character encoding of the response being sent to the client.
	 *
	 * @param characterEncoding a {@code String} specifying only the character set
	 * 			defined by IANA Character Sets (http://www.iana.org/assignments/character-sets)
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException;
	
	/**
	 * Returns the content type used for the MIME body sent in this response.
	 *
	 * @return a {@code String} specifying the content type,
	 * 			for example, {@code text/html}, or null
	 */
	String getContentType();

	/**
	 * Sets the content type of the response being sent to the client,
	 * if the response has not been committed yet.
	 *
	 * @param contentType a {@code String} specifying the MIME type of the content
	 */
	void setContentType(String contentType);

	/**
	 * Returns a {@code OutputStream} suitable for writing binary data in the response.
	 *
	 * @return a {@code OutputStream} for writing binary data 
	 * @throws IOException if an input or output exception occurs
	 */
	OutputStream getOutputStream() throws IOException;
	
	/**
	 * Returns a {@code Writer} object that can send character text to the client.
	 *
	 * @return a {@code Writer} object that can return character data to the client
	 * @throws IOException if an input or output exception occurs
	 */
	Writer getWriter() throws IOException;
	
	/**
	 * Redirects a client to a new URL.
	 *
	 * @param target the redirect target
	 * @throws IOException if an input or output exception occurs
	 */
	void redirect(String target) throws IOException;
	
	/**
	 * Redirects a client to a new URL.
	 *
	 * @param redirectResponseRule the redirect response rule
	 * @return the redirect target
	 * @throws IOException if an input or output exception occurs
	 */
	String redirect(RedirectResponseRule redirectResponseRule) throws IOException;
	
}
