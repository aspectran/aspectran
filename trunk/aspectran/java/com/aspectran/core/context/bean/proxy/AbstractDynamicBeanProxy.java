/**
 * 
 */
package com.aspectran.core.context.bean.proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.aspect.AspectAdviceRulePostRegister;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.aspect.AspectRuleRegistry;
import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AspectRuleMap;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.type.AspectTargetType;
import com.aspectran.core.context.rule.type.JoinpointScopeType;

/**
 * @author aspectran
 * 
 */
public abstract class AbstractDynamicBeanProxy {

	/** The logger. */
	private static final Logger logger = LoggerFactory.getLogger(AbstractDynamicBeanProxy.class);

	private static final boolean debugEnabled = logger.isDebugEnabled();

	private static final boolean traceEnabled = logger.isTraceEnabled();

	protected ActivityContext context;
	
	protected BeanRule beanRule;

	private AspectRuleRegistry aspectRuleRegistry;
	
	private Map<String, RelevantAspectRuleHolder> relevantAspectRuleHolderCache = new HashMap<String, RelevantAspectRuleHolder>();
	
	protected AbstractDynamicBeanProxy(ActivityContext context, BeanRule beanRule) {
		this.context = context;
		this.aspectRuleRegistry = context.getAspectRuleRegistry();
		this.beanRule = beanRule;
	}

	public Object dynamicInvoke(Object bean, Method method, Object[] args, ProxyMethodInvoker invoker) throws Throwable {
		Activity activity = context.getLocalCoreActivity();
		
		String transletName = activity.getTransletName();
		String beanId = beanRule.getId();
		String methodName = method.getName();

		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = retrieveAspectAdviceRuleRegistry(activity, transletName, beanId, methodName);
		
		if(aspectAdviceRuleRegistry == null) {
			if(invoker != null)
				return invoker.invoke();
			else
				return method.invoke(bean, args);
		}
		
		try {
			try {
				if(traceEnabled) {
					StringBuilder sb = new StringBuilder();
					sb.append("begin method ").append(methodName).append("(");
					for(int i = 0; i < args.length; i++) {
						if(i > 0)
							sb.append(", ");
						sb.append(args[i].toString());
					}
					sb.append(")");
					logger.trace(sb.toString());
				}

				if(aspectAdviceRuleRegistry.getBeforeAdviceRuleList() != null)
					activity.execute(aspectAdviceRuleRegistry.getBeforeAdviceRuleList());
				
				if(activity.isActivityEnded())
					return null;
	
				Object result;
				
				if(invoker != null)
					result = invoker.invoke();
				else
					result = method.invoke(bean, args);

				if(aspectAdviceRuleRegistry.getAfterAdviceRuleList() != null)
					activity.execute(aspectAdviceRuleRegistry.getAfterAdviceRuleList());
				
				if(activity.isActivityEnded())
					return null;
				
				return result;
			} finally {
				if(aspectAdviceRuleRegistry.getFinallyAdviceRuleList() != null)
					activity.forceExecute(aspectAdviceRuleRegistry.getFinallyAdviceRuleList());
				
				if(traceEnabled) {
					logger.trace("end method {}", methodName);
				}
			}
		} catch(Exception e) {
			activity.setRaisedException(e);
			
			List<AspectAdviceRule> exceptionRaizedAdviceRuleList = aspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
			
			if(exceptionRaizedAdviceRuleList != null) {
				activity.responseByContentType(exceptionRaizedAdviceRuleList);
				
				if(activity.isActivityEnded()) {
					return null;
				}
			}
			
			throw e;
		}
	}
	
	protected AspectAdviceRuleRegistry retrieveAspectAdviceRuleRegistry(Activity activity, String transletName, String beanId, String methodName) throws Throwable {
		String patternString = PointcutPatternRule.combinePatternString(transletName, beanId, methodName);
		
		RelevantAspectRuleHolder relevantAspectRuleHolder;
		
		synchronized(relevantAspectRuleHolderCache) {
			relevantAspectRuleHolder = relevantAspectRuleHolderCache.get(patternString);

			if(relevantAspectRuleHolder == null) {
				AspectRuleMap aspectRuleMap = aspectRuleRegistry.getAspectRuleMap();
				AspectAdviceRulePostRegister aspectAdviceRulePostRegister = new AspectAdviceRulePostRegister();
				List<AspectRule> activityAspectRuleList = new ArrayList<AspectRule>();
				
				for(AspectRule aspectRule : aspectRuleMap.values()) {
					AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();
					if(aspectTargetType == AspectTargetType.TRANSLET && !aspectRule.isOnlyTransletRelevanted()) {
						Pointcut pointcut = aspectRule.getPointcut();
						
						if(pointcut == null || pointcut.matches(transletName, beanId, methodName)) {
							if(aspectRule.getJoinpointScope() == JoinpointScopeType.BEAN) {
								aspectAdviceRulePostRegister.register(aspectRule);
							} else {
								activityAspectRuleList.add(aspectRule);
							}
						}
					}
				}
				
				AspectAdviceRuleRegistry aspectAdviceRuleRegistry = aspectAdviceRulePostRegister.getAspectAdviceRuleRegistry();
				
				relevantAspectRuleHolder = new RelevantAspectRuleHolder();
				
				if(aspectAdviceRuleRegistry != null && aspectAdviceRuleRegistry.getAspectRuleCount() > 0)
					relevantAspectRuleHolder.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
				
				if(!activityAspectRuleList.isEmpty())
					relevantAspectRuleHolder.setActivityAspectRuleList(activityAspectRuleList);
				
				relevantAspectRuleHolderCache.put(patternString, relevantAspectRuleHolder);
				
				if(debugEnabled)
					logger.debug("cache relevantAspectRuleHolder \"{}\"", patternString);
			}
		}
		
		if(relevantAspectRuleHolder.getActivityAspectRuleList() != null) {
			for(AspectRule aspectRule: relevantAspectRuleHolder.getActivityAspectRuleList()) {
//				System.out.println("activity.registerAspectRule " + aspectRule);
//				System.out.println("aspectRule.getJoinpointScope() " + aspectRule.getJoinpointScope());
				activity.registerAspectRule(aspectRule);
				
				if(activity.isActivityEnded())
					return null;
			}
		}
		
		return relevantAspectRuleHolder.getAspectAdviceRuleRegistry();
	}

}