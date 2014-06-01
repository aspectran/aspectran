package com.aspectran.core.context.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.aspect.pointcut.PointcutPattern;
import com.aspectran.core.context.bean.proxy.CglibDynamicBeanProxy;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.ReflectionUtils;
import com.aspectran.core.var.ValueMap;
import com.aspectran.core.var.rule.AspectRule;
import com.aspectran.core.var.rule.BeanRule;
import com.aspectran.core.var.rule.BeanRuleMap;
import com.aspectran.core.var.rule.ItemRule;
import com.aspectran.core.var.rule.ItemRuleMap;
import com.aspectran.core.var.token.ItemTokenExpressor;
import com.aspectran.core.var.type.BeanProxyModeType;
import com.aspectran.core.var.type.JoinpointScopeType;
import com.aspectran.core.var.type.ScopeType;

/**
 * <p>
 * Created: 2009. 03. 09 오후 23:48:09
 * </p>
 */
public abstract class AbstractBeanRegistry {

	private Map<String, List<AspectRule>> aspectRuleCache = new HashMap<String, List<AspectRule>>();
	
	protected final BeanRuleMap beanRuleMap;
	
	private BeanProxyModeType beanProxyMode;
	
	protected AbstractBeanRegistry(BeanRuleMap beanRuleMap) {
		this.beanRuleMap = beanRuleMap;
		
		for(BeanRule beanRule : beanRuleMap) {
			ScopeType scope = beanRule.getScopeType();

			if(scope == ScopeType.SINGLETON) {
				if(!beanRule.isRegistered() && !beanRule.isLazyInit()) {
					Object bean = createBean(beanRule);

					beanRule.setBean(bean);
					beanRule.setRegistered(true);
				}
			}
		}
	}
	
	abstract protected Object createBean(BeanRule beanRule);
	
	protected Object createBean(BeanRule beanRule, ItemTokenExpressor expressor) {
		try {
			ItemRuleMap constructorArgumentItemRuleMap = beanRule.getConstructorArgumentItemRuleMap();
			
			if(constructorArgumentItemRuleMap != null) {
				ValueMap valueMap = expressor.express(constructorArgumentItemRuleMap);
	
				int parameterSize = constructorArgumentItemRuleMap.size();
				Object[] args = new Object[parameterSize];
				Class<?>[] argTypes = new Class<?>[args.length];
				
				Iterator<ItemRule> iter = constructorArgumentItemRuleMap.iterator();
				int i = 0;
				
				while(iter.hasNext()) {
					ItemRule ir = iter.next();
					Object o = valueMap.get(ir.getName());
					args[i] = o;
					argTypes[i] = o.getClass();
					
					i++;
				}
				
				return createBean(beanRule, argTypes, args);
			} else {
				return createBean(beanRule, null, null);
			}
		} catch(Exception e) {
			throw new BeanCreationException(beanRule, e);
		}
	}
	
	private Object createBean(BeanRule beanRule, Class<?>[] argTypes, Object[] args) {
		CoreActivity activity = ActivityContext.getCoreActivity();
		
		/*
		 * 0. DynamicProxy 빈을 만들 것인지를 먼저 결정하라. 
		 * 1. 빈과 관련된 AspectRule을 추출하고, 캐슁하라.
		 * 2. 추출된 AspectRule을 AspectAdviceRegistry로 변환하고, DynamicProxy에 넘겨라
		 * 3. DynamicProxy에서 현재 실행 시점의 JoinScope에 따라 해당 JoinScope의 AspectAdviceRegistry에 Advice를 등록하라.
		 * 4. DynamicProxy에서 일치하는 메쏘드를 가진 AspectRule의 Advice를 실행하라.
		 */
		String transletName = null;
		JoinpointScopeType joinpointScope = null;
		String beanId = beanRule.getId();

		if(activity != null) {
			/*
			 * Translet, JoinpointScope의 적용여부를 결정 
			 */
			if(beanRule.getScopeType() == ScopeType.PROTOTYPE || beanRule.getScopeType() == ScopeType.REQUEST) {
				transletName = activity.getTransletName();
			}
			if(beanRule.getScopeType() == ScopeType.PROTOTYPE) {
				joinpointScope = activity.getJoinpointScope();
			}
		}
		
		/*
		 * Bean과 관련된 AspectRule을 모두 추출.
		 */
		List<AspectRule> aspectRuleList = retrieveAspectRuleList(activity, joinpointScope, transletName, beanId);
		
		Object obj = null;
		
		if(aspectRuleList.size() > 0) {
			obj = CglibDynamicBeanProxy.newInstance(aspectRuleList, beanRule, argTypes, args);
		} else {
			if(argTypes != null && args != null)
				obj = newInstance(beanRule, argTypes, args);
			else
				obj = newInstance(beanRule, new Class[0], new Object[0]);
		}
		
		return obj;
	}
	
