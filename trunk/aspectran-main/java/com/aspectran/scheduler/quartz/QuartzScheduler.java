package com.aspectran.scheduler.quartz;

import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.rule.AspectRule;
import com.aspectran.core.rule.AspectRuleMap;
import com.aspectran.core.rule.PointcutRule;
import com.aspectran.core.type.JoinpointTargetType;

public class QuartzScheduler {

	private AspectranContext context;
	
	public QuartzScheduler(AspectranContext context) {
		this.context = context;
		
		AspectRuleMap aspectRuleMap = context.getAspectRuleMap();
		
		if(aspectRuleMap != null)
			register(aspectRuleMap);
	}
	
	private void register(AspectRuleMap aspectRuleMap) {
		for(AspectRule aspectRule : aspectRuleMap) {
			JoinpointTargetType joinpointTarget = aspectRule.getJoinpointTarget();
			
			if(joinpointTarget == JoinpointTargetType.SCHEDULER) {
				String adivceBeanId = aspectRule.getAdviceBeanId();
				PointcutRule pointcutRule = aspectRule.getPointcutRule();
				
				
			}
		}
	}
	
}
