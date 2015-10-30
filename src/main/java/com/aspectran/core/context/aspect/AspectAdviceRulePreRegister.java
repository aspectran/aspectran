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
package com.aspectran.core.context.aspect;

import java.util.List;

import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AspectRuleMap;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.BeanRuleMap;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.TransletRuleMap;
import com.aspectran.core.context.rule.type.AspectTargetType;
import com.aspectran.core.context.rule.type.JoinpointScopeType;
import com.aspectran.core.context.rule.type.PointcutType;
import com.aspectran.core.util.ClassDescriptor;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.wildcard.WildcardPattern;

/**
 * The Class AspectAdviceRulePreRegister.
 */
public class AspectAdviceRulePreRegister extends AspectAdviceRuleRegister {
	
	private final Log log = LogFactory.getLog(AspectAdviceRulePreRegister.class);
	
	private AspectRuleMap aspectRuleMap;
	
	public AspectAdviceRulePreRegister(AspectRuleMap aspectRuleMap) {
		this.aspectRuleMap = aspectRuleMap;
		
		for(AspectRule aspectRule : aspectRuleMap) {
			AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();
			JoinpointScopeType joinpointScope = aspectRule.getJoinpointScope();
			Pointcut pointcut = aspectRule.getPointcut();

			if(aspectTargetType == AspectTargetType.TRANSLET) {
				if(joinpointScope == JoinpointScopeType.TRANSLET ||
						joinpointScope == JoinpointScopeType.REQUEST ||
						joinpointScope == JoinpointScopeType.CONTENT ||
						joinpointScope == JoinpointScopeType.RESPONSE) {
					if(pointcut == null) {
						aspectRule.setOnlyTransletRelevanted(true);
					} else {
						List<PointcutPatternRule> pointcutPatternRuleList = pointcut.getPointcutPatternRuleList();
						boolean onlyTransletRelevanted = true;
						
						for(PointcutPatternRule ppr : pointcutPatternRuleList) {
							if(ppr.getBeanIdPattern() != null || ppr.getBeanMethodNamePattern() != null) {
								onlyTransletRelevanted = false;
								break;
							}
						}
						
						aspectRule.setOnlyTransletRelevanted(onlyTransletRelevanted);
					}
				}
			}
		}
	}
	
	public void register(BeanRuleMap beanRuleMap) {
		for(BeanRule beanRule : beanRuleMap) {
			determineProxyBean(beanRule);
		}
	}
	
	private void determineProxyBean(BeanRule beanRule) {
		for(AspectRule aspectRule : aspectRuleMap) {
			AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();

			if(aspectTargetType == AspectTargetType.TRANSLET && !aspectRule.isOnlyTransletRelevanted()) {
				Pointcut pointcut = aspectRule.getPointcut();
				
				if(pointcut != null && pointcut.isExistsBeanMethodNamePattern()) {
					if(existsMatchedBean(pointcut, beanRule)) {
						beanRule.setProxied(true);
	
						if(log.isTraceEnabled())
							log.trace("apply aspectRule " + aspectRule + " to beanRule " + beanRule);
	
						break;
					}
				} else {
					if(pointcut == null || existsMatchedBean(pointcut, beanRule.getId())) {
						beanRule.setProxied(true);
	
						if(log.isTraceEnabled())
							log.trace("apply aspectRule " + aspectRule + " to beanRule " + beanRule);
	
						break;
					}
				}
			}
		}
	}
	
	public void register(TransletRuleMap transletRuleMap) {
		for(TransletRule transletRule : transletRuleMap) {
			register(transletRule);
		}
	}
	
	private void register(TransletRule transletRule) {
		for(AspectRule aspectRule : aspectRuleMap) {
			AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();
			
			if(aspectTargetType == AspectTargetType.TRANSLET) {
				Pointcut pointcut = aspectRule.getPointcut();

				if(aspectRule.isOnlyTransletRelevanted()) {
					JoinpointScopeType joinpointScope = aspectRule.getJoinpointScope();
					
					if(joinpointScope == JoinpointScopeType.REQUEST) {
						if(pointcut == null || pointcut.matches(transletRule.getName())) {
							RequestRule requestRule = transletRule.getRequestRule();
							
							if(log.isTraceEnabled())
								log.trace("apply aspectRule " + aspectRule + " to transletRule " + transletRule + " requestRule " + requestRule);
							
							register(requestRule, aspectRule);
						}
					} else if(joinpointScope == JoinpointScopeType.CONTENT) {
						if(pointcut == null || pointcut.matches(transletRule.getName())) {
							ContentList contentList = transletRule.touchContentList();
	
							if(log.isTraceEnabled())
								log.trace("apply aspectRule " + aspectRule + " to transletRule " + transletRule + " contentList " + contentList);
							
							register(contentList, aspectRule);
						}
					} else if(joinpointScope == JoinpointScopeType.RESPONSE) {
						if(pointcut == null || pointcut.matches(transletRule.getName())) {
							ResponseRule responseRule = transletRule.getResponseRule();
							
							if(log.isTraceEnabled())
								log.trace("apply aspectRule " + aspectRule + " to transletRule " + transletRule + " responseRule " + responseRule);
							
							register(responseRule, aspectRule);
						}
					} else {
						//translet scope
						if(pointcut == null || pointcut.matches(transletRule.getName())) {
							if(log.isTraceEnabled())
								log.trace("apply aspectRule " + aspectRule + " to transletRule " + transletRule);
							
							register(transletRule, aspectRule);
						}
					}
				}
				
				if(pointcut != null) {
					countMatchedTranslet(pointcut, transletRule.getName());
				}
			}
		}
	}

