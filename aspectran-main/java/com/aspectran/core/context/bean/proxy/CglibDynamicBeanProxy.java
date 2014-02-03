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
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.aspect.DynamicAspectAdviceRuleRegistry;
import com.aspectran.core.var.rule.AspectAdviceRule;
import com.aspectran.core.var.rule.BeanRule;

/**
 * @author aspectran
 * 
 */
public class CglibDynamicBeanProxy implements MethodInterceptor {

	private BeanRule beanRule;
	
	private Object obj;

	protected CglibDynamicBeanProxy(BeanRule beanRule, Object obj) {
		this.beanRule = beanRule;
		this.obj = obj;
	}

	public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
		CoreActivity activity = beanRule.getLocalActivity();
		
		if(activity == null) {
			throw new RuntimeException("반드시 활동상태에서 proxy 모드 빈을 사용할 수 있음.");
		}
		
		String transletName = activity == null ? null : activity.getTransletName();
		String beanId = beanRule.getId();
		String methodName = method.getName();
		
		DynamicAspectAdviceRuleRegistry dynamicAspectAdviceRuleRegistry = beanRule.getDynamicAspectAdviceRuleRegistry();
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = dynamicAspectAdviceRuleRegistry.getMatchAspectAdviceRuleRegistry(transletName, beanId, methodName);
		
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
		} finally {
			if(aspectAdviceRuleRegistry.getFinallyAdviceRuleList() != null)
				activity.execute(aspectAdviceRuleRegistry.getFinallyAdviceRuleList());
			
			System.out.println("end method " + method.getName());
		}
		
	}

	public static Object newInstance(BeanRule beanRule, Object obj) {
		return Enhancer.create(beanRule.getBeanClass(), new CglibDynamicBeanProxy(beanRule, obj));
	}
}