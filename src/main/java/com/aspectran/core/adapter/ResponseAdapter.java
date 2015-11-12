/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.adapter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.rule.RedirectResponseRule;

/**
 * The Interface ResponseAdapter.
 *
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public interface ResponseAdapter {

	/**
	 * Gets the Adaptee object.
	 *
	 * @param <T> the generic type
	 * @return the Adaptee object
	 */
	public <T> T getAdaptee();
	
	/**
	 * Gets the character encoding.
	 *
	 * @return the character encoding
	 */
	public String getCharacterEncoding();
	
	/**
	 * Sets the character encoding.
	 *
	 * @param characterEncoding the new character encoding
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException;
	
	/**
	 * Gets the content type.
	 *
	 * @return the content type
	 */
	public abstract String getContentType();

	/**
	 * Sets the content type.
	 *
	 * @param contentType the new content type
	 */
	public abstract void setContentType(String contentType);

	/**
	 * Gets the output stream.
	 *
	 * @return the output stream
	 * @throws IOException If an input or output exception occurs
	 */
	public OutputStream getOutputStream() throws IOException;
	
	/**
	 * Gets the writer.
	 *
	 * @return the writer
	 * @throws IOException If an input or output exception occurs
	 */
	public Writer getWriter() throws IOException;
	
	/**
	 * Redirects a client to a new URL.
	 *
	 * @param url the redirect location URL
	 * @throws IOException If an input or output exception occurs
	 */
	public void redirect(String url) throws IOException;
	
	/**
	 * Redirects a client to a new URL.
	 *
	 * @param activity the current Activity
	 * @param redirectResponseRule the redirect response rule
	 * @return the string
	 * @throws IOException If an input or output exception occurs
	 */
	public String redirect(Activity activity, RedirectResponseRule redirectResponseRule) throws IOException;
	
}
