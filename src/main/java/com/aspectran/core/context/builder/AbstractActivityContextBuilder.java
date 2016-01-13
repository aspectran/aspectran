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

import java.util.List;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.VoidActivity;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.aspect.AspectAdviceRulePostRegister;
import com.aspectran.core.context.aspect.AspectAdviceRulePreRegister;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.aspect.AspectRuleRegistry;
import com.aspectran.core.context.aspect.InvalidPointcutPatternException;
import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.aspect.pointcut.PointcutFactory;
import com.aspectran.core.context.bean.ContextBeanRegistry;
import com.aspectran.core.context.bean.ScopedContextBeanRegistry;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AspectRuleMap;
import com.aspectran.core.context.rule.BeanRuleMap;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.TemplateRuleMap;
import com.aspectran.core.context.rule.TransletRuleMap;
import com.aspectran.core.context.rule.type.AspectTargetType;
import com.aspectran.core.context.rule.type.BeanProxifierType;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.context.rule.type.ImportFileType;
import com.aspectran.core.context.rule.type.JoinpointScopeType;
import com.aspectran.core.context.template.TemplateRuleRegistry;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.util.ClassDescriptor;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * <p>Created: 2008. 06. 14 오후 8:53:29</p>
 */
public abstract class AbstractActivityContextBuilder extends ContextBuilderAssistant {
	
	private final Log log = LogFactory.getLog(AbstractActivityContextBuilder.class);
	
	public AbstractActivityContextBuilder(ApplicationAdapter applicationAdapter) {
		super(applicationAdapter);
	}
	
	protected ActivityContext makeActivityContext(ApplicationAdapter applicationAdapter) {
		AspectRuleMap aspectRuleMap = getAspectRuleMap();
		BeanRuleMap beanRuleMap = getBeanRuleMap();
		TransletRuleMap transletRuleMap = getTransletRuleMap();
		TemplateRuleMap templateRuleMap = getTemplateRuleMap();

		BeanReferenceInspector beanReferenceInspector = getBeanReferenceInspector();
		beanReferenceInspector.inspect(beanRuleMap);
		
		ActivityContext context = new ActivityContext(applicationAdapter);

		AspectRuleRegistry aspectRuleRegistry = makeAspectRuleRegistry(aspectRuleMap, beanRuleMap, transletRuleMap);
		context.setAspectRuleRegistry(aspectRuleRegistry);

		BeanProxifierType beanProxifierType = BeanProxifierType.valueOf((String)getSetting(DefaultSettingType.BEAN_PROXIFIER));
		ContextBeanRegistry contextBeanRegistry = makeContextBeanRegistry(beanRuleMap, beanProxifierType);
		context.setContextBeanRegistry(contextBeanRegistry);
		
		TransletRuleRegistry transletRuleRegistry = makeTransletRuleRegistry(transletRuleMap);
		context.setTransletRuleRegistry(transletRuleRegistry);

		TemplateRuleRegistry templateRuleRegistry = makeTemplateRuleRegistry(templateRuleMap);
		context.setTemplateRuleRegistry(templateRuleRegistry);
		
		CoreActivity activity = new VoidActivity(context);
		context.setCurrentActivity(activity);
		contextBeanRegistry.initialize(context);
		context.removeCurrentActivity();

		ClassDescriptor.clearCache();
		MethodUtils.clearCache();
		
		return context;
	}
	
