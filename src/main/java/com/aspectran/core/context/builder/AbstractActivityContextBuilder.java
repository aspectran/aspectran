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

import java.util.List;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.AspectranActivityContext;
import com.aspectran.core.context.aspect.AspectAdviceRulePostRegister;
import com.aspectran.core.context.aspect.AspectAdviceRulePreRegister;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.aspect.AspectRuleRegistry;
import com.aspectran.core.context.aspect.InvalidPointcutPatternException;
import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.aspect.pointcut.PointcutFactory;
import com.aspectran.core.context.bean.BeanRuleRegistry;
import com.aspectran.core.context.bean.ContextBeanRegistry;
import com.aspectran.core.context.builder.importer.FileImporter;
import com.aspectran.core.context.builder.importer.Importer;
import com.aspectran.core.context.builder.importer.ResourceImporter;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.type.AspectTargetType;
import com.aspectran.core.context.rule.type.BeanProxifierType;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.context.rule.type.ImportFileType;
import com.aspectran.core.context.rule.type.JoinpointScopeType;
import com.aspectran.core.context.template.ContextTemplateProcessor;
import com.aspectran.core.context.template.TemplateProcessor;
import com.aspectran.core.context.template.TemplateRuleRegistry;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.util.BeanDescriptor;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.SystemUtils;

/**
 * The Class AbstractActivityContextBuilder.
 * 
 * <p>Created: 2008. 06. 14 PM 8:53:29</p>
 */
abstract class AbstractActivityContextBuilder extends ContextBuilderAssistant implements ActivityContextBuilder {
	
	private static final String ACTIVE_PROFILES_PROPERTY_NAME = "aspectran.profiles.active";
	
	private ApplicationAdapter applicationAdapter;
	
	private boolean hybridLoad;
	
	private String[] activeProfiles;
	
	AbstractActivityContextBuilder(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
	}

	public ApplicationAdapter getApplicationAdapter() {
		return applicationAdapter;
	}

	public boolean isHybridLoad() {
		return hybridLoad;
	}

	public void setHybridLoad(boolean hybridLoad) {
		this.hybridLoad = hybridLoad;
	}
	
	public String[] getActiveProfiles() {
		if(activeProfiles == null) {
			String profilesProp = SystemUtils.getProperty(ACTIVE_PROFILES_PROPERTY_NAME);
			if(profilesProp != null) {
				String[] profiles = StringUtils.tokenize(StringUtils.trimAllWhitespace(profilesProp), ",");
				if(profiles != null && profiles.length > 0) {
					activeProfiles = profiles;
				}
			}
		}
		return activeProfiles;
	}

	public void setActiveProfiles(String[] activeProfiles) {
		this.activeProfiles = activeProfiles;
	}

	/**
	 * Returns a new instance of ActivityContext.
	 *
	 * @param applicationAdapter the application adapter
	 * @return the activity context
	 * @throws BeanReferenceException will be thrown when cannot resolve reference to bean
	 */
	ActivityContext makeActivityContext(ApplicationAdapter applicationAdapter) throws BeanReferenceException {
		AspectRuleRegistry aspectRuleRegistry = getAspectRuleRegistry();

		BeanRuleRegistry beanRuleRegistry = getBeanRuleRegistry();
		beanRuleRegistry.postProcess();

		TransletRuleRegistry transletRuleRegistry = getTransletRuleRegistry();
		TemplateRuleRegistry templateRuleRegistry = getTemplateRuleRegistry();

		BeanReferenceInspector beanReferenceInspector = getBeanReferenceInspector();
		beanReferenceInspector.inspect(beanRuleRegistry);
		
		initAspectRuleRegistry(aspectRuleRegistry, beanRuleRegistry, transletRuleRegistry);

		BeanProxifierType beanProxifierType = BeanProxifierType.lookup((String)getSetting(DefaultSettingType.BEAN_PROXIFIER));
		ContextBeanRegistry contextBeanRegistry = new ContextBeanRegistry(beanRuleRegistry, beanProxifierType);

		TemplateProcessor templateProcessor = new ContextTemplateProcessor(templateRuleRegistry);

		clearTypeAliases();
		BeanDescriptor.clearCache();
		MethodUtils.clearCache();

		AspectranActivityContext context = new AspectranActivityContext(applicationAdapter);
		context.setAspectRuleRegistry(aspectRuleRegistry);
		context.setContextBeanRegistry(contextBeanRegistry);
		context.setTransletRuleRegistry(transletRuleRegistry);
		context.setTemplateProcessor(templateProcessor);
		context.setActiveProfiles(getActiveProfiles());
		context.initialize();

		return context;
	}
	
