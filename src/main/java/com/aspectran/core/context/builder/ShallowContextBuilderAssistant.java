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

import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AspectRuleMap;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.BeanRuleMap;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TemplateRuleMap;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.TransletRuleMap;

/**
 * The Class ShallowContextBuilderAssistant.
 * 
 * <p>Created: 2008. 04. 01 오후 10:25:35</p>
 */
public class ShallowContextBuilderAssistant extends ContextBuilderAssistant {

	protected final AspectRuleMap aspectRuleMap = new AspectRuleMap();
	
	protected final BeanRuleMap beanRuleMap = new BeanRuleMap();

	protected final TemplateRuleMap templateRuleMap = new TemplateRuleMap();

	protected final TransletRuleMap transletRuleMap = new TransletRuleMap();
	
	public ShallowContextBuilderAssistant() {
		setImportHandler(new ShallowImportHandler());
	}

	@Override
	public String resolveAliasType(String alias) {
		return alias;
	}

	@Override
	public String applyTransletNamePattern(String transletName) {
		return transletName;
	}

	@Override
	public void applyTransletInterface(DefaultSettings defaultSettings) throws ClassNotFoundException {
		//ignore
	}

	@Override
	public void addAspectRule(AspectRule aspectRule) {
		aspectRuleMap.put(Integer.toString(aspectRuleMap.size()), aspectRule);
	}

	@Override
	public void addBeanRule(BeanRule beanRule) throws CloneNotSupportedException, ClassNotFoundException, IOException {
		beanRuleMap.put(Integer.toString(beanRuleMap.size()), beanRule);
	}

	@Override
	public void addTransletRule(TransletRule transletRule) throws CloneNotSupportedException {
		transletRuleMap.put(Integer.toString(transletRuleMap.size()), transletRule);
	}

	@Override
	public void addTemplateRule(TemplateRule templateRule) {
		templateRuleMap.put(Integer.toString(transletRuleMap.size()), templateRule);
	}

	@Override
	public AspectRuleMap getAspectRuleMap() {
		return aspectRuleMap;
	}

	@Override
	public BeanRuleMap getBeanRuleMap() {
		return beanRuleMap;
	}

	@Override
	public TemplateRuleMap getTemplateRuleMap() {
		return templateRuleMap;
	}

	@Override
	public TransletRuleMap getTransletRuleMap() {
		return transletRuleMap;
	}

}
