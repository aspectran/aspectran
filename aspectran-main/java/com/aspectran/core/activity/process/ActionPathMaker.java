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
package com.aspectran.core.activity.process;

import com.aspectran.base.context.builder.AspectranContextConstant;

/**
 * <p>Created: 2008. 7. 2. 오전 12:16:12</p>
 */
public class ActionPathMaker {

	/**
	 * Make a action path.
	 * 
	 * @param contentId the content id
	 * @param actionId the action id
	 * 
	 * @return the string
	 */
	public static String concatActionPath(String contentId, String actionId) {
		if(contentId == null)
			return actionId;
		
		StringBuilder sb = new StringBuilder();
		sb.append(contentId);

		if(actionId != null) {
			sb.append(AspectranContextConstant.CONTENT_ID_SEPARATOR);
			sb.append(actionId);
		}

		return sb.toString();
	}
	
	/**
	 * Make a action path.
	 * 
	 * @param parentActionPath the parent action path
	 * @param contentId the content id
	 * @param actionId the action id
	 * 
	 * @return the string
	 */
	public static String concatActionPath(String parentActionPath, String contentId, String actionId) {
		if(parentActionPath == null)
			return concatActionPath(contentId, actionId);
		
		StringBuilder sb = new StringBuilder();
		sb.append(parentActionPath);

		if(contentId != null) {
			sb.append(AspectranContextConstant.CONTENT_ID_SEPARATOR);
			sb.append(contentId);
		}

		if(actionId != null) {
			sb.append(AspectranContextConstant.CONTENT_ID_SEPARATOR);
			sb.append(actionId);
		}

		return sb.toString();
	}
}