	/**
	 * Initialize the aspect rule registry.
	 *
	 * @param aspectRuleRegistry the aspect rule registry
	 * @param beanRuleRegistry the bean rule registry
	 * @param transletRuleRegistry the translet rule registry
	 */
	private void initAspectRuleRegistry(AspectRuleRegistry aspectRuleRegistry, BeanRuleRegistry beanRuleRegistry, TransletRuleRegistry transletRuleRegistry) {
		AspectAdviceRulePostRegister sessionScopeAspectAdviceRulePostRegister = new AspectAdviceRulePostRegister();
		
		for(AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
			if(aspectRule.getAspectTargetType() == AspectTargetType.TRANSLET) {
				PointcutRule pointcutRule = aspectRule.getPointcutRule();
				
				if(pointcutRule != null) {
					Pointcut pointcut = PointcutFactory.createPointcut(pointcutRule);
					aspectRule.setPointcut(pointcut);
				}
				
				if(aspectRule.getJoinpointScope() == JoinpointScopeType.SESSION) {
					sessionScopeAspectAdviceRulePostRegister.register(aspectRule);
				}
			}
		}
		
		AspectAdviceRulePreRegister preRegister = new AspectAdviceRulePreRegister(aspectRuleRegistry);
		preRegister.register(beanRuleRegistry);
		preRegister.register(transletRuleRegistry);
		
		// check offending pointcut pattern
		boolean pointcutPatternVerifiable = isPointcutPatternVerifiable();
		int offendingPointcutPatterns = 0;
		
		for(AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
			AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();

			if(aspectTargetType == AspectTargetType.TRANSLET) {
				Pointcut pointcut = aspectRule.getPointcut();

				if(pointcut != null) {
					List<PointcutPatternRule> pointcutPatternRuleList = pointcut.getPointcutPatternRuleList();
					
					if(pointcutPatternRuleList != null) {
						for(PointcutPatternRule ppr : pointcutPatternRuleList) {
							/*
							if(ppr.getTransletNamePattern() != null && ppr.getMatchedTransletCount() == 0) {
								offendingPointcutPatterns++;
								String msg = "Incorrect pointcut pattern of translet name '" + ppr.getTransletNamePattern() + "' : aspectRule " + aspectRule;
								if(pointcutPatternVerifiable)
									log.error(msg);
								else
									log.warn(msg);
							}
							*/
							if(ppr.getBeanIdPattern() != null && ppr.getMatchedBeanCount() == 0) {
								offendingPointcutPatterns++;
								String msg = "Incorrect pointcut pattern of bean id '" + ppr.getBeanIdPattern() + "' : aspectRule " + aspectRule;
								if(pointcutPatternVerifiable)
									log.error(msg);
								else
									log.warn(msg);
							}
							if(ppr.getClassNamePattern() != null && ppr.getMatchedClassCount() == 0) {
								offendingPointcutPatterns++;
								String msg = "Incorrect pointcut pattern of class name '" + ppr.getClassNamePattern() + "' : aspectRule " + aspectRule;
								if(pointcutPatternVerifiable)
									log.error(msg);
								else
									log.warn(msg);
							}
							if(ppr.getMethodNamePattern() != null && ppr.getMatchedMethodCount() == 0) {
								offendingPointcutPatterns++;
								String msg = "Incorrect pointcut pattern of bean's method name '" + ppr.getMethodNamePattern() + "' : aspectRule " + aspectRule;
								if(pointcutPatternVerifiable)
									log.error(msg);
								else
									log.warn(msg);
							}
						}
					}
				}
			}
		}
		
		if(offendingPointcutPatterns > 0) {
			String msg = offendingPointcutPatterns + " Offending pointcut patterns. Please check the logs for more information.";
			if(pointcutPatternVerifiable) {
				log.error(msg);
				throw new InvalidPointcutPatternException(msg);
			} else {
				log.warn(msg);
			}
		}
		
		AspectAdviceRuleRegistry sessionScopeAspectAdviceRuleRegistry = sessionScopeAspectAdviceRulePostRegister.getAspectAdviceRuleRegistry();
		if(sessionScopeAspectAdviceRuleRegistry != null)
			aspectRuleRegistry.setSessionAspectAdviceRuleRegistry(sessionScopeAspectAdviceRuleRegistry);
	}

	Importer resolveImporter(String rootContext) {
		ImportFileType importFileType = rootContext.toLowerCase().endsWith(".apon") ? ImportFileType.APON : ImportFileType.XML;
		return resolveImporter(rootContext, importFileType);
	}
	
	Importer resolveImporter(String rootContext, ImportFileType importFileType) {
		Importer importer;

		if(rootContext.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
			String resource = rootContext.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
			importer = new ResourceImporter(getClassLoader(), resource, importFileType);
		} else if(rootContext.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
			String filePath = rootContext.substring(ResourceUtils.FILE_URL_PREFIX.length());
			importer = new FileImporter(filePath, importFileType);
		} else {
			importer = new FileImporter(getApplicationBasePath(), rootContext, importFileType);
		}
		
		return importer;
	}
	
}
