package com.aspectran.scheduler.quartz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerFactory;

import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.rule.AspectRule;
import com.aspectran.core.rule.AspectRuleMap;
import com.aspectran.core.rule.PointcutRule;
import com.aspectran.core.type.JoinpointTargetType;

public class QuartzScheduler {

	private final Log log = LogFactory.getLog(QuartzScheduler.class);

	private final boolean debugEnabled = log.isDebugEnabled();
	
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
				String schedulerBeanId = aspectRule.getAdviceBeanId();
				PointcutRule pointcutRule = aspectRule.getPointcutRule();
				
				SchedulerFactory schedulerFactory = (SchedulerFactory)context.getBeanRegistry().getBean(schedulerBeanId);
			}
		}
	}
	
}
