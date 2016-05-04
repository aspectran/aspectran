/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.builder.importer.ShallowImportHandler;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;

/**
 * The Class ShallowContextBuilderAssistant.
 * 
 * <p>Created: 2008. 04. 01 PM 10:25:35</p>
 */
public class ShallowContextBuilderAssistant extends ContextBuilderAssistant {

	private final List<AspectRule> aspectRules = new ArrayList<>();
	
	private final List<BeanRule> beanRules = new ArrayList<>();

	private final List<TemplateRule> templateRules = new ArrayList<>();

	private final List<TransletRule> transletRules = new ArrayList<>();
	
	public ShallowContextBuilderAssistant() {
		setImportHandler(new ShallowImportHandler());
	}

	public void readyAssist(ApplicationAdapter applicationAdapter) {
		// shallow
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
		// shallow
	}

	@Override
	public void addAspectRule(AspectRule aspectRule) {
		aspectRules.add(aspectRule);
	}

	@Override
	public void addBeanRule(BeanRule beanRule) {
		beanRules.add(beanRule);
	}

	@Override
	public void addTransletRule(TransletRule transletRule) {
		transletRules.add(transletRule);
	}

	@Override
	public void addTemplateRule(TemplateRule templateRule) {
		templateRules.add(templateRule);
	}

	@Override
	public Collection<AspectRule> getAspectRules() {
		return aspectRules;
	}

	@Override
	public Collection<BeanRule> getBeanRules() {
		return beanRules;
	}

	@Override
	public Collection<TemplateRule> getTemplateRules() {
		return templateRules;
	}

	@Override
	public Collection<TransletRule> getTransletRules() {
		return transletRules;
	}
	
	@Override
	public void resolveBeanClass(String beanId, AspectRule aspectRule) {
		// shallow
	}

	@Override
	public void resolveBeanClass(String beanId, BeanActionRule beanActionRule) {
		// shallow
	}

	@Override
	public void resolveBeanClass(String beanId, BeanRule beanRule) {
		// shallow
	}

	@Override
	public void resolveBeanClass(Token token) {
		// shallow
	}

	@Override
	public void putBeanReference(String beanId, BeanReferenceInspectable someRule) {
		// shallow
	}

	@Override
	public void putBeanReference(Class<?> beanClass, BeanReferenceInspectable someRule) {
		// shallow
	}

}
