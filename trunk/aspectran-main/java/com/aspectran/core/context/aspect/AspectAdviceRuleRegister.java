package com.aspectran.core.context.aspect;

import java.util.List;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.aspect.pointcut.PointcutFactory;
import com.aspectran.core.context.aspect.pointcut.ReusePointcutFactory;
import com.aspectran.core.rule.AspectAdviceRule;
import com.aspectran.core.rule.AspectRule;
import com.aspectran.core.rule.AspectRuleMap;
import com.aspectran.core.rule.PointcutRule;
import com.aspectran.core.rule.RequestRule;
import com.aspectran.core.rule.ResponseRule;
import com.aspectran.core.rule.SettingsAdviceRule;
import com.aspectran.core.rule.TransletRule;
import com.aspectran.core.rule.TransletRuleMap;
import com.aspectran.core.type.JoinpointScopeType;
import com.aspectran.core.type.JoinpointTargetType;

public class AspectAdviceRuleRegister {
	
	private AspectRuleMap aspectRuleMap;
	
	private PointcutFactory pointcutFactory = new ReusePointcutFactory();
	
	public AspectAdviceRuleRegister(AspectRuleMap aspectRuleMap) {
		this.aspectRuleMap = aspectRuleMap;
	}

	public void register(TransletRuleMap transletRuleMap) {
		for(TransletRule transletRule : transletRuleMap) {
			register(transletRule);
		}
	}
	
	private void register(TransletRule transletRule) {
		for(AspectRule aspectRule : aspectRuleMap) {
			JoinpointTargetType joinpointTarget = aspectRule.getJoinpointTarget();
			JoinpointScopeType joinpointScope = aspectRule.getJoinpointScope();
			PointcutRule pointcutRule = aspectRule.getPointcutRule();

			Pointcut pointcut = null;
			if(pointcutRule != null)
				pointcut = pointcutFactory.createPointcut(pointcutRule);
			
			if(joinpointTarget == JoinpointTargetType.TRANSLET) {
				if(joinpointScope == JoinpointScopeType.REQUEST) {
					if(pointcut == null || pointcut.matches(transletRule.getName())) {
						register(transletRule.getRequestRule(), aspectRule);
					}
				} else if(joinpointScope == JoinpointScopeType.CONTENT) {
					if(pointcut == null || pointcut.matches(transletRule.getName())) {
						ContentList contentList = transletRule.getContentList();
						
						if(contentList == null) {
							contentList = new ContentList();
							transletRule.setContentList(contentList);
						}

						register(contentList, aspectRule);
					}
				} else if(joinpointScope == JoinpointScopeType.RESPONSE) {
					if(pointcut == null || pointcut.matches(transletRule.getName())) {
						register(transletRule.getResponseRule(), aspectRule);
					}
				} else if(joinpointScope == JoinpointScopeType.ACTION) {
					ContentList contentList = transletRule.getContentList();
					
					if(contentList != null) {
						for(ActionList actionList : contentList) {
							for(Executable action : actionList) {
								if(pointcut == null || pointcut.matches(transletRule.getName(), action.getFullActionId())) {
									register(action, aspectRule);
								}
							}
						}
					}
				} else { //translet scope
					if(pointcut == null || pointcut.matches(transletRule.getName())) {
						register(transletRule, aspectRule);
					}
				}
			}			
		}
	}
	
	private void register(TransletRule transletRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = transletRule.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			transletRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	private void register(RequestRule requestRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = requestRule.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			requestRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	private void register(ContentList contentList, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = contentList.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			contentList.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	private void register(ResponseRule responseRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = responseRule.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			responseRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	private void register(Executable action, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = action.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			action.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	private void register(AspectAdviceRuleRegistry aspectAdviceRuleRegistry, AspectRule aspectRule) {
		SettingsAdviceRule settingsAdviceRule = aspectRule.getSettingsAdviceRule();
		List<AspectAdviceRule> aspectAdviceRuleList = aspectRule.getAspectAdviceRuleList();
		
		if(settingsAdviceRule != null)
			aspectAdviceRuleRegistry.addAspectAdviceRule(settingsAdviceRule);
		
		if(aspectAdviceRuleList != null) {
			for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
				aspectAdviceRuleRegistry.addAspectAdviceRule(aspectAdviceRule);
			}
		}
	}
	
}
