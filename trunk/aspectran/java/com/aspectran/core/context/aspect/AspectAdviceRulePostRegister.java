package com.aspectran.core.context.aspect;

import com.aspectran.core.var.rule.AspectRule;


public class AspectAdviceRulePostRegister extends AspectAdviceRuleRegister {
	
	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;
	
	public AspectAdviceRuleRegistry register(AspectRule aspectRule) {
		if(aspectAdviceRuleRegistry == null)
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
		
		register(aspectAdviceRuleRegistry, aspectRule);
		
		return aspectAdviceRuleRegistry;
	}
	
	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry() {
		return aspectAdviceRuleRegistry;
	}
	
//	private final Logger logger = LoggerFactory.getLogger(AspectAdviceRulePostRegister.class);
	
//	private AspectRuleMap aspectRuleMap;
//	
//	public AspectAdviceRulePostRegister(AspectRuleMap aspectRuleMap) {
//		this.aspectRuleMap = aspectRuleMap;
//	}
//	
//	private void register(BeanRule beanRule) {
//		for(AspectRule aspectRule : aspectRuleMap) {
//			AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();
//			//JoinpointScopeType joinpointScope = aspectRule.getJoinpointScope();
//			
//			if(aspectTargetType == AspectTargetType.TRANSLET) {
//				Pointcut pointcut = aspectRule.getPointcut();
//			
//				//if(joinpointScope == JoinpointScopeType.BEAN) {
//					if(pointcut == null || pointcut.matches(null, beanRule.getId())) {
//						logger.debug("aspectRule " + aspectRule + "\n\t> beanRule " + beanRule);
//						register(beanRule, aspectRule);
//						beanRule.setProxyMode(true);
//					}
//				//}
//			}
//		}
//	}
	
}
