/**
 * 
 */
package com.aspectran.core.context.bean.proxy;

import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.context.ActivityContext;
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
	
	private List<AspectRule> aspectRuleList;

	private BeanRule beanRule;
	
	protected CglibDynamicBeanProxy(List<AspectRule> aspectRuleList, BeanRule beanRule) {
		this.aspectRuleList = aspectRuleList;
		this.beanRule = beanRule;
	}

	public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
		CoreActivity activity = ActivityContext.getCoreActivity();
		
		if(activity == null) {
			throw new RuntimeException("반드시 활동상태에서 proxy 모드 빈을 사용할 수 있음.");
		}

		String transletName = activity.getTransletName();
		String beanId = beanRule.getId();
		String methodName = method.getName();

		JoinpointScopeType joinpointScope = activity.getJoinpointScope();
		
		AspectAdviceRulePostRegister aspectAdviceRulePostRegister = new AspectAdviceRulePostRegister();
		
		for(AspectRule aspectRule : aspectRuleList) {
			Pointcut pointcut = aspectRule.getPointcut();
			
			if(pointcut == null || pointcut.matches(transletName, beanId, methodName)) {
				if(joinpointScope == JoinpointScopeType.BEAN) {
					aspectAdviceRulePostRegister.register(aspectRule);
				} else {
					activity.registerAspectRule(aspectRule);
					
					if(activity.isResponseEnd())
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
				
				if(activity.isResponseEnd())
					return null;
				
				System.out.print("begin method " + method.getName() + "(");
	
				for(int i = 0; i < args.length; i++) {
					if(i > 0)
						System.out.print(",");
					System.out.print(" " + args[i].toString());
				}
	
				System.out.println(" )");
	
				Object result = methodProxy.invokeSuper(object, args);
				
				if(aspectAdviceRuleRegistry.getAfterAdviceRuleList() != null)
					activity.execute(aspectAdviceRuleRegistry.getAfterAdviceRuleList());
				
				if(activity.isResponseEnd())
					return null;
				
				return result;
			} finally {
				if(aspectAdviceRuleRegistry.getFinallyAdviceRuleList() != null)
					activity.execute(aspectAdviceRuleRegistry.getFinallyAdviceRuleList());
				
				System.out.println("end method " + method.getName());
			}
		} catch(Exception e) {
			activity.setRaisedException(e);
			
			List<AspectAdviceRule> exceptionRaizedAdviceRuleList = aspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
			
			if(exceptionRaizedAdviceRuleList != null) {
				activity.responseByContentType(exceptionRaizedAdviceRuleList);
				
				if(activity.isResponseEnd()) {
					return null;
				}
			}
			
			throw e;
		}		
	}
	
	public static Object newInstance(List<AspectRule> aspectRuleList, BeanRule beanRule, Class<?>[] constructorArgTypes, Object[] constructorArgs) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(beanRule.getBeanClass());
		enhancer.setCallback(new CglibDynamicBeanProxy(aspectRuleList, beanRule));
		Object obj;
		
		if(constructorArgs == null)
			obj = enhancer.create();
		else
			obj = enhancer.create(constructorArgTypes, constructorArgs);
		
		return obj;
	}
}