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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.TransletRule;

/**
 * <p>Created: 2008. 04. 01 오후 10:25:35</p>
 */
public class ShallowContextBuilderAssistant extends ContextBuilderAssistant {

	private final Logger logger = LoggerFactory.getLogger(ShallowContextBuilderAssistant.class);
	
	public ShallowContextBuilderAssistant() {
	}

	/**
	 * Returns the resolve alias type.
	 * 
	 * @param alias the alias
	 * 
	 * @return the string
	 */
	public String resolveAliasType(String alias) {
		return alias;
	}
	
	/**
	 * Returns the trnaslet name of the prefix and suffix are combined.
	 * 
	 * @param transletName the translet name
	 * 
	 * @return the string
	 */
	public String applyTransletNamePattern(String transletName) {
		return transletName;
	}
	
	/**
	 * Adds the bean rule.
	 *
	 * @param beanRule the bean rule
	 * @throws CloneNotSupportedException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public void addBeanRule(BeanRule beanRule) throws CloneNotSupportedException, ClassNotFoundException, IOException {
		beanRuleMap.putBeanRule(beanRule);

		if(logger.isTraceEnabled())
			logger.trace("add BeanRule " + beanRule);
	}

	public void addTransletRule(TransletRule transletRule) throws CloneNotSupportedException {
		transletRuleMap.putTransletRule(transletRule);
		
		if(logger.isTraceEnabled())
			logger.trace("add TransletRule " + transletRule);
	}
	
}
