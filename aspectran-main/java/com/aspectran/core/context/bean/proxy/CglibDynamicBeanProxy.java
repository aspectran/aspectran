/**
 * 
 */
package com.aspectran.core.context.bean.proxy;

import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

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
public class CglibDynamicBeanProxy implements MethodInterceptor {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(CglibDynamicBeanProxy.class);

	private CoreActivity activity;
	
	private List<AspectRule> aspectRuleList;

	private BeanRule beanRule;
	
	protected CglibDynamicBeanProxy(CoreActivity activity, List<AspectRule> aspectRuleList, BeanRule beanRule) {
		this.activity = activity;
		this.aspectRuleList = aspectRuleList;
		this.beanRule = beanRule;
	}

	public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
		String transletName = activity.getTransletName();
		String beanId = beanRule.getId();
		String methodName = method.getName();

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
		
		if(aspectAdviceRuleRegistry == null || aspectAdviceRuleRegistry.getAspectRuleCount() == 0) {
			return methodProxy.invokeSuper(object, args);
		}
		
		try {
			try {
				if(aspectAdviceRuleRegistry.getBeforeAdviceRuleList() != null)
					activity.execute(aspectAdviceRuleRegistry.getBeforeAdviceRuleList());
				
				if(activity.isActivityEnd())
					return null;
				
				if(logger.isDebugEnabled()) {
					StringBuilder sb = new StringBuilder();
					sb.append("begin method ").append(method.getName()).append("(");
					for(int i = 0; i < args.length; i++) {
						if(i > 0)
							sb.append(",");
						sb.append(" ").append(args[i].toString());
					}
					sb.append(")");
					logger.debug(sb.toString());
				}
	
				Object result = methodProxy.invokeSuper(object, args);
				
				if(aspectAdviceRuleRegistry.getAfterAdviceRuleList() != null)
					activity.execute(aspectAdviceRuleRegistry.getAfterAdviceRuleList());
				
				if(activity.isActivityEnd())
					return null;
				
				return result;
			} finally {
				if(aspectAdviceRuleRegistry.getFinallyAdviceRuleList() != null)
					activity.forceExecute(aspectAdviceRuleRegistry.getFinallyAdviceRuleList());
				
				if(logger.isDebugEnabled()) {
					logger.debug("end method " + method.getName());
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
	
	public static Object newInstance(CoreActivity activity, List<AspectRule> aspectRuleList, BeanRule beanRule, Class<?>[] constructorArgTypes, Object[] constructorArgs) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(beanRule.getBeanClass());
		enhancer.setCallback(new CglibDynamicBeanProxy(activity, aspectRuleList, beanRule));
		Object obj;
		
		if(constructorArgs == null)
			obj = enhancer.create();
		else
			obj = enhancer.create(constructorArgTypes, constructorArgs);
		
		return obj;
	}
}