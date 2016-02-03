package com.aspectran.core.context.bean;

import java.lang.reflect.Constructor;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.bean.annotation.Autowired;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.util.ClassUtils;

/**
 * The Class BeanAnnotationProcessor.
 */
public abstract class BeanAnnotationProcessor {

	public static void process(BeanRule beanRule, Object bean, Activity activity) {
		Class<?> targetClass = beanRule.getBeanClass();
		Autowired autowiredAnno = targetClass.getAnnotation(Autowired.class);
		boolean required = autowiredAnno.required();


		
	}

	private static Constructor<?> getMatchConstructor(Class<?> clazz, Object[] args) {
		Constructor<?>[] candidates = clazz.getDeclaredConstructors();
		Constructor<?> constructorToUse = null;
		float bestMatchWeight = Float.MAX_VALUE;
		float matchWeight = Float.MAX_VALUE;

		for(Constructor<?> candidate : candidates) {
			if(candidate.isAnnotationPresent(Autowired.class)) {
				matchWeight = ClassUtils.getTypeDifferenceWeight(candidate.getParameterTypes(), args);

				if(matchWeight < bestMatchWeight) {
					constructorToUse = candidate;
					bestMatchWeight = matchWeight;
				}
			}
		}

		return constructorToUse;
	}

}
