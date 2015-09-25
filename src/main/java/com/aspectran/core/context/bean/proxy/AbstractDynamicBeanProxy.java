/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.bean.proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.aspect.AspectAdviceRulePostRegister;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.aspect.AspectRuleRegistry;
import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AspectRuleMap;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ExceptionHandlingRule;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.type.AspectTargetType;
import com.aspectran.core.context.rule.type.JoinpointScopeType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AbstractDynamicBeanProxy.
 *
 * @author aspectran
 */
public abstract class AbstractDynamicBeanProxy {

	/** The log. */
	private static final Log log = LogFactory.getLog(AbstractDynamicBeanProxy.class);

	private static final boolean debugEnabled = log.isDebugEnabled();

	private static final boolean traceEnabled = log.isTraceEnabled();

	protected ActivityContext context;
	
	protected BeanRule beanRule;

	private AspectRuleRegistry aspectRuleRegistry;
	
	private Map<String, RelevantAspectRuleHolder> relevantAspectRuleHolderCache = new ConcurrentHashMap<String, RelevantAspectRuleHolder>();
	
	protected AbstractDynamicBeanProxy(ActivityContext context, BeanRule beanRule) {
		this.context = context;
		this.aspectRuleRegistry = context.getAspectRuleRegistry();
		this.beanRule = beanRule;
	}

	public Object dynamicInvoke(Object bean, Method method, Object[] args, ProxyMethodInvoker invoker) throws Throwable {
		Activity activity = context.getCurrentActivity();
		
		String transletName = activity.getTransletName();
		String beanId = beanRule.getId();
		String beanMethodName = method.getName();
		
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = retrieveAspectAdviceRuleRegistry(activity, transletName, beanId, beanMethodName);
		
		if(aspectAdviceRuleRegistry == null) {
			if(invoker != null)
				return invoker.invoke();
			else
				return method.invoke(bean, args);
		}
		
		try {
			try {
				if(traceEnabled) {
					StringBuilder sb = new StringBuilder();
					sb.append("begin method ").append(beanMethodName).append("(");
					for(int i = 0; i < args.length; i++) {
						if(i > 0)
							sb.append(", ");
						sb.append(args[i].toString());
					}
					sb.append(")");
					log.trace(sb.toString());
				}
				
				if(aspectAdviceRuleRegistry.getBeforeAdviceRuleList() != null)
					activity.execute(aspectAdviceRuleRegistry.getBeforeAdviceRuleList());
				
				Object result;

				if(!activity.isActivityEnded()) {
					if(invoker != null)
						result = invoker.invoke();
					else
						result = method.invoke(bean, args);
				} else {
					result = null;
				}

				if(aspectAdviceRuleRegistry.getAfterAdviceRuleList() != null)
					activity.execute(aspectAdviceRuleRegistry.getAfterAdviceRuleList());
				
				return result;
			} finally {
				if(aspectAdviceRuleRegistry.getFinallyAdviceRuleList() != null)
					activity.forceExecute(aspectAdviceRuleRegistry.getFinallyAdviceRuleList());
				
				if(traceEnabled) {
					log.trace("end method " + beanMethodName);
				}
			}
		} catch(Exception e) {
			activity.setRaisedException(e);
			
			List<ExceptionHandlingRule> exceptionHandlingRuleList = aspectAdviceRuleRegistry.getExceptionHandlingRuleList();
			
			if(exceptionHandlingRuleList != null) {
				activity.responseByContentType(exceptionHandlingRuleList);
				
				if(activity.isActivityEnded()) {
					return null;
				}
			}
			
			throw e;
		}
	}
	
	protected AspectAdviceRuleRegistry retrieveAspectAdviceRuleRegistry(Activity activity, String transletName, String beanId, String beanMethodName) throws Throwable {
		String patternString = PointcutPatternRule.combinePatternString(transletName, beanId, beanMethodName);
		
		RelevantAspectRuleHolder relevantAspectRuleHolder;
		
		synchronized(relevantAspectRuleHolderCache) {
			// Check the cache first
			relevantAspectRuleHolder = relevantAspectRuleHolderCache.get(patternString);

			if(relevantAspectRuleHolder == null) {
				AspectRuleMap aspectRuleMap = aspectRuleRegistry.getAspectRuleMap();
				AspectAdviceRulePostRegister aspectAdviceRulePostRegister = new AspectAdviceRulePostRegister();
				List<AspectRule> activityAspectRuleList = new ArrayList<AspectRule>();
				
				for(AspectRule aspectRule : aspectRuleMap.values()) {
					AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();
					if(aspectTargetType == AspectTargetType.TRANSLET && !aspectRule.isOnlyTransletRelevanted()) {
						Pointcut pointcut = aspectRule.getPointcut();

						if(pointcut == null || pointcut.matches(transletName, beanId, beanMethodName)) {
							if(aspectRule.getJoinpointScope() == JoinpointScopeType.BEAN) {
								aspectAdviceRulePostRegister.register(aspectRule);
							} else {
								activityAspectRuleList.add(aspectRule);
							}
						}
					}
				}
				
				AspectAdviceRuleRegistry aspectAdviceRuleRegistry = aspectAdviceRulePostRegister.getAspectAdviceRuleRegistry();
				
				relevantAspectRuleHolder = new RelevantAspectRuleHolder();
				
				if(aspectAdviceRuleRegistry != null && aspectAdviceRuleRegistry.getAspectRuleCount() > 0)
					relevantAspectRuleHolder.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
				
				if(!activityAspectRuleList.isEmpty())
					relevantAspectRuleHolder.setActivityAspectRuleList(activityAspectRuleList);
				
				relevantAspectRuleHolderCache.put(patternString, relevantAspectRuleHolder);
				
				if(debugEnabled)
					log.debug("cache relevantAspectRuleHolder \"" + patternString + "\"");
			}
		}
		
		if(relevantAspectRuleHolder.getActivityAspectRuleList() != null) {
			for(AspectRule aspectRule : relevantAspectRuleHolder.getActivityAspectRuleList()) {
				activity.registerAspectRule(aspectRule);
			}
		}
		
		return relevantAspectRuleHolder.getAspectAdviceRuleRegistry();
	}

}