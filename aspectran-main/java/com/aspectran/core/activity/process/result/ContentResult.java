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

import java.util.ArrayList;

/**
 * <p>Created: 2008. 03. 23 오후 12:01:24</p>
 */
public class ContentResult extends ArrayList<ActionResult> {

	/** @serial */
	static final long serialVersionUID = 7394299260107452305L;
	
	private String contentId;
	
	/**
	 * Gets the content id.
	 * 
	 * @return the content id
	 */
	public String getContentId() {
		return contentId;
	}

	/**
	 * Sets the content id.
	 * 
	 * @param contentId the new content id
	 */
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}
	
	/**
	 * Adds the action result.
	 * 
	 * @param actionResult the action result
	 */
	public void addActionResult(ActionResult actionResult) {
		add(actionResult);
	}
	
	public void addActionResult(String actionId, Object resultValue) {
		ActionResult actionResult = new ActionResult();
		actionResult.setActionId(actionId);
		actionResult.setResultValue(resultValue);
		actionResult.setParent(this);

		add(actionResult);
	}
	
	/* (non-Javadoc)
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	public boolean add(ActionResult actionResult) {
		if(actionResult.getParent() == null)
			actionResult.setParent(this);
		
		return super.add(actionResult);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{contentId=").append(contentId);
		sb.append(", actionResults=");

		if(size() > 0) {
			sb.append('[');
			
			String name;
			int offset;
			
			for(int i = 0; i < size(); i++) {
				ActionResult actionResult = get(i);
				
				if(i > 0)
					sb.append(", ");
				
				name = actionResult.toString();
				offset = name.lastIndexOf('.') + 1;
				
				sb.append(name.substring(offset));
			}

			sb.append(']');
		}
		
		sb.append("}");
		
		return sb.toString();
	}
}
