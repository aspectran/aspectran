package com.aspectran.base.util;


/**
 * Simple utility class for working with the reflection API.
 *
 * @author Gulendol
 * @since 2011. 2. 5.
 *
 */
public class ReflectionUtils {

	public static float getTypeDifferenceWeight(Class<?>[] srcArgs, Object[] destArgs) {
		if(srcArgs.length != destArgs.length)
			return Float.MAX_VALUE;
		
		float weight = 0.0f;
		
		for(int i = 0; i < srcArgs.length; i++) {
			Class<?> srcClass = srcArgs[i];
			Object destObject = destArgs[i];
			weight += getTypeDifferenceWeight(srcClass, destObject);

			if(weight == Float.MAX_VALUE)
				break;
		}
		
		return weight;
	}
	
	public static float getTypeDifferenceWeight(Class<?> srcClass, Object destObject) {
		if(!ClassUtils.isAssignableValue(srcClass, destObject))
			return Float.MAX_VALUE;
		
		return getTypeDifferenceWeight(srcClass, destObject.getClass());
	}

	/**
	 * Returns the sum of the object transformation cost for each class in the source
	 * argument list.
	 * @param srcArgs The source arguments
	 * @param destArgs The destination arguments
	 * @return the accumulated weight for all arguments
	 */
	public static float getTypeDifferenceWeight(Class<?>[] srcArgs, Class<?>[] destArgs) {
		float weight = 0.0f;
		
		for(int i = 0; i < srcArgs.length; i++) {
			Class<?> srcClass = srcArgs[i];
			Class<?> destClass = destArgs[i];
			weight += getTypeDifferenceWeight(srcClass, destClass);
		}

		return weight;
	}

	/**
	 * Gets the number of steps required needed to turn the source class into the
	 * destination class. This represents the number of steps in the object hierarchy
	 * graph.
	 * @param srcClass The source class
	 * @param destClass The destination class
	 * @return The cost of transforming an object
	 */
	public static float getTypeDifferenceWeight(Class<?> srcClass, Class<?> destClass) {
		float weight = 0.0f;

		while(destClass != null && !destClass.equals(srcClass)) {
			if(destClass.isInterface() && ClassUtils.isAssignable(destClass, srcClass)) {
				// slight penalty for interface match.
				// we still want an exact match to override an interface match, but
				// an interface match should override anything where we have to get a
				// superclass.
				weight += 0.25f;
				break;
			}
			weight++;
			destClass = destClass.getSuperclass();
		}

		/*
		 * If the destination class is null, we've travelled all the way up to
		 * an Object match. We'll penalize this by adding 1.5 to the cost.
		 */
		if(destClass == null) {
			weight += 1.5f;
		}

		return weight;
	}
}
