package com.aspectran.core.context.aspect;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.var.rule.AspectRule;
import com.aspectran.core.var.rule.AspectRuleMap;
import com.aspectran.core.var.type.AspectTargetType;
import com.aspectran.core.var.type.JoinpointScopeType;

public class AspectRuleRegistry {

	private final AspectRuleMap aspectRuleMap;
	
	public AspectRuleRegistry(AspectRuleMap aspectRuleMap) {
		this.aspectRuleMap = aspectRuleMap;
	}
	
	/**
	 * Gets the aspect rule map.
	 * 
	 * @return the aspect rule map
	 */
	public AspectRuleMap getAspectRuleMap() {
		return aspectRuleMap;
	}

	public boolean contains(String aspectId) {
		return aspectRuleMap.containsKey(aspectId);
	}
	
	public AspectRule getAspectRule(String aspectId) {
		return aspectRuleMap.get(aspectId);
	}
	
	public void destroy() {
		if(aspectRuleMap != null) {
			aspectRuleMap.clear();
		}
	}

	public List<AspectRule> getAspectRuleList(JoinpointScopeType joinpointScope, String transletName, String beanId) {
		return getAspectRuleList(joinpointScope, transletName, null);
	}
	
	public List<AspectRule> getAspectRuleList(JoinpointScopeType joinpointScope, String transletName, String beanId, String methodName) {
		List<AspectRule> aspectRuleList = new ArrayList<AspectRule>();
		
		for(AspectRule aspectRule : aspectRuleMap) {
			AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();
			JoinpointScopeType joinpointScope2 = aspectRule.getJoinpointScope();
			
			if(aspectTargetType == AspectTargetType.TRANSLET) {
				if(joinpointScope == null || joinpointScope == JoinpointScopeType.TRANSLET || joinpointScope == JoinpointScopeType.BEAN || joinpointScope == joinpointScope2) {
					Pointcut pointcut = aspectRule.getPointcut();
			
					if(pointcut == null || pointcut.matches(null, beanId, methodName)) {
						aspectRuleList.add(aspectRule);
					}
				}
			}
		}
		
		return aspectRuleList;
	}
	
	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry(JoinpointScopeType joinpointScope, String transletName, String beanId) {
		return getAspectAdviceRuleRegistry(joinpointScope, transletName, beanId, null);
	}
	
	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry(JoinpointScopeType joinpointScope, String transletName, String beanId, String methodName) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
		      
		for(AspectRule aspectRule : aspectRuleMap) {
			AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();
			JoinpointScopeType joinpointScope2 = aspectRule.getJoinpointScope();
			
			if(aspectTargetType == AspectTargetType.TRANSLET) {
				if(joinpointScope == null || joinpointScope == JoinpointScopeType.TRANSLET || joinpointScope == JoinpointScopeType.BEAN || joinpointScope == joinpointScope2) {
					Pointcut pointcut = aspectRule.getPointcut();
			
					if(pointcut == null || pointcut.matches(null, beanId, methodName)) {
						AspectAdviceRuleRegister.register(aspectAdviceRuleRegistry, aspectRule);
					}
				}
			}
		}
		
		return aspectAdviceRuleRegistry;
	}

}
