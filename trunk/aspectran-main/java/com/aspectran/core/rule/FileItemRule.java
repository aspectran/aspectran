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
package com.aspectran.core.rule;

import com.aspectran.core.type.FileItemUnityType;
import com.aspectran.util.FileUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * <p>Created: 2008. 03. 27 오후 5:57:09</p>
 */
public class FileItemRule {
	
	private static final String DELIMITERS = " ,;\t\n\r\f";

	private FileItemUnityType unityType;

	private String name;
	
	private Set<String> allowFileExtentions;
	
	private Set<String> denyFileExtentions;

	/**
	 * Gets the unity type.
	 * 
	 * @return the unity type
	 */
	public FileItemUnityType getUnityType() {
		return unityType;
	}
	
	/**
	 * Gets the name of the file item.
	 * 
	 * @return the name of the file item
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the file item.
	 * 
	 * @param name the new name of the file item
	 */
	public void setName(String name) {
		if(name.endsWith(ItemRule.LIST_SUFFIX)) {
			this.name = name.substring(0, name.length() - 2);
			this.unityType = FileItemUnityType.ARRAY;
		} else if(name.endsWith(ItemRule.MAP_SUFFIX)) {
			this.name = name.substring(0, name.length() - 2);
			this.unityType = FileItemUnityType.SINGLE;
		} else {
			this.name = name;
			this.unityType = FileItemUnityType.SINGLE;
		}
	}
	
	/**
	 * Gets the allowed file extentions.
	 * 
	 * @return the allowed file extentions
	 */
	public Set<String> getAllowFileExtentions() {
		return allowFileExtentions;
	}

	/**
	 * Sets the allowed file extentions.
	 * 
	 * @param allowFileExtentions the new allowed file extentions
	 */
	public void setAllowFileExtentions(Set<String> allowFileExtentions) {
		clearAll();
		this.allowFileExtentions = allowFileExtentions;
	}

	/**
	 * Gets the denied file extentions.
	 * 
	 * @return the denied file extentions
	 */
	public Set<String> getDenyFileExtentions() {
		return denyFileExtentions;
	}

	/**
	 * Sets the denied file extentions.
	 * 
	 * @param denyFileExtentions the new denied file extentions
	 */
	public void setDenyFileExtentions(Set<String> denyFileExtentions) {
		clearAll();
		this.denyFileExtentions = denyFileExtentions;
	}

	/**
	 * Sets the allowed file extentions.
	 * 
	 * @param extentions the new allowed file extentions
	 */
	public void setAllowFileExtentions(String extentions) {
		Set<String> allowFileExtentions = new HashSet<String>();
		
		StringTokenizer st = new StringTokenizer(extentions, DELIMITERS);
		
		while(st.hasMoreTokens()) {
			String extention = st.nextToken();
			allowFileExtentions.add(extention.toLowerCase());
		}
		
		setAllowFileExtentions(allowFileExtentions);
	}
	
	/**
	 * Sets the denied file extentions.
	 * 
	 * @param extentions the new denied file extentions
	 */
	public void setDenyFileExtentions(String extentions) {
		Set<String> denyFileExtentions = new HashSet<String>();
		
		StringTokenizer st = new StringTokenizer(extentions, DELIMITERS);
		
		while(st.hasMoreTokens()) {
			String extention = st.nextToken();
			denyFileExtentions.add(extention.toLowerCase());
		}
		
		setDenyFileExtentions(denyFileExtentions);
	}

	/**
	 * Checks if is valid file extention.
	 * 
	 * @param fileName the file name
	 * 
	 * @return true, if is valid file extention
	 */
	public boolean isValidFileExtention(String fileName) {
		String ext = FileUtils.getFileExtention(fileName);
		
		if(denyFileExtentions != null)
			return !denyFileExtentions.contains(ext);

		if(allowFileExtentions != null)
			return allowFileExtentions.contains(ext);
		
		return true;
	}
	
	/**
	 * Clear all.
	 */
	private void clearAll() {
		if(allowFileExtentions != null) {
			allowFileExtentions.clear();
			allowFileExtentions = null;
		}		

		if(denyFileExtentions != null) {
			denyFileExtentions.clear();
			denyFileExtentions = null;
		}		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{type=").append(unityType.toString());
		sb.append(", name=").append(name);
		sb.append(", allowFileExtentions=").append(allowFileExtentions);
		sb.append(", denyFileExtentions=").append(denyFileExtentions);
		sb.append("}");
		
		return sb.toString();
	}
}
