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
package com.aspectran.base.context.builder;

import java.util.HashMap;
import java.util.Map;

import com.aspectran.base.util.ArrayStack;

/**
 * <p>Created: 2008. 04. 01 오후 10:25:35</p>
 */
public abstract class AbstractContextBuilderAssistant {

	/** The object stack. */
	private ArrayStack objectStack;
	
	private Map<String, Object> settings;
	
	/** The use namespaces. */
	protected boolean useNamespaces;

	/** The namespace. */
	protected String namespace;
	
	/** The type aliases. */
	private Map<String, String> typeAliases;
	
	/**
	 * Instantiates a new translets config.
	 */
	public AbstractContextBuilderAssistant() {
		this.objectStack = new ArrayStack(); 
		this.typeAliases = new HashMap<String, String>();
		this.settings = new HashMap<String, Object>();
	}

	/**
	 * Push object.
	 * 
	 * @param item the item
	 */
	public void pushObject(Object item) {
		objectStack.push(item);
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

	public Map<String, Object> getSettings() {
		return settings;
	}

	public void setSettings(Map<String, Object> settings) {
		this.settings = settings;
		applySettings();
	}

	public void putSetting(String name, Object value) {
		settings.put(name, value);
	}
	
	public Object getSetting(String name) {
		return settings.get(name);
	}
	
	public boolean isSettedTrue(String name) {
		Boolean b = (Boolean)settings.get(name);
		return b.booleanValue();
	}
	
	public boolean isSettedNull(String name) {
		Object o = settings.get(name);
		return (o == null);
	}
	
	public abstract void applySettings();
	
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
	
	/**
	 * Sets the namespace.
	 * 
	 * @param namespace the new namespace
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * Checks if is use namespaces.
	 * 
	 * @return true, if is usenamespaces
	 */
	public boolean isUseNamespaces() {
		return useNamespaces;
	}

	/**
	 * Sets the use namespaces.
	 * 
	 * @param useNamespaces the new use namespaces
	 */
	public void setUseNamespaces(boolean useNamespaces) {
		this.useNamespaces = useNamespaces;
	}
	
}
