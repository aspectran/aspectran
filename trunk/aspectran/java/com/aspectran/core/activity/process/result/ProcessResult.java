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
 * <p>Created: 2008. 06. 09 오후 4:13:40</p>
 */
public class ProcessResult extends ArrayList<ContentResult> {

	/** @serial */
	static final long serialVersionUID = 4734650376929217378L;

	private String name;
	
	private boolean omittable;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isOmittable() {
		return omittable;
	}

	public void setOmittable(boolean omittable) {
		this.omittable = omittable;
	}

	/**
	 * Adds the content result.
	 * 
	 * @param contentResult the content result
	 */
	public void addContentResult(ContentResult contentResult) {
		add(contentResult);
	}
	
	public ContentResult findContentResult(String contentId) {
		for(ContentResult contentResult : this) {
			if(contentResult.getContentId() != null && contentResult.getContentId().equals(contentId))
				return contentResult;
		}
		
		return null;
	}
	
	public ActionResult findActionResult(String actionId) {
		for(ContentResult contentResult : this) {
			ActionResult actionResult = contentResult.getActionResult(actionId);
			
			if(actionResult != null)
				return actionResult;
		}
		
		return null;
	}
	
	public Object getResultValue(String actionId) {
		ActionResult actionResult = findActionResult(actionId);
		
		if(actionResult != null)
			return actionResult.getResultValue();
		
		return null;
	}
	
}
