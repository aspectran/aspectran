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
import com.aspectran.core.context.rule.type.JoinpointType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AbstractDynamicBeanProxy.
 */
public abstract class AbstractDynamicBeanProxy {

	protected final Log log = LogFactory.getLog(getClass());

	private static final Map<String, RelevantAspectRuleHolder> cache = new WeakHashMap<>();

	private static final RelevantAspectRuleHolder EMPTY_HOLDER = new RelevantAspectRuleHolder();

	private final AspectRuleRegistry aspectRuleRegistry;

	public AbstractDynamicBeanProxy(AspectRuleRegistry aspectRuleRegistry) {
		this.aspectRuleRegistry = aspectRuleRegistry;
	}

	protected AspectAdviceRuleRegistry retrieveAspectAdviceRuleRegistry(Activity activity,
			String transletName, String beanId, String className, String methodName) throws Throwable {
		RelevantAspectRuleHolder holder = getRelevantAspectRuleHolder(transletName, beanId, className, methodName);
		
		if (holder.getDynamicAspectRuleList() != null) {
			for (AspectRule aspectRule : holder.getDynamicAspectRuleList()) {
				// register dynamically
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

		if (holder == null) {
			synchronized(cache) {
				holder = cache.get(patternString);
				if (holder == null) {
					holder = createRelevantAspectRuleHolder(transletName, beanId, className, methodName);
					cache.put(patternString, holder);

					if (log.isDebugEnabled()) {
						log.debug("cache relevantAspectRuleHolder [" + patternString + "] " + holder);
					}
				}
			}
		}
		
		return holder;
	}

	private RelevantAspectRuleHolder createRelevantAspectRuleHolder(
			String transletName, String beanId, String className, String methodName) {
		Map<String, AspectRule> aspectRuleMap = aspectRuleRegistry.getAspectRuleMap();
		AspectAdviceRulePostRegister postRegister = new AspectAdviceRulePostRegister();
		List<AspectRule> dynamicAspectRuleList = new ArrayList<>();

		for (AspectRule aspectRule : aspectRuleMap.values()) {
			if (aspectRule.isBeanRelevanted()) {
				Pointcut pointcut = aspectRule.getPointcut();
				if (pointcut == null || pointcut.matches(transletName, beanId, className, methodName)) {
					if (aspectRule.getJoinpointType() == JoinpointType.BEAN) {
						postRegister.register(aspectRule);
					} else if (aspectRule.getJoinpointType() == JoinpointType.TRANSLET) {
						dynamicAspectRuleList.add(aspectRule);
					}
				}
			}
		}

		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = postRegister.getAspectAdviceRuleRegistry();

		if (!dynamicAspectRuleList.isEmpty() ||
				(aspectAdviceRuleRegistry != null && aspectAdviceRuleRegistry.getAspectRuleCount() > 0)) {
			RelevantAspectRuleHolder holder = new RelevantAspectRuleHolder();
			if (!dynamicAspectRuleList.isEmpty()) {
				holder.setDynamicAspectRuleList(dynamicAspectRuleList);
			} else {
				holder.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
			}
			return holder;
		} else {
			return EMPTY_HOLDER;
		}
	}

}