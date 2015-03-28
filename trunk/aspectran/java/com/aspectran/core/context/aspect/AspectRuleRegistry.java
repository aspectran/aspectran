package com.aspectran.core.context.aspect;

import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AspectRuleMap;

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
/*
	public List<AspectRule> getBeanRelevantedAspectRuleList(JoinpointScopeType joinpointScope, String transletName, String beanId) {
		return getBeanRelevantedAspectRuleList(joinpointScope, transletName, beanId, null);
	}
	
	public List<AspectRule> getBeanRelevantedAspectRuleList(JoinpointScopeType joinpointScope, String transletName, String beanId, String methodName) {
		List<AspectRule> aspectRuleList = new ArrayList<AspectRule>();
		
		for(AspectRule aspectRule : aspectRuleMap) {
			//Translet에 한정된 것은 제외
			if(aspectRule.getAspectTargetType() == AspectTargetType.TRANSLET && !aspectRule.isOnlyTransletRelevanted()) {
				JoinpointScopeType joinpointScope2 = aspectRule.getJoinpointScope();
				
				if(joinpointScope == null ||
						joinpointScope == JoinpointScopeType.TRANSLET ||
						joinpointScope2 == JoinpointScopeType.BEAN ||
						joinpointScope2 == joinpointScope) {
					Pointcut pointcut = aspectRule.getPointcut();

					System.out.println("********joinpointScope: " + joinpointScope);
					System.out.println("********joinpointScope2: " + joinpointScope2);
					System.out.println("********pointcut: " + pointcut);
					System.out.println("********beanId: " + beanId);
					
					if(pointcut == null || pointcut.matches(transletName, beanId, methodName)) {
						aspectRuleList.add(aspectRule);
					}
				}
			}
		}
		
		return aspectRuleList;
	}
*/
/*
	public AspectAdviceRuleRegistry getBeanRelevantedAspectAdviceRuleRegistry(JoinpointScopeType joinpointScope, String transletName, String beanId) {
		return getBeanRelevantedAspectAdviceRuleRegistry(joinpointScope, transletName, beanId, null);
	}
	
	public AspectAdviceRuleRegistry getBeanRelevantedAspectAdviceRuleRegistry(JoinpointScopeType joinpointScope, String transletName, String beanId, String methodName) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
		      
		for(AspectRule aspectRule : aspectRuleMap) {
			//Translet에 한정된 것은 제외
			if(aspectRule.getAspectTargetType() == AspectTargetType.TRANSLET && !aspectRule.isOnlyTransletRelevanted()) {
				JoinpointScopeType joinpointScope2 = aspectRule.getJoinpointScope();
			
				if(joinpointScope == null ||
						joinpointScope == JoinpointScopeType.TRANSLET ||
						joinpointScope2 == JoinpointScopeType.BEAN ||
						joinpointScope2 == joinpointScope) {
					Pointcut pointcut = aspectRule.getPointcut();
			
					if(pointcut == null || pointcut.matches(transletName, beanId, methodName)) {
						AspectAdviceRuleRegister.register(aspectAdviceRuleRegistry, aspectRule);
					}
				}
			}
		}
		
		return aspectAdviceRuleRegistry;
	}
*/
	public void destroy() {
		if(aspectRuleMap != null) {
			aspectRuleMap.clear();
		}
	}

}
