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
package com.aspectran.base.rule;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * <p>Created: 2008. 03. 29 오후 6:22:08</p>
 */
public class FileItemRuleMap extends LinkedHashMap<String, FileItemRule> implements Iterable<FileItemRule> {

	/** @serial */
	static final long serialVersionUID = 1151281684814174816L;

	/**
	 * Put file item rule.
	 * 
	 * @param fileItemRule the file item rule
	 * 
	 * @return the file item rule
	 */
	public FileItemRule putFileItemRule(FileItemRule fileItemRule) {
		return put(fileItemRule.getName(), fileItemRule);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<FileItemRule> iterator() {
		return this.values().iterator();
	}
}