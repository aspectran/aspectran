package com.aspectran.core.context.aspect;

import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.aspect.pointcut.PointcutFactory;
import com.aspectran.core.context.aspect.pointcut.ReusePointcutFactory;
import com.aspectran.core.rule.AspectRule;
import com.aspectran.core.rule.AspectRuleMap;
import com.aspectran.core.rule.PointcutRule;
import com.aspectran.core.rule.TransletRule;
import com.aspectran.core.rule.TransletRuleMap;
import com.aspectran.core.type.JoinpointTargetType;
import com.aspectran.core.type.PointcutType;

public class AspectAdviceRuleRegister {
	
	private AspectRuleMap aspectRuleMap;
	
	private PointcutFactory pointcutFactory = new ReusePointcutFactory();
	
	public AspectAdviceRuleRegister(AspectRuleMap aspectRuleMap) {
		this.aspectRuleMap = aspectRuleMap;
	}

	public void register(TransletRuleMap transletRuleMap) {
		for(AspectRule aspectRule : aspectRuleMap) {
			JoinpointTargetType  joinpointTarget = aspectRule.getJoinpointTarget();
			PointcutRule pointcutRule = aspectRule.getPointcutRule();
			PointcutType pointcutType = pointcutRule.getPointcutType();
			
			Pointcut pointcut = pointcutFactory.createPointcut(pointcutRule);
			
			
		}
	}
	
	public void register(TransletRule transletRule) {
		for(AspectRule aspectRule : aspectRuleMap) {
			JoinpointTargetType  joinpointTarget = aspectRule.getJoinpointTarget();
			PointcutRule pointcutRule = aspectRule.getPointcutRule();

			Pointcut pointcut = pointcutFactory.createPointcut(pointcutRule);
			
			if(joinpointTarget == JoinpointTargetType.TRANSLET) {
				if(pointcut.matches(transletRule.getName())) {
					
				}
			} else if(joinpointTarget == JoinpointTargetType.REQUEST) {
				
			} else if(joinpointTarget == JoinpointTargetType.RESPONSE) {
				
			} else if(joinpointTarget == JoinpointTargetType.ACTION) {
				
			}
			
			
		}
		
	}
	
	public void register(AspectRule aspectRule, TransletRule transletRule) {
		JoinpointTargetType  joinpointTarget = aspectRule.getJoinpointTarget();
		PointcutRule pointcutRule = aspectRule.getPointcutRule();

		Pointcut pointcut = pointcutFactory.createPointcut(pointcutRule);
		
		if(joinpointTarget == JoinpointTargetType.TRANSLET) {
			if(pointcut.matches(transletRule.getName())) {
				
			}
		} else if(joinpointTarget == JoinpointTargetType.REQUEST) {
			
		} else if(joinpointTarget == JoinpointTargetType.RESPONSE) {
			
		} else if(joinpointTarget == JoinpointTargetType.ACTION) {
			
		}
	}
	
}
