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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.aspect.AspectAdviceRulePreRegister;
import com.aspectran.core.context.aspect.AspectRuleRegistry;
import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.aspect.pointcut.PointcutFactory;
import com.aspectran.core.context.bean.ContextBeanRegistry;
import com.aspectran.core.context.bean.ScopedBeanRegistry;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.var.rule.AspectRule;
import com.aspectran.core.var.rule.AspectRuleMap;
import com.aspectran.core.var.rule.BeanRuleMap;
import com.aspectran.core.var.rule.PointcutRule;
import com.aspectran.core.var.rule.TransletRuleMap;
import com.aspectran.core.var.type.AspectTargetType;
import com.aspectran.core.var.type.BeanProxyModeType;
import com.aspectran.core.var.type.DefaultSettingType;

/**
 * TransletsContext builder.
 * 
 * <p>Created: 2008. 06. 14 오후 8:53:29</p>
 */
public abstract class AbstractActivityContextBuilder extends ContextBuilderAssistant {
	
	protected AbstractActivityContextBuilder(String applicationBasePath, ClassLoader classLoader) {
		super(applicationBasePath, classLoader);
	}
	
	protected ActivityContext makeActivityContext(ApplicationAdapter applicationAdapter) {
		AspectRuleMap aspectRuleMap = getAspectRuleMap();
		BeanRuleMap beanRuleMap = getBeanRuleMap();
		TransletRuleMap transletRuleMap = getTransletRuleMap();
		
		BeanReferenceInspector beanReferenceInspector = getBeanReferenceInspector();
		beanReferenceInspector.inspect(beanRuleMap);
		
		AspectRuleRegistry aspectRuleRegistry = makeAspectRuleRegistry(aspectRuleMap, beanRuleMap, transletRuleMap);
		
		ActivityContext context = new ActivityContext();
		context.setApplicationAdapter(applicationAdapter);
		context.setAspectRuleRegistry(aspectRuleRegistry);
		context.setActivityDefaultHandler((String)getSetting(DefaultSettingType.ACTIVITY_DEFAULT_HANDLER));

		BeanProxyModeType beanProxyMode = BeanProxyModeType.valueOf((String)getSetting(DefaultSettingType.BEAN_PROXY_MODE));
		ContextBeanRegistry beanRegistry = makeBeanRegistry(context, beanRuleMap, beanProxyMode);
		context.setLocalBeanRegistry(beanRegistry);
		
		TransletRuleRegistry transletRuleRegistry = makeTransletRegistry(transletRuleMap);
		context.setTransletRuleRegistry(transletRuleRegistry);
		
		return context;
	}
	
	protected AspectRuleRegistry makeAspectRuleRegistry(AspectRuleMap aspectRuleMap, BeanRuleMap beanRuleMap, TransletRuleMap transletRuleMap) {
		PointcutFactory pointcutFactory = new PointcutFactory();
		
		for(AspectRule aspectRule : aspectRuleMap) {
			if(aspectRule.getAspectTargetType() == AspectTargetType.TRANSLET) {
				PointcutRule pointcutRule = aspectRule.getPointcutRule();
				
				if(pointcutRule != null) {
					Pointcut pointcut = pointcutFactory.createPointcut(pointcutRule);
					aspectRule.setPointcut(pointcut);
					
//					List<PointcutPattern> pointcutPatternList = pointcut.getPointcutPatternList();
//					boolean onlyTransletRelevanted = true;
//					
//					for(PointcutPattern pp : pointcutPatternList) {
//						if(pp.getBeanOrActionIdPattern() != null || pp.getBeanMethodNamePattern() != null) {
//							onlyTransletRelevanted = false;
//							break;
//						}
//					}
//					
//					aspectRule.setOnlyTransletRelevanted(onlyTransletRelevanted);
//				} else {
//					aspectRule.setOnlyTransletRelevanted(true);
				}
			}
		}
		
		AspectAdviceRulePreRegister aspectAdviceRuleRegister = new AspectAdviceRulePreRegister(aspectRuleMap);
		aspectAdviceRuleRegister.register(beanRuleMap, transletRuleMap);

		pointcutFactory.close();
		
		return new AspectRuleRegistry(aspectRuleMap);
	}
	
	protected ContextBeanRegistry makeBeanRegistry(ActivityContext context, BeanRuleMap beanRuleMap, BeanProxyModeType beanProxyMode) {
		beanRuleMap.freeze();
		
		return new ScopedBeanRegistry(context, beanRuleMap, beanProxyMode);
	}

	protected TransletRuleRegistry makeTransletRegistry(TransletRuleMap transletRuleMap) {
		transletRuleMap.freeze();
		
		return new TransletRuleRegistry(transletRuleMap);
	}
	
}
