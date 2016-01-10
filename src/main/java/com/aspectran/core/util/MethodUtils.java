/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

public class MethodUtils {

    /**
     * Indicates whether methods should be cached for improved performance.
     * <p>
     * Note that when this class is deployed via a shared classloader in
     * a container, this will affect all webapps. However making this
     * configurable per webapp would mean having a map keyed by context classloader
     * which may introduce memory-leak problems.
     */
    private static boolean cacheEnabled = true;

	/** An empty class array */
	public static final Class<?>[] EMPTY_CLASS_PARAMETERS = new Class[0];

	/** An empty object array */
	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * Stores a cache of MethodDescriptor -> Method in a WeakHashMap.
     * <p>
     * The keys into this map only ever exist as temporary variables within
     * methods of this class, and are never exposed to users of this class.
     * This means that the WeakHashMap is used only as a mechanism for
     * limiting the size of the cache, ie a way to tell the garbage collector
     * that the contents of the cache can be completely garbage-collected
     * whenever it needs the memory. Whether this is a good approach to
     * this problem is doubtful; something like the commons-collections
     * LRUMap may be more appropriate (though of course selecting an
     * appropriate size is an issue).
     * <p>
     * This static variable is safe even when this code is deployed via a
     * shared classloader because it is keyed via a MethodDescriptor object
     * which has a Class as one of its members and that member is used in
     * the MethodDescriptor.equals method. So two components that load the same
     * class via different classloaders will generate non-equal MethodDescriptor
     * objects and hence end up with different entries in the map.
     */
    private static final Map<MethodDescriptor, Reference<Method>> cache = Collections.synchronizedMap(new WeakHashMap<MethodDescriptor, Reference<Method>>());