	private List<AspectRule> retrieveAspectRuleList(CoreActivity activity, JoinpointScopeType joinpointScope, String transletName, String beanId) {
		String joinpointScopeString = joinpointScope == null ? null : joinpointScope.toString();
		String patternString = PointcutPattern.combinePatternString(joinpointScopeString, transletName, beanId, null);

		List<AspectRule> aspectRuleList;
		
		synchronized(aspectRuleCache) {
			aspectRuleList = aspectRuleCache.get(patternString);
			
			if(aspectRuleList == null) {
				aspectRuleList = activity.getAspectRuleRegistry().getAspectRuleList(joinpointScope, transletName, beanId);
				aspectRuleCache.put(patternString, aspectRuleList);
			}
		}
		
		return aspectRuleList;
	}
	
	protected Object newInstance(BeanRule beanRule, Class<?>[] argTypes, Object[] args) throws BeanInstantiationException {
		Constructor<?> constructorToUse;
		
		try {
			constructorToUse = getMatchConstructor(beanRule.getBeanClass(), args);

			if(constructorToUse == null) {
				constructorToUse = beanRule.getBeanClass().getDeclaredConstructor(argTypes);
			}
		} catch(NoSuchMethodException e) {
			throw new BeanInstantiationException(beanRule.getBeanClass(), "No default constructor found.", e);
		}
		
		Object obj = newInstance(constructorToUse, args);
		
		return obj;
	}
	
	private Object newInstance(Constructor<?> ctor, Object[] args) throws BeanInstantiationException {
		try {
			if(!Modifier.isPublic(ctor.getModifiers()) ||
					!Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) {
				ctor.setAccessible(true);
			}
	
			return ctor.newInstance(args);
		} catch(InstantiationException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(), "Is it an abstract class?", ex);
		} catch(IllegalAccessException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(),
					"Has the class definition changed? Is the constructor accessible?", ex);
		} catch(IllegalArgumentException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(), "Illegal arguments for constructor", ex);
		} catch(InvocationTargetException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(), "Constructor threw exception", ex
					.getTargetException());
		}
	}
	
	private Constructor<?> getMatchConstructor(Class<?> clazz, Object[] args) {
		Constructor<?>[] candidates = clazz.getDeclaredConstructors();

		Constructor<?> constructorToUse = null;
		float bestMatchWeight = Float.MAX_VALUE;
		float matchWeight = Float.MAX_VALUE;
		
		for(Constructor<?> candidate : candidates) {
			matchWeight = ReflectionUtils.getTypeDifferenceWeight(candidate.getParameterTypes(), args);
			
			if(matchWeight < bestMatchWeight) {
				constructorToUse = candidate;
				bestMatchWeight = matchWeight;
			}
		}
		
		return constructorToUse;
	}
	
	public void destroy() {
		for(BeanRule beanRule : beanRuleMap) {
			ScopeType scopeType = beanRule.getScopeType();

			if(scopeType == ScopeType.SINGLETON) {
				if(beanRule.isRegistered()) {
					String destroyMethodName = beanRule.getDestroyMethodName();
					
					if(destroyMethodName != null) {
						try {
							MethodUtils.invokeExactMethod(beanRule.getBean(), destroyMethodName, null);
						} catch(Exception e) {
							throw new BeanDestroyFailedException(beanRule);
						}
					}
					
					beanRule.setBean(null);
					beanRule.setRegistered(false);
				}
			}
		}
	}

}
