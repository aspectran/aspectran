package com.aspectran.core.context.bean.registry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import com.aspectran.core.rule.BeanRule;
import com.aspectran.core.util.ReflectionUtils;

/**
 * <p>
 * Created: 2009. 03. 09 오후 23:48:09
 * </p>
 */
public abstract class AbstractBeanRegistry {

	public Object newInstance(BeanRule beanRule, Object[] args) throws BeanInstantiationException {
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
	
	public Object newInstance(Constructor<?> ctor, Object[] args) throws BeanInstantiationException {
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
	
	public Constructor<?> getMatchConstructor(Class<?> clazz, Object[] args) {
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

}