	/**
	 * Sets the value of a bean property to an Object.
	 *
	 * @param object The bean to change
	 * @param setterName The property name or setter method name
	 * @param arg use this argument
	 * @throws NoSuchMethodException the no such method exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
    public static void invokeSetter(Object object, String setterName, Object arg)
    		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Object[] args = { arg };
		invokeSetter(object, setterName, args);
    }

    /**
     * Sets the value of a bean property to an Object.
     *
     * @param object The bean to change
	 * @param setterName The property name or setter method name
     * @param args use this arguments
     * @throws NoSuchMethodException the no such method exception
     * @throws IllegalAccessException the illegal access exception
     * @throws InvocationTargetException the invocation target exception
     */
    public static void invokeSetter(Object object, String setterName, Object[] args)
    		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    	int index = setterName.indexOf('.');
    	if(index > 0) {
    		String getterName = setterName.substring(0, index);
    		Object o = invokeGetter(object, getterName);
    		invokeSetter(o, setterName.substring(index + 1), args);
    	} else {
	    	if(!setterName.startsWith("set")) {
	    		setterName = "set" + setterName.substring(0, 1).toUpperCase(Locale.US) + setterName.substring(1);
	    	}
	    	invokeMethod(object, setterName, args);
    	}
    }

    /**
     * Gets an Object property from a bean.
     *
	 * @param object The bean
	 * @param getterName The property name or getter method name
	 * @return The property value (as an Object)
     * @throws NoSuchMethodException the no such method exception
     * @throws IllegalAccessException the illegal access exception
     * @throws InvocationTargetException the invocation target exception
     */
    public static Object invokeGetter(Object object, String getterName)
    		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    	return invokeMethod(object, getterName);
    }

    /**
     * Gets an Object property from a bean.
     *
     * @param object The bean
	 * @param getterName The property name or getter method name
     * @param arg use this argument
     * @return The property value (as an Object)
     * @throws NoSuchMethodException the no such method exception
     * @throws IllegalAccessException the illegal access exception
     * @throws InvocationTargetException the invocation target exception
     */
    public static Object invokeGetter(Object object, String getterName, Object arg)
    		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    	Object[] args = { arg };
    	return invokeGetter(object, getterName, args);
    }

    /**
     * Gets an Object property from a bean.
     *
     * @param object The bean
	 * @param getterName The property name or getter method name
     * @param args use this arguments
     * @return The property value (as an Object)
     * @throws NoSuchMethodException the no such method exception
     * @throws IllegalAccessException the illegal access exception
     * @throws InvocationTargetException the invocation target exception
     */
    public static Object invokeGetter(Object object, String getterName, Object[] args)
    		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    	int index = getterName.indexOf('.');
    	if(index > 0) {
    		String getterName2 = getterName.substring(0, index);
    		Object o = invokeGetter(object, getterName2);
    		return invokeGetter(o, getterName.substring(index + 1), args);
    	} else {
	    	if(!getterName.startsWith("get") && !getterName.startsWith("is")) {
	    		getterName = "get" + getterName.substring(0, 1).toUpperCase(Locale.US) + getterName.substring(1);
	    	}
	    	return invokeMethod(object, getterName, args);
    	}
    }

	/**
	 * <p>Invoke a named method whose parameter type matches the object type.</p>
	 *
	 * @param object invoke method on this object
	 * @param methodName get method with this name
	 * @return The value returned by the invoked method
	 *
	 * @throws NoSuchMethodException if there is no such accessible method
	 * @throws InvocationTargetException wraps an exception thrown by the method invoked
	 * @throws IllegalAccessException if the requested method is not accessible
	 */
	public static Object invokeMethod(Object object, String methodName)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return invokeMethod(object, methodName, EMPTY_OBJECT_ARRAY, EMPTY_CLASS_PARAMETERS);
	}

	/**
	 * <p>Invoke a named method whose parameter type matches the object type.</p>
	 *
	 * <p>The behaviour of this method is less deterministic
	 * than <code>invokeExactMethod()</code>.
	 * It loops through all methods with names that match
	 * and then executes the first it finds with compatable parameters.</p>
	 *
	 * <p>This method supports calls to methods taking primitive parameters
	 * via passing in wrapping classes. So, for example, a <code>Boolean</code> class
	 * would match a <code>boolean</code> primitive.</p>
	 *
	 * <p> This is a convenient wrapper for
	 * {@link #invokeMethod(Object object,String methodName,Object[] args)}.
	 * </p>
	 *
	 * @param object invoke method on this object
	 * @param methodName get method with this name
	 * @param arg use this argument
	 * @return The value returned by the invoked method
	 *
	 * @throws NoSuchMethodException if there is no such accessible method
	 * @throws InvocationTargetException wraps an exception thrown by the method invoked
	 * @throws IllegalAccessException if the requested method is not accessible
	 *  via reflection
	 */
	public static Object invokeMethod(Object object, String methodName, Object arg)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Object[] args = { arg };
		return invokeMethod(object, methodName, args);
	}

	/**
	 * <p>Invoke a named method whose parameter type matches the object type.</p>
	 *
	 * <p>The behaviour of this method is less deterministic
	 * than {@link #invokeExactMethod(Object object,String methodName,Object[] args)}.
	 * It loops through all methods with names that match
	 * and then executes the first it finds with compatable parameters.</p>
	 *
	 * <p>This method supports calls to methods taking primitive parameters
	 * via passing in wrapping classes. So, for example, a <code>Boolean</code> class
	 * would match a <code>boolean</code> primitive.</p>
	 *
	 * <p> This is a convenient wrapper for
	 * {@link #invokeMethod(Object object,String methodName,Object[] args,Class[] parameterTypes)}.
	 * </p>
	 *
	 * @param object invoke method on this object
	 * @param methodName get method with this name
	 * @param args use these arguments - treat null as empty array
	 * @return The value returned by the invoked method
	 *
	 * @throws NoSuchMethodException if there is no such accessible method
	 * @throws InvocationTargetException wraps an exception thrown by the
	 *  method invoked
	 * @throws IllegalAccessException if the requested method is not accessible
	 *  via reflection
	 */
	public static Object invokeMethod(Object object, String methodName, Object[] args)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Class<?>[] parameterTypes;

		if(args == null) {
			args = EMPTY_OBJECT_ARRAY;
			parameterTypes = EMPTY_CLASS_PARAMETERS;
		} else {
			int arguments = args.length;
			if(arguments == 0) {
				parameterTypes = EMPTY_CLASS_PARAMETERS;
			} else {
				parameterTypes = new Class[arguments];

				for (int i = 0; i < arguments; i++) {
					if (args[i] != null)
						parameterTypes[i] = args[i].getClass();
				}
			}
		}
		
		return invokeMethod(object, methodName, args, parameterTypes);
	}

	/**
	 * <p>Invoke a named method whose parameter type matches the object type.</p>
	 *
	 * <p>The behaviour of this method is less deterministic
	 * than {@link
	 * #invokeExactMethod(Object object,String methodName,Object[] args,Class[] parameterTypes)}.
	 * It loops through all methods with names that match
	 * and then executes the first it finds with compatable parameters.</p>
	 *
	 * <p>This method supports calls to methods taking primitive parameters
	 * via passing in wrapping classes. So, for example, a <code>Boolean</code> class
	 * would match a <code>boolean</code> primitive.</p>
	 *
	 *
	 * @param object invoke method on this object
	 * @param methodName get method with this name
	 * @param args use these arguments - treat null as empty array
	 * @param parameterTypes match these parameters - treat null as empty array
	 * @return The value returned by the invoked method
	 *
	 * @throws NoSuchMethodException if there is no such accessible method
	 * @throws InvocationTargetException wraps an exception thrown by the
	 *  method invoked
	 * @throws IllegalAccessException if the requested method is not accessible
	 *  via reflection
	 */
	public static Object invokeMethod(Object object, String methodName, Object[] args, Class<?>[] parameterTypes)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if(args == null)
			args = EMPTY_OBJECT_ARRAY;

		if(parameterTypes == null)
			parameterTypes = EMPTY_CLASS_PARAMETERS;

		Method method = getMatchingAccessibleMethod(object.getClass(), methodName, parameterTypes);
		
		if(method == null) {
			throw new NoSuchMethodException("No such accessible method: " + methodName + "() on object: " + object.getClass().getName());
		}

		return invokeMethod(object, method, args, parameterTypes);
	}

	/**
	 * <p>Invoke a method whose parameter type matches exactly the object type.</p>
	 *
	 * <p> This is a convenient wrapper for
	 * {@link #invokeExactMethod(Object object,String methodName,Object[] args)}.
	 * </p>
	 *
	 * @param object invoke method on this object
	 * @param methodName get method with this name
	 * @param arg use this argument
	 * @return The value returned by the invoked method
	 *
	 * @throws NoSuchMethodException if there is no such accessible method
	 * @throws InvocationTargetException wraps an exception thrown by the
	 *  method invoked
	 * @throws IllegalAccessException if the requested method is not accessible
	 *  via reflection
	 */
	public static Object invokeExactMethod(Object object, String methodName, Object arg)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Object[] args = { arg };
		return invokeExactMethod(object, methodName, args);
	}

	/**
	 * <p>Invoke a method whose parameter types match exactly the object types.</p>
	 *
	 * <p> This uses reflection to invoke the method obtained from a call to
	 * <code>getAccessibleMethod()</code>.</p>
	 *
	 * @param object invoke method on this object
	 * @param methodName get method with this name
	 * @param args use these arguments - treat null as empty array
	 * @return The value returned by the invoked method
	 *
	 * @throws NoSuchMethodException if there is no such accessible method
	 * @throws InvocationTargetException wraps an exception thrown by the
	 *  method invoked
	 * @throws IllegalAccessException if the requested method is not accessible
	 *  via reflection
	 */
	public static Object invokeExactMethod(Object object, String methodName, Object[] args)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if(args == null) {
			args = EMPTY_OBJECT_ARRAY;
		}
		int arguments = args.length;
		Class<?>[] parameterTypes = new Class[arguments];
		for(int i = 0; i < arguments; i++) {
			parameterTypes[i] = args[i].getClass();
		}
		
		return invokeExactMethod(object, methodName, args, parameterTypes);
	}

	/**
	 * <p>Invoke a method whose parameter types match exactly the parameter types given.</p>
	 *
	 * <p>This uses reflection to invoke the method obtained from a call to
	 * <code>getAccessibleMethod()</code>.</p>
	 *
	 * @param object invoke method on this object
	 * @param methodName get method with this name
	 * @param args use these arguments - treat null as empty array
	 * @param parameterTypes match these parameters - treat null as empty array
	 * @return The value returned by the invoked method
	 *
	 * @throws NoSuchMethodException if there is no such accessible method
	 * @throws InvocationTargetException wraps an exception thrown by the
	 *  method invoked
	 * @throws IllegalAccessException if the requested method is not accessible
	 *  via reflection
	 */
	public static Object invokeExactMethod(Object object, String methodName, Object[] args, Class<?>[] parameterTypes)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if(args == null) {
			args = EMPTY_OBJECT_ARRAY;
		}

		if(parameterTypes == null) {
			parameterTypes = EMPTY_CLASS_PARAMETERS;
		}

		Method method = getAccessibleMethod(object.getClass(), methodName, parameterTypes);
		
		if(method == null) {
			throw new NoSuchMethodException("No such accessible method: " + methodName + "() on object: " + object.getClass().getName());
		}
		
		return method.invoke(object, args);
	}

	/**
	 * <p>Invoke a static method whose parameter types match exactly the parameter types given.</p>
	 *
	 * <p>This uses reflection to invoke the method obtained from a call to
	 * {@link #getAccessibleMethod(Class, String, Class[])}.</p>
	 *
	 * @param objectClass invoke static method on this class
	 * @param methodName get method with this name
	 * @param args use these arguments - treat null as empty array
	 * @param parameterTypes match these parameters - treat null as empty array
	 * @return The value returned by the invoked method
	 *
	 * @throws NoSuchMethodException if there is no such accessible method
	 * @throws InvocationTargetException wraps an exception thrown by the
	 *  method invoked
	 * @throws IllegalAccessException if the requested method is not accessible
	 *  via reflection
	 */
	public static Object invokeExactStaticMethod(Class<?> objectClass, String methodName, Object[] args, Class<?>[] parameterTypes)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if(args == null) {
			args = EMPTY_OBJECT_ARRAY;
		}

		if(parameterTypes == null) {
			parameterTypes = EMPTY_CLASS_PARAMETERS;
		}

		Method method = getAccessibleMethod(objectClass, methodName, parameterTypes);
		
		if(method == null) {
			throw new NoSuchMethodException("No such accessible method: " + methodName + "() on class: " + objectClass.getName());
		}
		
		return method.invoke(null, args);
	}

	/**
	 * <p>Invoke a named static method whose parameter type matches the object type.</p>
	 *
	 * <p>The behaviour of this method is less deterministic
	 * than {@link #invokeExactMethod(Object, String, Object[], Class[])}.
	 * It loops through all methods with names that match
	 * and then executes the first it finds with compatable parameters.</p>
	 *
	 * <p>This method supports calls to methods taking primitive parameters
	 * via passing in wrapping classes. So, for example, a <code>Boolean</code> class
	 * would match a <code>boolean</code> primitive.</p>
	 *
	 * <p> This is a convenient wrapper for
	 * {@link #invokeStaticMethod(Class objectClass,String methodName,Object[] args)}.
	 * </p>
	 *
	 * @param objectClass invoke static method on this class
	 * @param methodName get method with this name
	 * @param arg use this argument
	 * @return The value returned by the invoked method
	 *
	 * @throws NoSuchMethodException if there is no such accessible method
	 * @throws InvocationTargetException wraps an exception thrown by the
	 *  method invoked
	 * @throws IllegalAccessException if the requested method is not accessible
	 *  via reflection
	 */
	public static Object invokeStaticMethod(Class<?> objectClass, String methodName, Object arg)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Object[] args = { arg };
		return invokeStaticMethod(objectClass, methodName, args);
	}

	/**
	 * <p>Invoke a named static method whose parameter type matches the object type.</p>
	 *
	 * <p>The behaviour of this method is less deterministic
	 * than {@link #invokeExactMethod(Object object,String methodName,Object[] args)}.
	 * It loops through all methods with names that match
	 * and then executes the first it finds with compatable parameters.</p>
	 *
	 * <p>This method supports calls to methods taking primitive parameters
	 * via passing in wrapping classes. So, for example, a <code>Boolean</code> class
	 * would match a <code>boolean</code> primitive.</p>
	 *
	 * <p> This is a convenient wrapper for
	 * {@link #invokeStaticMethod(Class objectClass,String methodName,Object[] args,Class[] parameterTypes)}.
	 * </p>
	 *
	 * @param objectClass invoke static method on this class
	 * @param methodName get method with this name
	 * @param args use these arguments - treat null as empty array
	 * @return The value returned by the invoked method
	 *
	 * @throws NoSuchMethodException if there is no such accessible method
	 * @throws InvocationTargetException wraps an exception thrown by the
	 *  method invoked
	 * @throws IllegalAccessException if the requested method is not accessible
	 *  via reflection
	 */
	public static Object invokeStaticMethod(Class<?> objectClass, String methodName, Object[] args)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if(args == null) {
			args = EMPTY_OBJECT_ARRAY;
		}
		int arguments = args.length;
		Class<?>[] parameterTypes = new Class[arguments];
		for(int i = 0; i < arguments; i++) {
			parameterTypes[i] = args[i].getClass();
		}
		return invokeStaticMethod(objectClass, methodName, args, parameterTypes);
	}

	/**
	 * <p>Invoke a named static method whose parameter type matches the object type.</p>
	 *
	 * <p>The behaviour of this method is less deterministic
	 * than {@link
	 * #invokeExactStaticMethod(Class objectClass,String methodName,Object[] args,Class[] parameterTypes)}.
	 * It loops through all methods with names that match
	 * and then executes the first it finds with compatable parameters.</p>
	 *
	 * <p>This method supports calls to methods taking primitive parameters
	 * via passing in wrapping classes. So, for example, a <code>Boolean</code> class
	 * would match a <code>boolean</code> primitive.</p>
	 *
	 *
	 * @param objectClass invoke static method on this class
	 * @param methodName get method with this name
	 * @param args use these arguments - treat null as empty array
	 * @param parameterTypes match these parameters - treat null as empty array
	 * @return The value returned by the invoked method
	 *
	 * @throws NoSuchMethodException if there is no such accessible method
	 * @throws InvocationTargetException wraps an exception thrown by the
	 *  method invoked
	 * @throws IllegalAccessException if the requested method is not accessible
	 *  via reflection
	 */
	public static Object invokeStaticMethod(Class<?> objectClass, String methodName, Object[] args, Class<?>[] parameterTypes)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if(args == null)
			args = EMPTY_OBJECT_ARRAY;

		if(parameterTypes == null)
			parameterTypes = EMPTY_CLASS_PARAMETERS;

		Method method = getMatchingAccessibleMethod(objectClass, methodName, parameterTypes);
		
		if(method == null) {
			throw new NoSuchMethodException("No such accessible method: " + methodName + "() on class: " + objectClass.getName());
		}
		
		return invokeMethod(null, method, args, parameterTypes);
	}

	/**
	 * <p>Invoke a static method whose parameter type matches exactly the object type.</p>
	 *
	 * <p> This is a convenient wrapper for
	 * {@link #invokeExactStaticMethod(Class objectClass,String methodName,Object[] args)}.
	 * </p>
	 *
	 * @param objectClass invoke static method on this class
	 * @param methodName get method with this name
	 * @param arg use this argument
	 * @return The value returned by the invoked method
	 *
	 * @throws NoSuchMethodException if there is no such accessible method
	 * @throws InvocationTargetException wraps an exception thrown by the
	 *  method invoked
	 * @throws IllegalAccessException if the requested method is not accessible
	 *  via reflection
	 */
	public static Object invokeExactStaticMethod(Class<?> objectClass, String methodName, Object arg)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Object[] args = { arg };
		return invokeExactStaticMethod(objectClass, methodName, args);
	}

	/**
	 * <p>Invoke a static method whose parameter types match exactly the object types.</p>
	 *
	 * <p> This uses reflection to invoke the method obtained from a call to
	 * {@link #getAccessibleMethod(Class, String, Class[])}.</p>
	 *
	 * @param objectClass invoke static method on this class
	 * @param methodName get method with this name
	 * @param args use these arguments - treat null as empty array
	 * @return The value returned by the invoked method
	 *
	 * @throws NoSuchMethodException if there is no such accessible method
	 * @throws InvocationTargetException wraps an exception thrown by the
	 *  method invoked
	 * @throws IllegalAccessException if the requested method is not accessible
	 *  via reflection
	 */
	public static Object invokeExactStaticMethod(Class<?> objectClass, String methodName, Object[] args)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if(args == null) {
			args = EMPTY_OBJECT_ARRAY;
		}
		
		int arguments = args.length;
		Class<?>[] parameterTypes = new Class[arguments];
		
		for(int i = 0; i < arguments; i++) {
			parameterTypes[i] = args[i].getClass();
		}
		
		return invokeExactStaticMethod(objectClass, methodName, args, parameterTypes);
	}
	
	private static Object invokeMethod(Object object, Method method, Object[] args, Class<?>[] parameterTypes)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?>[] methodsParams = method.getParameterTypes();
		if(hasPrimitiveArray(methodsParams)) {
			for(int i = 0; i < methodsParams.length; i++) {
				if(ClassUtils.isPrimitiveArray(methodsParams[i])) {
					if(ClassUtils.isPrimitiveWrapperArray(parameterTypes[i])) {
						args[i] = toPrimitiveArray(args[i]);
					}
				}
			}
		}
		
		return method.invoke(object, args);
	}
	
	private static boolean hasPrimitiveArray(Class<?>[] parameterTypes) {
		for(int i = 0; i < parameterTypes.length; i++) {
			if(ClassUtils.isPrimitiveArray(parameterTypes[i]))
				return true;
		}
		
		return false;
	}

