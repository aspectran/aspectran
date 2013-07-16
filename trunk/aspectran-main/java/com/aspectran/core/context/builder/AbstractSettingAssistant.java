/*
 *  Copyright (c) 2009 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.core.context.builder;

import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.type.ActivitySettingType;
import com.aspectran.core.util.ArrayStack;

/**
 * <p>Created: 2008. 04. 01 오후 10:25:35</p>
 */
public abstract class AbstractSettingAssistant {

	/** The object stack. */
	private ArrayStack objectStack;
	
	private Map<ActivitySettingType, String> settings;
	
	/** The type aliases. */
	private Map<String, String> typeAliases;
	
	/**
	 * Instantiates a new translets config.
	 */
	public AbstractSettingAssistant() {
		this.objectStack = new ArrayStack(); 
		this.typeAliases = new HashMap<String, String>();
		this.settings = new HashMap<ActivitySettingType, String>();
	}

	/**
	 * Push object.
	 * 
	 * @param object the item
	 */
	public void pushObject(Object object) {
		objectStack.push(object);
	}
	
	/**
	 * Pop object.
	 * 
	 * @return the object
	 */
	public Object popObject() {
		return objectStack.pop();
	}
	
	/**
	 * Peek object.
	 * 
	 * @return the object
	 */
	public Object peekObject() {
		return objectStack.peek();
	}
	
	/**
	 * Clear object stack.
	 */
	public void clearObjectStack() {
		objectStack.clear();
	}
	
	/**
	 * Clear type aliases.
	 */
	public void clearTypeAliases() {
		typeAliases.clear();
	}

	public Map<ActivitySettingType, String> getSettings() {
		return settings;
	}

	public void setSettings(Map<ActivitySettingType, String> settings) {
		this.settings = settings;
		applyActivitySettings();
	}

	public void putSetting(ActivitySettingType settingType, String value) {
		settings.put(settingType, value);
	}
	
	public Object getSetting(ActivitySettingType settingType) {
		return settings.get(settingType);
	}
	
	public abstract void applyActivitySettings();
	
	/**
	 * Adds the type alias.
	 * 
	 * @param alias the alias
	 * @param type the type
	 */
	public void addTypeAlias(String alias, String type) {
		typeAliases.put(alias, type);
	}
	
	/**
	 * Gets the alias type.
	 * 
	 * @param alias the alias
	 * 
	 * @return the alias type
	 */
	public String getAliasType(String alias) {
		return typeAliases.get(alias);
	}
	
}
