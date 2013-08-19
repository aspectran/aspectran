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
package com.aspectran.core.activity.process.result;

import com.aspectran.core.activity.process.ActionPathMaker;

/**
 * <p>Created: 2008. 03. 23 오후 12:01:24</p>
 */
public class ActionResult {
	
	public static final Object NO_RESULT = new Object();
	
	private String actionId;

	private Object resultValue;
	
	private ContentResult parent;

	/**
	 * Gets the content id.
	 * 
	 * @return the content id
	 */
	public String getContentId() {
		if(parent != null)
			return parent.getContentId();
		
		return null;
	}

	/**
	 * Gets the action id.
	 * 
	 * @return the action id
	 */
	public String getActionId() {
		return actionId;
	}

	/**
	 * Sets the action id.
	 * 
	 * @param actionId the new action id
	 */
	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	/**
	 * Gets the result value.
	 * 
	 * @return the result value
	 */
	public Object getResultValue() {
		return resultValue;
	}

	/**
	 * Sets the result value.
	 * 
	 * @param resultValue the new result value
	 */
	public void setResultValue(Object resultValue) {
		this.resultValue = resultValue;
	}

	/**
	 * Gets the parent.
	 * 
	 * @return the parent
	 */
	public ContentResult getParent() {
		return parent;
	}

	/**
	 * Sets the parent.
	 * 
	 * @param parent the new parent
	 */
	protected void setParent(ContentResult parent) {
		this.parent = parent;
	}

	/**
	 * Gets the action path.
	 * 
	 * @return the action path
	 */
	public String getFullActionId() {
		return ActionPathMaker.concatActionPath(getContentId(), actionId);
	}

	/**
	 * Gets the action path.
	 * 
	 * @param parentFullActionId the parent action path
	 * 
	 * @return the action path
	 */
	public String getFullActionId(String parentFullActionId) {
		return ActionPathMaker.concatActionPath(parentFullActionId, getContentId(), actionId);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{contentId=").append(getContentId());
		sb.append(", actionId=").append(actionId);
		sb.append(", fullActionId=").append(getFullActionId());
		sb.append(", resultValue=").append(resultValue);
		sb.append("}");

		return sb.toString();
	}
}