	protected void register(TransletRule transletRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = transletRule.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			transletRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	protected void register(RequestRule requestRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = requestRule.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			requestRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	protected void register(ContentList contentList, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = contentList.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			contentList.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		if(aspectRule != null)
			register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	protected void register(ResponseRule responseRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = responseRule.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			responseRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
/*	
	protected void register(Executable action, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = action.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			action.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
*/
	private void countMatchedTranslet(Pointcut pointcut, String transletName) {
		List<PointcutPatternRule> pointcutPatternRuleList = pointcut.getPointcutPatternRuleList();
		
		if(pointcutPatternRuleList != null) {
			for(PointcutPatternRule ppr : pointcutPatternRuleList) {
				if(existsMatchedTranslet(pointcut, ppr, transletName)) {
					ppr.increaseMatchedTransletCount();
				}
			}
		}
	}
	
	private boolean existsMatchedTranslet(Pointcut pointcut, PointcutPatternRule pointcutPatternRule, String transletName) {
		boolean matched = true;
		
		if(pointcutPatternRule.getTransletNamePattern() != null) {
			matched = pointcut.patternMatches(pointcutPatternRule.getTransletNamePattern(), transletName, AspectranConstant.TRANSLET_NAME_SEPARATOR);
		}
		
		return matched;
	}
	
	private boolean existsMatchedBean(Pointcut pointcut, String beanId) {
		List<PointcutPatternRule> pointcutPatternRuleList = pointcut.getPointcutPatternRuleList();
		
		if(pointcutPatternRuleList != null) {
			for(PointcutPatternRule ppr : pointcutPatternRuleList) {
				if(existsBean(pointcut, ppr, beanId, null)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean existsMatchedBean(Pointcut pointcut, BeanRule beanRule) {
		List<PointcutPatternRule> pointcutPatternRuleList = pointcut.getPointcutPatternRuleList();
		
		if(pointcutPatternRuleList != null) {
			ClassDescriptor cd = ClassDescriptor.getInstance(beanRule.getBeanClass());
			
			String beanId = beanRule.getId();
			String[] beanMethodNames = cd.getDistinctMethodNames();

			for(PointcutPatternRule ppr : pointcutPatternRuleList) {
				if(existsBean(pointcut, ppr, beanId, beanMethodNames)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean existsBean(Pointcut pointcut, PointcutPatternRule pointcutPatternRule, String beanId, String[] beanMethodNames) {
		boolean matched = true;
		
		if(beanId != null && pointcutPatternRule.getBeanIdPattern() != null) {
			matched = pointcut.patternMatches(pointcutPatternRule.getBeanIdPattern(), beanId, AspectranConstant.ID_SEPARATOR);
			if(matched)
				pointcutPatternRule.increaseMatchedBeanCount();
		}
		
		if(beanMethodNames != null) {
			if(matched && pointcutPatternRule.getBeanMethodNamePattern() != null) {
				if(pointcutPatternRule.getPointcutType() == PointcutType.WILDCARD) {
					boolean hasWildcards = WildcardPattern.hasWildcards(pointcutPatternRule.getBeanMethodNamePattern());
					
					if(hasWildcards) {
						matched = false;
						for(String beanMethodName : beanMethodNames) {
							boolean matched2 = pointcut.patternMatches(pointcutPatternRule.getBeanMethodNamePattern(), beanMethodName);
							if(matched2) {
								matched = true;
								pointcutPatternRule.increaseMatchedBeanMethodCount();
							}
						}
					} else {
						matched = false;
						for(String beanMethodName : beanMethodNames) {
							boolean matched2 = pointcut.patternMatches(pointcutPatternRule.getBeanMethodNamePattern(), beanMethodName);
							if(matched2) {
								matched = true;
								pointcutPatternRule.increaseMatchedBeanMethodCount();
								break;
							}
						}
					}
				} else {
					matched = false;
					for(String beanMethodName : beanMethodNames) {
						boolean matched2 = pointcut.patternMatches(pointcutPatternRule.getBeanMethodNamePattern(), beanMethodName);
						if(matched2) {
							matched = true;
							pointcutPatternRule.increaseMatchedBeanMethodCount();
						}
					}
				}
			}
		}
		
		return matched;
	}
	
}
