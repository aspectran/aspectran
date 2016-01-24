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
package com.aspectran.core.activity.process.result;

import java.util.ArrayList;

/**
 * The Class ContentResult.
 * 
 * <p>Created: 2008. 03. 23 오후 12:01:24</p>
 */
public class ContentResult extends ArrayList<ActionResult> {

	/** @serial */
	static final long serialVersionUID = 7394299260107452305L;

	private String name;
	
	private boolean omittable;

	public ContentResult() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ActionResult getActionResult(String actionId) {
		for(ActionResult actionResult : this) {
			if(actionId.equals(actionResult.getActionId()))
				return actionResult;
		}
		
		return null;
	}
	
	public boolean isOmittable() {
		return omittable;
	}

	public void setOmittable(boolean omittable) {
		this.omittable = omittable;
	}

	/**
	 * Adds the action result.
	 *
	 * @param actionResult the action result
	 */
	public void addActionResult(ActionResult actionResult) {
		add(actionResult);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{name=").append(name);
		sb.append(", actionResults=");
		if(!isEmpty()) {
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
