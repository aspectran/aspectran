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
package com.aspectran.core.context.bean.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.aspect.AspectAdviceRulePostRegister;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.aspect.AspectRuleRegistry;
import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.type.AspectTargetType;
import com.aspectran.core.context.rule.type.JoinpointScopeType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AbstractDynamicBeanProxy.
 */
public abstract class AbstractDynamicBeanProxy {

	protected final Log log = LogFactory.getLog(getClass());

	private static volatile Map<String, RelevantAspectRuleHolder> cache = new WeakHashMap<String, RelevantAspectRuleHolder>();

	private final AspectRuleRegistry aspectRuleRegistry;

	public AbstractDynamicBeanProxy(AspectRuleRegistry aspectRuleRegistry) {
		this.aspectRuleRegistry = aspectRuleRegistry;
	}

	protected AspectAdviceRuleRegistry retrieveAspectAdviceRuleRegistry(
			Activity activity, String transletName, String beanId, String className, String methodName) throws Throwable {
		RelevantAspectRuleHolder holder = getRelevantAspectRuleHolder(transletName, beanId, className, methodName);
		
		if(holder.getActivityAspectRuleList() != null) {
			for(AspectRule aspectRule : holder.getActivityAspectRuleList()) {
				activity.registerAspectRule(aspectRule);
			}
		}
		
		return holder.getAspectAdviceRuleRegistry();
	}
	
	private RelevantAspectRuleHolder getRelevantAspectRuleHolder(
			String transletName, String beanId, String className, String methodName) {

		String patternString = PointcutPatternRule.combinePatternString(transletName, beanId, className, methodName);

		// Check the cache first
		RelevantAspectRuleHolder holder = cache.get(patternString);

		if(holder == null) {
			synchronized(cache) {
				holder = cache.get(patternString);
				if(holder == null) {
					holder = createRelevantAspectRuleHolder(transletName, beanId, className, methodName);
					cache.put(patternString, holder);

					if(log.isDebugEnabled()) {
						log.debug("cache relevantAspectRuleHolder [" + patternString + "] " + holder);
					}
				}
			}
		}
		
		return holder;
	}

	private RelevantAspectRuleHolder createRelevantAspectRuleHolder(String transletName, String beanId, String className, String methodName) {
		Map<String, AspectRule> aspectRuleMap = aspectRuleRegistry.getAspectRuleMap();
		AspectAdviceRulePostRegister postRegister = new AspectAdviceRulePostRegister();
		List<AspectRule> activityAspectRuleList = new ArrayList<AspectRule>();

		for(AspectRule aspectRule : aspectRuleMap.values()) {
			AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();
			if(aspectTargetType == AspectTargetType.TRANSLET && aspectRule.isBeanRelevanted()) {
				Pointcut pointcut = aspectRule.getPointcut();

				if(pointcut == null || pointcut.matches(transletName, beanId, className, methodName)) {
					if(aspectRule.getJoinpointScope() == JoinpointScopeType.BEAN) {
						postRegister.register(aspectRule);
					} else {
						activityAspectRuleList.add(aspectRule);
					}
				}
			}
		}

		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = postRegister.getAspectAdviceRuleRegistry();

		RelevantAspectRuleHolder holder = new RelevantAspectRuleHolder();

		if(aspectAdviceRuleRegistry != null && aspectAdviceRuleRegistry.getAspectRuleCount() > 0)
			holder.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);

		if(!activityAspectRuleList.isEmpty())
			holder.setActivityAspectRuleList(activityAspectRuleList);

		return holder;
	}

}