	protected AspectRuleRegistry makeAspectRuleRegistry(AspectRuleMap aspectRuleMap, BeanRuleMap beanRuleMap, TransletRuleMap transletRuleMap) {
		AspectAdviceRulePostRegister sessionScopeAspectAdviceRulePostRegister = new AspectAdviceRulePostRegister();
		
		for(AspectRule aspectRule : aspectRuleMap) {
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
		
		AspectAdviceRulePreRegister aspectAdviceRuleRegister = new AspectAdviceRulePreRegister(aspectRuleMap);
		aspectAdviceRuleRegister.register(beanRuleMap);
		aspectAdviceRuleRegister.register(transletRuleMap);
		
		// check offending pointcut pattern
		boolean pointcutPatternVerifiable = isPointcutPatternVerifiable();
		int offendingPointcutPatterns = 0;
		
		for(AspectRule aspectRule : aspectRuleMap) {
			AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();

			if(aspectTargetType == AspectTargetType.TRANSLET) {
				Pointcut pointcut = aspectRule.getPointcut();

				if(pointcut != null) {
					List<PointcutPatternRule> pointcutPatternRuleList = pointcut.getPointcutPatternRuleList();
					
					if(pointcutPatternRuleList != null) {
						for(PointcutPatternRule ppr : pointcutPatternRuleList) {
							if(ppr.getTransletNamePattern() != null && ppr.getMatchedTransletCount() == 0) {
								offendingPointcutPatterns++;
								String msg = "Incorrect pointcut pattern of translet name \"" + ppr.getTransletNamePattern() + "\" : aspectRule " + aspectRule;
								if(pointcutPatternVerifiable)
									log.error(msg);
								else
									log.warn(msg);
							}
							if(ppr.getBeanIdPattern() != null && ppr.getMatchedBeanCount() == 0) {
								offendingPointcutPatterns++;
								String msg = "Incorrect pointcut pattern of bean id \"" + ppr.getBeanIdPattern() + "\" : aspectRule " + aspectRule;
								if(pointcutPatternVerifiable)
									log.error(msg);
								else
									log.warn(msg);
							}
							if(ppr.getBeanMethodNamePattern() != null && ppr.getMatchedBeanMethodCount() == 0) {
								offendingPointcutPatterns++;
								String msg = "Incorrect pointcut pattern of bean's method name \"" + ppr.getBeanMethodNamePattern() + "\" : aspectRule " + aspectRule;
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
		
		AspectRuleRegistry aspectRuleRegistry = new AspectRuleRegistry(aspectRuleMap);
		
		AspectAdviceRuleRegistry sessionScopeAspectAdviceRuleRegistry = sessionScopeAspectAdviceRulePostRegister.getAspectAdviceRuleRegistry();
		if(sessionScopeAspectAdviceRuleRegistry != null)
			aspectRuleRegistry.setSessionAspectAdviceRuleRegistry(sessionScopeAspectAdviceRuleRegistry);
		
		return aspectRuleRegistry;
	}
	
	protected ContextBeanRegistry makeContextBeanRegistry(BeanRuleMap beanRuleMap, BeanProxifierType beanProxifierType) {
		beanRuleMap.freeze();
		
		return new ScopedContextBeanRegistry(beanRuleMap, beanProxifierType);
	}

	protected TransletRuleRegistry makeTransletRuleRegistry(TransletRuleMap transletRuleMap) {
		transletRuleMap.freeze();
		
		return new TransletRuleRegistry(transletRuleMap);
	}
	
	protected TemplateRuleRegistry makeTemplateRuleRegistry(TemplateRuleMap templateRuleMap) {
		templateRuleMap.freeze();
		
		return new TemplateRuleRegistry(templateRuleMap);
	}
	
	protected Importable makeImportable(String rootContext) {
		ImportFileType importFileType = rootContext.toLowerCase().endsWith(".apon") ? ImportFileType.APON : ImportFileType.XML;
		return makeImportable(rootContext, importFileType);
	}
	
	protected Importable makeImportable(String rootContext, ImportFileType importFileType) {
		Importable importable = null;

		if(rootContext.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
			String resource = rootContext.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
			importable = new ImportableResource(getClassLoader(), resource, importFileType);
		} else if(rootContext.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
			String filePath = rootContext.substring(ResourceUtils.FILE_URL_PREFIX.length());
			importable = new ImportableFile(filePath, importFileType);
		} else {
			importable = new ImportableFile(getApplicationBasePath(), rootContext, importFileType);
		}
		
		return importable;
	}
	
}
