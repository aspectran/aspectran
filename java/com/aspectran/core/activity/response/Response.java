/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.activity.response;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.context.rule.type.ResponseType;

/**
 * <p>Created: 2008. 03. 23 오후 12:52:04</p>
 */
public interface Response {
	
	/**
	 * Gets the response type.
	 * 
	 * @return the response type
	 */
	public ResponseType getResponseType();

	/**
	 * Gets the content type.
	 * 
	 * @return the content type
	 */
	public String getContentType();
	
	/**
	 * Gets the action list.
	 *
	 * @return the action list
	 */
	public ActionList getActionList();
	
	/**
	 * Response.
	 * 
	 * @param activity the translet
	 * 
	 * @throws ResponseException the response exception
	 */
	public void response(Activity activity) throws ResponseException;
	
}
