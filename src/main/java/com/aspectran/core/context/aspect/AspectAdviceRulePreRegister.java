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
import com.aspectran.core.activity.process.action.Executable;
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
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

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
			register(beanRule);
		}
	}
	
	private void register(BeanRule beanRule) {
		for(AspectRule aspectRule : aspectRuleMap) {
			AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();

			if(aspectTargetType == AspectTargetType.TRANSLET && !aspectRule.isOnlyTransletRelevanted()) {
				Pointcut pointcut = aspectRule.getPointcut();
				
				if(pointcut == null || pointcut.exists(null, beanRule.getId())) {
					if(log.isTraceEnabled())
						log.trace("aspectRule " + aspectRule + "\n\t> beanRule " + beanRule);

					beanRule.setProxyMode(true);
					break;
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
			
			if(aspectTargetType == AspectTargetType.TRANSLET && aspectRule.isOnlyTransletRelevanted()) {
				JoinpointScopeType joinpointScope = aspectRule.getJoinpointScope();
				Pointcut pointcut = aspectRule.getPointcut();
				
				if(joinpointScope == JoinpointScopeType.REQUEST) {
					if(pointcut == null || pointcut.matches(transletRule.getName())) {
						RequestRule requestRule = transletRule.getRequestRule();
						
						if(log.isTraceEnabled())
							log.trace("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule + "\n\t> requestRule " + requestRule);
						
						register(requestRule, aspectRule);
					}
				} else if(joinpointScope == JoinpointScopeType.CONTENT) {
					if(pointcut == null || pointcut.matches(transletRule.getName())) {
						ContentList contentList = transletRule.touchContentList();

						if(log.isTraceEnabled())
							log.trace("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule + "\n\t> contentList " + contentList);
						
						register(contentList, aspectRule);
					}
				} else if(joinpointScope == JoinpointScopeType.RESPONSE) {
					if(pointcut == null || pointcut.matches(transletRule.getName())) {
						ResponseRule responseRule = transletRule.getResponseRule();
						
						if(log.isTraceEnabled())
							log.trace("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule + "\n\t> responseRule " + responseRule);
						
						register(responseRule, aspectRule);
					}
				} else {
					//translet scope
					if(pointcut == null || pointcut.matches(transletRule.getName())) {
						if(log.isTraceEnabled())
							log.trace("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule);
						
						register(transletRule, aspectRule);
					}
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
	
	protected void register(Executable action, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = action.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			action.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
}
