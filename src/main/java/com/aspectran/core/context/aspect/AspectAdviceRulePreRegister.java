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
package com.aspectran.core.context.aspect;

import java.util.List;

import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.context.AspectranConstants;
import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.bean.BeanRuleRegistry;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.AspectTargetType;
import com.aspectran.core.context.rule.type.JoinpointScopeType;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.util.ClassDescriptor;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AspectAdviceRulePreRegister.
 */
public class AspectAdviceRulePreRegister extends AspectAdviceRuleRegister {
	
	private final Log log = LogFactory.getLog(AspectAdviceRulePreRegister.class);
	
	private AspectRuleRegistry aspectRuleRegistry;
	
	public AspectAdviceRulePreRegister(AspectRuleRegistry aspectRuleRegistry) {
		this.aspectRuleRegistry = aspectRuleRegistry;
		
		for(AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
			AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();
			JoinpointScopeType joinpointScope = aspectRule.getJoinpointScope();

			if(aspectTargetType == AspectTargetType.TRANSLET) {
				if(joinpointScope == JoinpointScopeType.BEAN) {
					aspectRule.setBeanRelevanted(true);
				} else if(joinpointScope == JoinpointScopeType.SESSION) {
					aspectRule.setBeanRelevanted(false);
				} else {
					Pointcut pointcut = aspectRule.getPointcut();

					if(pointcut == null) {
						aspectRule.setBeanRelevanted(false);
					} else {
						List<PointcutPatternRule> pointcutPatternRuleList = pointcut.getPointcutPatternRuleList();
						boolean beanRelevanted = false;
						
						for(PointcutPatternRule ppr : pointcutPatternRuleList) {
							if(ppr.getBeanIdPattern() != null || ppr.getMethodNamePattern() != null) {
								beanRelevanted = true;
								break;
							}
						}
						
						aspectRule.setBeanRelevanted(beanRelevanted);
					}
				}
			}
		}
	}
	
	public void register(BeanRuleRegistry beanRuleRegistry) {
		for(BeanRule beanRule : beanRuleRegistry.getIdBasedBeanRuleMap()) {
			if(!beanRule.isOffered()) {
				determineProxyBean(beanRule);
			}
		}
	}
	
	private void determineProxyBean(BeanRule beanRule) {
		for(AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
			AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();

			if(aspectTargetType == AspectTargetType.TRANSLET && aspectRule.isBeanRelevanted()) {
				Pointcut pointcut = aspectRule.getPointcut();
				
				if(pointcut != null && pointcut.isExistsBeanMethodNamePattern()) {
					if(existsMatchedBean(pointcut, beanRule)) {
						beanRule.setProxied(true);
	
						if(log.isTraceEnabled())
							log.trace("apply aspectRule " + aspectRule + " to beanRule " + beanRule);
	
						break;
					}
				} else {
					if(pointcut == null || existsMatchedBean(pointcut, beanRule.getId(), beanRule.getTargetBeanClassName())) {
						beanRule.setProxied(true);
	
						if(log.isTraceEnabled())
							log.trace("apply aspectRule " + aspectRule + " to beanRule " + beanRule);
	
						break;
					}
				}
			}
		}
	}
	
	public void register(TransletRuleRegistry transletRuleRegistry) {
		for(TransletRule transletRule : transletRuleRegistry.getTransletRules()) {
			register(transletRule);
		}
	}
	
	private void register(TransletRule transletRule) {
		for(AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
			AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();
			
			if(aspectTargetType == AspectTargetType.TRANSLET) {
				JoinpointScopeType joinpointScope = aspectRule.getJoinpointScope();
				Pointcut pointcut = aspectRule.getPointcut();

				if(!aspectRule.isBeanRelevanted() && joinpointScope != JoinpointScopeType.SESSION) {
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
			matched = pointcut.patternMatches(pointcutPatternRule.getTransletNamePattern(), transletName, AspectranConstants.TRANSLET_NAME_SEPARATOR);
		}
		
		return matched;
	}
	
	private boolean existsMatchedBean(Pointcut pointcut, String beanId, String className) {
		List<PointcutPatternRule> pointcutPatternRuleList = pointcut.getPointcutPatternRuleList();
		
		if(pointcutPatternRuleList != null) {
			for(PointcutPatternRule ppr : pointcutPatternRuleList) {
				if(existsBean(pointcut, ppr, beanId, className, null)) {
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
			String className = beanRule.getTargetBeanClassName();
			String[] methodNames = cd.getDistinctMethodNames();

			for(PointcutPatternRule ppr : pointcutPatternRuleList) {
				if(existsBean(pointcut, ppr, beanId, className, methodNames)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean existsBean(Pointcut pointcut, PointcutPatternRule pointcutPatternRule, String beanId, String className, String[] methodNames) {
		boolean matched = true;
		
		if(beanId != null && pointcutPatternRule.getBeanIdPattern() != null) {
			matched = pointcut.patternMatches(pointcutPatternRule.getBeanIdPattern(), beanId, AspectranConstants.ID_SEPARATOR);
			if(matched)
				pointcutPatternRule.increaseMatchedBeanCount();
		}
		
		if(matched && className != null && pointcutPatternRule.getClassNamePattern() != null) {
			matched = pointcut.patternMatches(pointcutPatternRule.getClassNamePattern(), className, AspectranConstants.ID_SEPARATOR);
			if(matched)
				pointcutPatternRule.increaseMatchedClassCount();
		}

		if(matched && methodNames != null && pointcutPatternRule.getMethodNamePattern() != null) {
			matched = false;
			for(String methodName : methodNames) {
				boolean matched2 = pointcut.patternMatches(pointcutPatternRule.getMethodNamePattern(), methodName);
				if(matched2) {
					matched = true;
					pointcutPatternRule.increaseMatchedMethodCount();
				}
			}
		}
		
		return matched;
	}
	
}
