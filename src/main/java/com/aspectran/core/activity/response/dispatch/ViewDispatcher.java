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
package com.aspectran.core.activity.response.dispatch;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.rule.DispatchResponseRule;

/**
 * The Interface ViewDispatcher.
 * 
 * @since 2008. 03. 23
 */
public interface ViewDispatcher {
	
	/**
	 * Dispaching according to a given rule, and transmits this response.
	 *
	 * @param activity the current activity
	 * @param dispatchResponseRule the dispatch response rule
	 */
	public void dispatch(Activity activity, DispatchResponseRule dispatchResponseRule) throws ViewDispatchException;
	
}
