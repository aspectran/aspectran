package com.aspectran.core.context.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Iterator;

import com.aspectran.core.rule.BeanRule;
import com.aspectran.core.rule.BeanRuleMap;
import com.aspectran.core.rule.ItemRule;
import com.aspectran.core.rule.ItemRuleMap;
import com.aspectran.core.token.expression.ItemTokenExpressor;
import com.aspectran.core.type.ScopeType;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.ReflectionUtils;
import com.aspectran.core.var.ValueMap;

/**
 * <p>
 * Created: 2009. 03. 09 오후 23:48:09
 * </p>
 */
public abstract class AbstractBeanRegistry {

	protected final BeanRuleMap beanRuleMap;
	
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
				
				Iterator<ItemRule> iter = constructorArgumentItemRuleMap.iterator();
				int i = 0;
				
				while(iter.hasNext()) {
					ItemRule ir = iter.next();
					Object o = valueMap.get(ir.getName());
					args[i] = o;
					
					i++;
				}
				
				return newInstance(beanRule, args);
			} else {
				return newInstance(beanRule, new Object[0]);
			}
		} catch(Exception e) {
			throw new BeanCreationException(beanRule, e);
		}
	}
	
	protected Object newInstance(BeanRule beanRule, Object[] args) throws BeanInstantiationException {
		Constructor<?> constructorToUse;
		
		try {
			constructorToUse = getMatchConstructor(beanRule.getBeanClass(), args);

			if(constructorToUse == null) {
				Class<?>[] parameterTypes = new Class<?>[args.length];
				
				for(int i = 0; i < args.length; i++) {
					parameterTypes[i] = args[i].getClass();
				}
				
				constructorToUse = beanRule.getBeanClass().getDeclaredConstructor(parameterTypes);
			}
		} catch(NoSuchMethodException e) {
			throw new BeanInstantiationException(beanRule.getBeanClass(), "No default constructor found.", e);
		}
		
		return newInstance(constructorToUse, args);
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
