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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.type.FileItemUnityType;

/**
 * <p>Created: 2008. 03. 27 오후 5:57:09</p>
 */
public class FileItemRule {
	
	private FileItemUnityType unityType;

	private String name;

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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{type=").append(unityType.toString());
		sb.append(", name=").append(name);
		sb.append("}");
		
		return sb.toString();
	}
	
	public static FileItemRule newInstance(String name) {
		FileItemRule fir = new FileItemRule();
		fir.setName(name);

		return fir;
	}
	
}
