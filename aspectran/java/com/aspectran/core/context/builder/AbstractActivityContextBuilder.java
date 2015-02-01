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

import java.io.IOException;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.aspect.AspectAdviceRulePreRegister;
import com.aspectran.core.context.aspect.AspectRuleRegistry;
import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.aspect.pointcut.PointcutFactory;
import com.aspectran.core.context.bean.ContextBeanRegistry;
import com.aspectran.core.context.bean.ScopedContextBeanRegistry;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.io.FileImportStream;
import com.aspectran.core.util.io.ImportStream;
import com.aspectran.core.util.io.ResourceImportStream;
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

//		for(BeanRule br : beanRuleMap) {
//			System.out.println("###BeanRule " + br);
//		}
//
		
		BeanReferenceInspector beanReferenceInspector = getBeanReferenceInspector();
		beanReferenceInspector.inspect(beanRuleMap);
		
		AspectRuleRegistry aspectRuleRegistry = makeAspectRuleRegistry(aspectRuleMap, beanRuleMap, transletRuleMap);
		
		ActivityContext context = new ActivityContext(applicationAdapter);
		context.setAspectRuleRegistry(aspectRuleRegistry);
		context.setActivityDefaultHandler((String)getSetting(DefaultSettingType.ACTIVITY_DEFAULT_HANDLER));

		BeanProxyModeType beanProxyMode = BeanProxyModeType.valueOf((String)getSetting(DefaultSettingType.BEAN_PROXY_MODE));
		ContextBeanRegistry contextBeanRegistry = makeContextBeanRegistry(context, beanRuleMap, beanProxyMode);
		context.setContextBeanRegistry(contextBeanRegistry);
		
		TransletRuleRegistry transletRuleRegistry = makeTransletRegistry(transletRuleMap);
		context.setTransletRuleRegistry(transletRuleRegistry);
		
		return context;
	}
	
	protected AspectRuleRegistry makeAspectRuleRegistry(AspectRuleMap aspectRuleMap, BeanRuleMap beanRuleMap, TransletRuleMap transletRuleMap) {
		for(AspectRule aspectRule : aspectRuleMap) {
			if(aspectRule.getAspectTargetType() == AspectTargetType.TRANSLET) {
				PointcutRule pointcutRule = aspectRule.getPointcutRule();
				
				if(pointcutRule != null) {
					Pointcut pointcut = PointcutFactory.createPointcut(pointcutRule);
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
		
		return new AspectRuleRegistry(aspectRuleMap);
	}
	
	protected ContextBeanRegistry makeContextBeanRegistry(ActivityContext context, BeanRuleMap beanRuleMap, BeanProxyModeType beanProxyMode) {
		beanRuleMap.freeze();
		
		return new ScopedContextBeanRegistry(context, beanRuleMap, beanProxyMode);
	}

	protected TransletRuleRegistry makeTransletRegistry(TransletRuleMap transletRuleMap) {
		transletRuleMap.freeze();
		
		return new TransletRuleRegistry(transletRuleMap);
	}
	
	protected ImportStream makeImportStream(String rootContext) throws IOException {
		if(rootContext == null)
			throw new IllegalArgumentException("rootContext must not be null");
		
		ImportStream importStream = null;

		if(rootContext.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
			String resource = rootContext.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
			importStream = new ResourceImportStream(getClassLoader(), resource);
		} else if(rootContext.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
			String filePath = rootContext.substring(ResourceUtils.FILE_URL_PREFIX.length());
			importStream = new FileImportStream(filePath);
		} else {
			importStream = new FileImportStream(getApplicationBasePath(), rootContext);
		}

		return importStream;
	}
	
}
