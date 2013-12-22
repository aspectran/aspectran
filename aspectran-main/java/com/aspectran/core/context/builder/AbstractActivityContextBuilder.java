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
package com.aspectran.core.context.builder;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegister;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.context.bean.ScopedBeanRegistry;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.var.rule.AspectRuleMap;
import com.aspectran.core.var.rule.BeanRuleMap;
import com.aspectran.core.var.rule.TransletRuleMap;
import com.aspectran.core.var.type.DefaultSettingType;

/**
 * TransletsContext builder.
 * 
 * <p>Created: 2008. 06. 14 오후 8:53:29</p>
 */
public abstract class AbstractActivityContextBuilder extends ContextBuilderAssistant {
	
	protected AbstractActivityContextBuilder(String applicationBasePath) {
		super(applicationBasePath);
	}
	
	protected ActivityContext makeActivityContext() {
		BeanRegistry beanRegistry = makeBeanRegistry(getBeanRuleMap());
		TransletRuleRegistry transletRuleRegistry = makeTransletRegistry(getAspectRuleMap(), getTransletRuleMap());
		
		BeanReferenceInspector beanReferenceInspector = getBeanReferenceInspector();
		beanReferenceInspector.inpect(getBeanRuleMap());
		
		ActivityContext context = new ActivityContext();
		context.setAspectRuleMap(getAspectRuleMap());
		context.setBeanRegistry(beanRegistry);
		context.setTransletRuleRegistry(transletRuleRegistry);
		context.setActivityDefaultHandler((String)getSetting(DefaultSettingType.ACTIVITY_DEFAULT_HANDLER));
		
		return context;
	}

	protected TransletRuleRegistry makeTransletRegistry(AspectRuleMap aspectRuleMap, TransletRuleMap transletRuleMap) {
		transletRuleMap.freeze();
		
		AspectAdviceRuleRegister aspectAdviceRuleRegister = new AspectAdviceRuleRegister(aspectRuleMap);
		aspectAdviceRuleRegister.register(transletRuleMap);
		
		return new TransletRuleRegistry(transletRuleMap);
	}

	protected BeanRegistry makeBeanRegistry(BeanRuleMap beanRuleMap) {
		beanRuleMap.freeze();
		
		return new ScopedBeanRegistry(beanRuleMap);
	}
	
}
