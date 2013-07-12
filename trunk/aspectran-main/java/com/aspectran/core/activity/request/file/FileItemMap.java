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
package com.aspectran.core.activity.request.file;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>Created: 2008. 03. 29 오후 6:23:00</p>
 */
public class FileItemMap extends LinkedHashMap<String, Object> {
	
	/** @serial */
	static final long serialVersionUID = 8579876394671525755L;

	/**
	 * Gets the value.
	 * 
	 * @param name the name
	 * 
	 * @return the value
	 */
	public FileItem getFileItem(String name) {
		Object value = get(name);
		
		if(value instanceof String)
			return (FileItem)value;
		
		return null;
	}
	
	/**
	 * Gets the values.
	 * 
	 * @param name the name
	 * 
	 * @return the values
	 */
	public FileItem[] getFileItems(String name) {
		Object value = get(name);
		
		if(value instanceof FileItem[])
			return (FileItem[])value;
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Object put(String key, Object value) {
		if(value == null || value instanceof FileItem || value instanceof FileItem[])
			return super.put(key, value);

		throw new IllegalArgumentException("Invalid value type.");
	}

	/* (non-Javadoc)
	 * @see java.util.HashMap#putAll(java.util.Map)
	 */
	@Override
	public void putAll(Map<? extends String, ? extends Object> t) {
		for(Map.Entry<? extends String, ? extends Object> entry : t.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Sets the file-item.
	 * If the file-item had already been set, the new value overwrites the previous one.
	 * 
	 * @param name the file item name
	 * @param fileItem the file item
	 */
	public void putFileItem(String name, FileItem fileItem) {
		super.put(name, fileItem);
	}
	
	/**
	 * Sets the file item.
	 * If the file-item had already been set, the new value overwrites the previous one.
	 * 
	 * @param name the file item name
	 * @param fileItems the file items
	 */
	public void putFileItem(String name, FileItem[] fileItems) {
		super.put(name, fileItems);
	}
	
	/**
	 * Copies all of the mappings from the specified map to this map.
	 * 
	 * @param fileItemMap the file item map
	 */
	public void putFileItem(FileItemMap fileItemMap) {
		super.putAll(fileItemMap);
	}
	
	/**
	 * delete all saved files.
	 */
	public void rollback() {
		for(Object o : values()) {
			if(o instanceof FileItem[]) {
				for(FileItem f : (FileItem[])o) {
					f.rollback();
				}
			} else {
				((FileItem)o).rollback();
			}
		}
	}

}