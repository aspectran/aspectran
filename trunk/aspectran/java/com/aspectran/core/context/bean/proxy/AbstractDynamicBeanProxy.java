/**
 * 
 */
package com.aspectran.core.context.bean.proxy;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.context.aspect.AspectAdviceRulePostRegister;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.var.rule.AspectAdviceRule;
import com.aspectran.core.var.rule.AspectRule;
import com.aspectran.core.var.rule.BeanRule;
import com.aspectran.core.var.type.JoinpointScopeType;

/**
 * @author aspectran
 * 
 */
public abstract class AbstractDynamicBeanProxy {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(AbstractDynamicBeanProxy.class);

	protected CoreActivity activity;
	
	protected List<AspectRule> aspectRuleList;

	protected BeanRule beanRule;
	
	protected AbstractDynamicBeanProxy(CoreActivity activity, List<AspectRule> aspectRuleList, BeanRule beanRule) {
		this.activity = activity;
		this.aspectRuleList = aspectRuleList;
		this.beanRule = beanRule;
	}

	public Object dynamicInvoke(Object object, Method method, Object[] args, ProxyMethodInvoker invoker) throws Throwable {
		String methodName = method.getName();

		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = getAspectAdviceRuleRegistry(methodName);
		
		if(aspectAdviceRuleRegistry == null) {
			if(invoker != null)
				return invoker.invoke();
			else
				return method.invoke(object, args);
		}
		
		try {
			try {
				if(aspectAdviceRuleRegistry.getBeforeAdviceRuleList() != null)
					activity.execute(aspectAdviceRuleRegistry.getBeforeAdviceRuleList());
				
				if(activity.isActivityEnd())
					return null;
				
				if(logger.isDebugEnabled()) {
					StringBuilder sb = new StringBuilder();
					sb.append("begin method ").append(methodName).append("(");
					for(int i = 0; i < args.length; i++) {
						if(i > 0)
							sb.append(",");
						sb.append(" ").append(args[i].toString());
					}
					sb.append(")");
					logger.debug(sb.toString());
				}
	
				Object result;
				
				if(invoker != null)
					result = invoker.invoke();
				else
					result = method.invoke(object, args);

				if(aspectAdviceRuleRegistry.getAfterAdviceRuleList() != null)
					activity.execute(aspectAdviceRuleRegistry.getAfterAdviceRuleList());
				
				if(activity.isActivityEnd())
					return null;
				
				return result;
			} finally {
				if(aspectAdviceRuleRegistry.getFinallyAdviceRuleList() != null)
					activity.forceExecute(aspectAdviceRuleRegistry.getFinallyAdviceRuleList());
				
				if(logger.isDebugEnabled()) {
					logger.debug("end method " + methodName);
				}
			}
		} catch(Exception e) {
			activity.setRaisedException(e);
			
			List<AspectAdviceRule> exceptionRaizedAdviceRuleList = aspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
			
			if(exceptionRaizedAdviceRuleList != null) {
				activity.responseByContentType(exceptionRaizedAdviceRuleList);
				
				if(activity.isActivityEnd()) {
					return null;
				}
			}
			
			throw e;
		}
	}
	
	protected AspectAdviceRuleRegistry getAspectAdviceRuleRegistry(String methodName) throws Throwable {
		String transletName = activity.getTransletName();
		String beanId = beanRule.getId();

		AspectAdviceRulePostRegister aspectAdviceRulePostRegister = new AspectAdviceRulePostRegister();
		
		for(AspectRule aspectRule : aspectRuleList) {
			Pointcut pointcut = aspectRule.getPointcut();
			
			if(pointcut == null || pointcut.matches(transletName, beanId, methodName)) {
				if(aspectRule.getJoinpointScope() == JoinpointScopeType.BEAN) {
					aspectAdviceRulePostRegister.register(aspectRule);
				} else {
					activity.registerAspectRule(aspectRule);
					
					if(activity.isActivityEnd())
						return null;
				}
			}
		}
		
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = aspectAdviceRulePostRegister.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry != null && aspectAdviceRuleRegistry.getAspectRuleCount() > 0)
			return aspectAdviceRuleRegistry;
		else
			return null;
	}

}