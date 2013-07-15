/*
 *  Copyright (c) 2010 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.core.activity.response.dispatch;

import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.rule.DispatchResponseRule;

/**
 * <p>Created: 2008. 03. 23 오후 12:52:04</p>
 */
public interface ViewDispatcher {
	
	/**
	 * Response.
	 *
	 * @param activity the translet
	 * @param viewName the view name
	 * @throws ResponseException the response exception
	 */
	public void dispatch(AspectranActivity activity, DispatchResponseRule dispatchResponseRule) throws ResponseException;
}
