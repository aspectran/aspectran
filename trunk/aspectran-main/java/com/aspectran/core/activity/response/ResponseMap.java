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
package com.aspectran.core.activity.response;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * <p>Created: 2008. 03. 29 오후 11:50:02</p>
 */
public class ResponseMap extends LinkedHashMap<String, Responsible> implements Iterable<Responsible> {

	/** @serial */
	static final long serialVersionUID = 2093093144584776388L;

	/**
	 * Puts a responsible.
	 * 
	 * @param responsible the responsible
	 * 
	 * @return the responsible
	 */
	public Responsible putResponse(Responsible responsible) {
		return put(responsible.getId(), responsible);
	}
	
	/**
	 * Gets the response.
	 * 
	 * @param index the index
	 * 
	 * @return the response
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size()) 
	 */
	public Responsible get(int index) {
		if(index < 0 || index >= size())
			throw new IndexOutOfBoundsException("index is out of range.");
		
		String[] keys = keySet().toArray(new String[size()]);
		
		return get(keys[index]);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<Responsible> iterator() {
		return this.values().iterator();
	}
}