//	/**
//	 * <p>Return an accessible method (that is, one that can be invoked via
//	 * reflection) with given name and a single parameter.  If no such method
//	 * can be found, return <code>null</code>.
//	 * Basically, a convenience wrapper that constructs a <code>Class</code>
//	 * array for you.</p>
//	 *
//	 * @param clazz get method from this class
//	 * @param methodName get method with this name
//	 * @param parameterType taking this type of parameter
//	 * @return The accessible method
//	 */
//	public static Method getAccessibleMethod(Class<?> clazz, String methodName, Class<?> parameterType) {
//		Class<?>[] parameterTypes = { parameterType };
//		return getAccessibleMethod(clazz, methodName, parameterTypes);
//	}

	/**
	 * <p>Return an accessible method (that is, one that can be invoked via
	 * reflection) with given name and parameters.  If no such method
	 * can be found, return <code>null</code>.
	 * This is just a convenient wrapper for
	 * {@link #getAccessibleMethod(Method method)}.</p>
	 *
	 * @param clazz get method from this class
	 * @param methodName get method with this name
	 * @param parameterTypes with these parameters types
	 * @return The accessible method
	 */
	public static Method getAccessibleMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
		try {
			MethodDescriptor md = new MethodDescriptor(clazz, methodName, parameterTypes, true);
			
			// Check the cache first
			Method method = getCachedMethod(md);
            if(method != null) {
                return method;
            }
            
			method = getAccessibleMethod(clazz.getMethod(methodName, parameterTypes));
			cacheMethod(md, method);
			
			return method;
		} catch(NoSuchMethodException e) {
			return (null);
		}
	}

    /**
     * <p>Return an accessible method (that is, one that can be invoked via
     * reflection) that implements the specified Method.  If no such method
     * can be found, return <code>null</code>.</p>
     *
     * @param method The method that we wish to call
     * @return The accessible method
     */
    public static Method getAccessibleMethod(final Method method) {
        // Make sure we have a method to check
        if(method == null) {
            return null;
        }

        return getAccessibleMethod(method.getDeclaringClass(), method);
    }

    /**
     * <p>Return an accessible method (that is, one that can be invoked via
     * reflection) that implements the specified Method.  If no such method
     * can be found, return <code>null</code>.</p>
     *
     * @param clazz The class of the object
     * @param method The method that we wish to call
     * @return The accessible method
     */
    public static Method getAccessibleMethod(Class<?> clazz, Method method) {
        // Make sure we have a method to check
        if(method == null) {
            return (null);
        }

        // If the requested method is not public we cannot call it
        if(!Modifier.isPublic(method.getModifiers())) {
            return (null);
        }

        boolean sameClass = true;
        if(clazz == null) {
            clazz = method.getDeclaringClass();
        } else {
            sameClass = clazz.equals(method.getDeclaringClass());
            if(!method.getDeclaringClass().isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(clazz.getName() + " is not assignable from " + method.getDeclaringClass().getName());
            }
        }

        // If the class is public, we are done
        if(Modifier.isPublic(clazz.getModifiers())) {
            if(!sameClass && !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
                setMethodAccessible(method); // Default access superclass workaround
            }
            return (method);
        }

        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();

        // Check the implemented interfaces and subinterfaces
        method = getAccessibleMethodFromInterfaceNest(clazz, methodName, parameterTypes);

        // Check the superclass chain
        if(method == null) {
            method = getAccessibleMethodFromSuperclass(clazz, methodName, parameterTypes);
        }

        return method;
    }
	
	/**
	 * <p>Return an accessible method (that is, one that can be invoked via
	 * reflection) by scanning through the superclasses. If no such method
	 * can be found, return <code>null</code>.</p>
	 *
	 * @param clazz Class to be checked
	 * @param methodName Method name of the method we wish to call
	 * @param parameterTypes The parameter type signatures
	 */
	private static Method getAccessibleMethodFromSuperclass(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
		Class<?> parentClazz = clazz.getSuperclass();
		
		while(parentClazz != null) {
			if(Modifier.isPublic(parentClazz.getModifiers())) {
				try {
					return parentClazz.getMethod(methodName, parameterTypes);
				} catch(NoSuchMethodException e) {
					return null;
				}
			}
			
			parentClazz = parentClazz.getSuperclass();
		}
		
		return null;
	}

	/**
	 * <p>Return an accessible method (that is, one that can be invoked via
	 * reflection) that implements the specified method, by scanning through
	 * all implemented interfaces and subinterfaces.  If no such method
	 * can be found, return <code>null</code>.</p>
	 *
	 * <p> There isn't any good reason why this method must be private.
	 * It is because there doesn't seem any reason why other classes should
	 * call this rather than the higher level methods.</p>
	 *
	 * @param clazz Parent class for the interfaces to be checked
	 * @param methodName Method name of the method we wish to call
	 * @param parameterTypes The parameter type signatures
	 */
	private static Method getAccessibleMethodFromInterfaceNest(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
		Method method = null;

		// Search up the superclass chain
		for(; clazz != null; clazz = clazz.getSuperclass()) {
			// Check the implemented interfaces of the parent class
			Class<?>[] interfaces = clazz.getInterfaces();
			
			for(int i = 0; i < interfaces.length; i++) {
				// Is this interface public?
				if(!Modifier.isPublic(interfaces[i].getModifiers())) {
					continue;
				}

				// Does the method exist on this interface?
				try {
					method = interfaces[i].getDeclaredMethod(methodName, parameterTypes);
				} catch(NoSuchMethodException e) {
					/* Swallow, if no method is found after the loop then this
					 * method returns null.
					 */
				}
				if(method != null) {
					return method;
				}

				// Recursively check our parent interfaces
				method = getAccessibleMethodFromInterfaceNest(interfaces[i], methodName, parameterTypes);
				if(method != null) {
					return method;
				}
			}
		}

		// We did not find anything
		return null;
	}

	/**
	 * <p>Find an accessible method that matches the given name and has compatible parameters.
	 * Compatible parameters mean that every method parameter is assignable from
	 * the given parameters.
	 * In other words, it finds a method with the given name
	 * that will take the parameters given.
	 *
	 * <p>This method is slightly undeterminstic since it loops
	 * through methods names and return the first matching method.</p>
	 *
	 * <p>This method is used by
	 * {@link
	 * #invokeMethod(Object object,String methodName,Object[] args,Class[] parameterTypes)}.
	 *
	 * <p>This method can match primitive parameter by passing in wrapper classes.
	 * For example, a <code>Boolean</code> will match a primitive <code>boolean</code>
	 * parameter.
	 *
	 * @param clazz find method in this class
	 * @param methodName find method with this name
	 * @param parameterTypes find method with compatible parameters
	 * @return The accessible method
	 */
	public static Method getMatchingAccessibleMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
		MethodDescriptor md = new MethodDescriptor(clazz, methodName, parameterTypes, false);

		// see if we can find the method directly
		// most of the time this works and it's much faster
		try {
			// Check the cache first
			Method method = getCachedMethod(md);
			if(method != null) {
				return method;
			}

			method = clazz.getMethod(methodName, parameterTypes);

			setMethodAccessible(method); // Default access superclass workaround
			
			cacheMethod(md, method);
			return method;
		} catch(NoSuchMethodException e) { /* SWALLOW */ }

		// search through all methods
		int paramSize = parameterTypes.length;
		Method bestMatch = null;
		Method[] methods = clazz.getMethods();
		float bestMatchWeight = Float.MAX_VALUE;
		float myWeight = Float.MAX_VALUE;

		for(int i = 0, size = methods.length; i < size; i++) {
			if(methods[i].getName().equals(methodName)) {
				// compare parameters
				Class<?>[] methodsParams = methods[i].getParameterTypes();
				int methodParamSize = methodsParams.length;
				if(methodParamSize == paramSize) {
					boolean match = true;
					for(int n = 0; n < methodParamSize; n++) {
						if(!ClassUtils.isAssignable(methodsParams[n], parameterTypes[n])) {
							match = false;
							break;
						}
					}

					if(match) {
						// get accessible version of method
						Method method = getAccessibleMethod(methods[i]);
						if(method != null) {
							setMethodAccessible(method); // Default access superclass workaround
							myWeight = ClassUtils.getTypeDifferenceWeight(method.getParameterTypes(), parameterTypes);
							if(myWeight < bestMatchWeight) {
								bestMatch = method;
								bestMatchWeight = myWeight;
							}
						}
					}
				}
			}
		}
		
		if(bestMatch != null) {
			cacheMethod(md, bestMatch);
		}
		
		return bestMatch;
	}
	
	/**
     * Try to make the method accessible
     * @param method The source arguments
     */
    private static void setMethodAccessible(final Method method) {
        try {
            //
            // XXX Default access superclass workaround
            //
            // When a public class has a default access superclass
            // with public methods, these methods are accessible.
            // Calling them from compiled code works fine.
            //
            // Unfortunately, using reflection to invoke these methods
            // seems to (wrongly) to prevent access even when the method
            // modifer is public.
            //
            // The following workaround solves the problem but will only
            // work from sufficiently privilages code.
            //
            // Better workarounds would be greatfully accepted.
            //
            if(!method.isAccessible()) {
                method.setAccessible(true);
            }

        } catch(final SecurityException se) {
        	// Current Security Manager restricts use of workarounds for reflection bugs in pre-1.4 JVMs.
        }
    }
    
	private static Object toPrimitiveArray(Object val) {
		int len = Array.getLength(val);
		
		if(val instanceof Boolean[]) {
			boolean[] arr = new boolean[len];
			for(int i = 0; i < len; i++) {
				arr[i] = ((Boolean)Array.get(val, i)).booleanValue();
			}
			return arr;
		} else if(val instanceof Byte[]) {
			byte[] arr = new byte[len];
			for(int i = 0; i < len; i++) {
				arr[i] = ((Byte)Array.get(val, i)).byteValue();
			}
			return arr;
		} else if(val instanceof Character[]) {
			char[] arr = new char[len];
			for(int i = 0; i < len; i++) {
				arr[i] = ((Character)Array.get(val, i)).charValue();
			}
			return arr;
		} else if(val instanceof Short[]) {
			short[] arr = new short[len];
			for(int i = 0; i < len; i++) {
				arr[i] = ((Short)Array.get(val, i)).shortValue();
			}
			return arr;
		} else if(val instanceof Integer[]) {
			int[] arr = new int[len];
			for(int i = 0; i < len; i++) {
				arr[i] = ((Integer)Array.get(val, i)).intValue();
			}
			return arr;
		} else if(val instanceof Long[]) {
			long[] arr = new long[len];
			for(int i = 0; i < len; i++) {
				arr[i] = ((Long)Array.get(val, i)).longValue();
			}
			return arr;
		} else if(val instanceof Float[]) {
			float[] arr = new float[len];
			for(int i = 0; i < len; i++) {
				arr[i] = ((Float)Array.get(val, i)).floatValue();
			}
			return arr;
		} else if(val instanceof Double[]) {
			double[] arr = new double[len];
			for(int i = 0; i < len; i++) {
				arr[i] = ((Double)Array.get(val, i)).doubleValue();
			}
			return arr;
		}
		
		return null;
	}

    /**
     * Return the method from the cache, if present.
     *
     * @param md The method descriptor
     * @return The cached method
     */
    private static Method getCachedMethod(MethodDescriptor md) {
        if(cacheEnabled) {
            final Reference<Method> methodRef = cache.get(md);
            if(methodRef != null) {
                return methodRef.get();
            }
        }
        return null;
    }

    /**
     * Add a method to the cache.
     *
     * @param md The method descriptor
     * @param method The method to cache
     */
    private static void cacheMethod(MethodDescriptor md, Method method) {
        if(cacheEnabled) {
            if(method != null) {
            	cache.put(md, new WeakReference<Method>(method));
            }
        }
    }

    /**
     * Set whether methods should be cached for greater performance or not,
     * default is <code>true</code>.
     *
     * @param cacheMethods <code>true</code> if methods should be
     * cached for greater performance, otherwise <code>false</code>
     */
    public static synchronized void setCacheMethods(final boolean cacheMethods) {
    	cacheEnabled = cacheMethods;
        if(!cacheEnabled) {
            clearCache();
        }
    }

    /**
     * Clear the method cache.
     * @return the number of cached methods cleared
     */
    public static synchronized int clearCache() {
        final int size = cache.size();
        cache.clear();
        return size;
    }
    
	/**
	 * Represents the key to looking up a Method by reflection.
	 */
	private static class MethodDescriptor {
		
		private Class<?> cls;

		private String methodName;

		private Class<?>[] paramTypes;

		private boolean exact;

		private int hashCode;

		/**
		 * The sole constructor.
		 *
		 * @param cls  the class to reflect, must not be null
		 * @param methodName  the method name to obtain
		 * @param paramTypes the array of classes representing the paramater types
		 * @param exact whether the match has to be exact.
		 */
		public MethodDescriptor(Class<?> cls, String methodName, Class<?>[] paramTypes, boolean exact) {
			if(cls == null) {
				throw new IllegalArgumentException("Class cannot be null.");
			}
			if(methodName == null) {
				throw new IllegalArgumentException("Method Name cannot be null.");
			}
			if(paramTypes == null) {
				paramTypes = EMPTY_CLASS_PARAMETERS;
			}

			this.cls = cls;
			this.methodName = methodName;
			this.paramTypes = paramTypes;
			this.exact = exact;

			this.hashCode = methodName.length();
		}

		/**
		 * Checks for equality.
		 * @param obj object to be tested for equality
		 * @return true, if the object describes the same Method.
		 */
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof MethodDescriptor)) {
				return false;
			}
			MethodDescriptor md = (MethodDescriptor)obj;

			return (exact == md.exact && methodName.equals(md.methodName) && cls.equals(md.cls) && Arrays.equals(paramTypes, md.paramTypes));
		}

		/**
		 * Returns the string length of method name. I.e. if the
		 * hashcodes are different, the objects are different. If the
		 * hashcodes are the same, need to use the equals method to
		 * determine equality.
		 * @return the string length of method name.
		 */
		@Override
		public int hashCode() {
			return hashCode;
		}
	}

}
