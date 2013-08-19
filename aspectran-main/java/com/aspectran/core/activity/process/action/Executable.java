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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.type.ActionType;

/**
 * <p>Created: 2008. 03. 23 오전 10:38:29</p>
 */
public interface Executable {

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getActionId();

	public String getFullActionId();

	/**
	 * Execute this action.
	 *
	 * @param activity the activity
	 * @return the result of action execution
	 * @throws ActionExecutionException the action execution exception
	 */
	public Object execute(AspectranActivity activity) throws ActionExecutionException;
	
	/**
	 * Checks if is hidden.
	 * 
	 * @return true, if is hidden
	 */
	public boolean isHidden();
	
	/**
	 * Gets the action list.
	 * 
	 * @return the action list
	 */
	public ActionList getParent();
	
	/**
	 * Gets the Action Type.
	 *
	 * @return the Action Type
	 */
	public ActionType getActionType();
	
	/**
	 * Gets the aspect advice registry.
	 *
	 * @return the aspect advice registry
	 */
	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry();
	
}
