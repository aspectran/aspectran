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
package com.aspectran.core.context.builder;

import java.io.IOException;

import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.TransletRule;

/**
 * The Class ShallowContextBuilderAssistant.
 * 
 * <p>Created: 2008. 04. 01 오후 10:25:35</p>
 */
public class ShallowContextBuilderAssistant extends ContextBuilderAssistant {

	/**
	 * Instantiates a new shallow context builder assistant.
	 */
	public ShallowContextBuilderAssistant() {
		setImportHandler(new ShallowImportHandler());
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
	 * @throws CloneNotSupportedException the clone not supported exception
	 * @throws ClassNotFoundException the class not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void addBeanRule(BeanRule beanRule) throws CloneNotSupportedException, ClassNotFoundException, IOException {
		beanRuleMap.put(Integer.toString(beanRuleMap.size()), beanRule);
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.context.builder.ContextBuilderAssistant#addTransletRule(com.aspectran.core.context.rule.TransletRule)
	 */
	public void addTransletRule(TransletRule transletRule) throws CloneNotSupportedException {
		transletRuleMap.addShallowTransletRule(transletRule);
	}
	
}
