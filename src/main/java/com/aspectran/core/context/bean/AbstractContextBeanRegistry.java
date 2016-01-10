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
package com.aspectran.core.context.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.VoidActivity;
import com.aspectran.core.activity.process.action.BeanAction;
import com.aspectran.core.activity.variable.ValueMap;
import com.aspectran.core.activity.variable.token.ItemTokenExpression;
import com.aspectran.core.activity.variable.token.ItemTokenExpressor;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.ablility.FactoryBean;
import com.aspectran.core.context.bean.proxy.CglibDynamicBeanProxy;
import com.aspectran.core.context.bean.proxy.JavassistDynamicBeanProxy;
import com.aspectran.core.context.bean.proxy.JdkDynamicBeanProxy;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.BeanRuleMap;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.BeanProxifierType;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AbstractContextBeanRegistry.
 * 
 * <p>Created: 2009. 03. 09 오후 23:48:09</p>
 */
public abstract class AbstractContextBeanRegistry implements ContextBeanRegistry {
	
	private final Log log = LogFactory.getLog(AbstractContextBeanRegistry.class);
	
	protected final ActivityContext context;

	protected final BeanRuleMap beanRuleMap;

	protected final Map<Class<?>, BeanRule> classBeanRuleMap;

	private final BeanProxifierType beanProxifierType;

	private final Map<String, List<AspectRule>> aspectRuleListCache = new HashMap<String, List<AspectRule>>();
	
	private boolean initialized;
	
	public AbstractContextBeanRegistry(ActivityContext context, BeanRuleMap beanRuleMap, BeanProxifierType beanProxifierType) {
		this.context = context;
		this.beanRuleMap = beanRuleMap;
		this.classBeanRuleMap = beanRuleMap.getClassBeanRuleMap();
		this.beanProxifierType = (beanProxifierType == null ? BeanProxifierType.JAVASSIST : beanProxifierType);
	}
	
	protected Object createBean(BeanRule beanRule) {
		Activity activity = context.getCurrentActivity();
		return createBean(beanRule, activity);
	}

	private Object createBean(BeanRule beanRule, Activity activity) {
		try {
			Object bean;
			
			ItemRuleMap constructorArgumentItemRuleMap = beanRule.getConstructorArgumentItemRuleMap();
			ItemRuleMap propertyItemRuleMap = beanRule.getPropertyItemRuleMap();
			ItemTokenExpressor expressor = null;
			
			if(constructorArgumentItemRuleMap != null) {
				expressor = new ItemTokenExpression(activity);
				
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
				
				bean = instantiateBean(beanRule, argTypes, args);
			} else {
				bean = instantiateBean(beanRule, null, null);
			}

			if(propertyItemRuleMap != null) {
				if(expressor == null) {
					expressor = new ItemTokenExpression(activity);
				}
				
				ValueMap valueMap = expressor.express(propertyItemRuleMap);
				
				for(Map.Entry<String, Object> entry : valueMap.entrySet()) {
					MethodUtils.invokeSetter(bean, entry.getKey(), entry.getValue());
				}
			}
			
			String initMethodName = beanRule.getInitMethodName();
			
			if(initMethodName != null) {
				if(beanRule.getInitMethodRequiresTranslet() == null) {
					try {
						BeanAction.invokeMethod(activity, bean, initMethodName, null, null, true);
						beanRule.setInitMethodRequiresTranslet(Boolean.TRUE);
					} catch(NoSuchMethodException e) {
						log.info("Cannot find a method that requires a argument translet. So in the future will continue to call a method with no argument translet. beanActionRule " + beanRule);
						
						beanRule.setInitMethodRequiresTranslet(Boolean.FALSE);
						BeanAction.invokeMethod(activity, bean, initMethodName, null, null, false);
					}
				} else {
					BeanAction.invokeMethod(activity, bean, initMethodName, null, null, beanRule.getInitMethodRequiresTranslet().booleanValue());
				}
			}

			if(beanRule.isFactoryBeanImplmented()) {
				FactoryBean factory = (FactoryBean)bean;

				try {
					bean = factory.getObject();
				} catch(Exception ex) {
					throw new BeanCreationException(beanRule, "FactoryBean threw exception on object creation", ex);
				}

				if(bean == null) {
					throw new FactoryBeanNotInitializedException(beanRule,
									"FactoryBean returned null object: " +
									"probably not fully initialized (maybe due to circular bean reference)");
				}
			}

			return bean;
		} catch(Exception e) {
			throw new BeanCreationException(beanRule, e);
		}
	}

	private Object instantiateBean(BeanRule beanRule, Class<?>[] argTypes, Object[] args) {
		Object bean;
		
		if(beanRule.isProxied()) {
			if(beanProxifierType == BeanProxifierType.JAVASSIST) {
				if(log.isTraceEnabled())
					log.trace("JavassistDynamicBeanProxy " + beanRule);
				
				bean = JavassistDynamicBeanProxy.newInstance(context, beanRule, argTypes, args);
			} else if(beanProxifierType == BeanProxifierType.CGLIB) {
				if(log.isTraceEnabled())
					log.trace("CglibDynamicBeanProxy " + beanRule);
				
				bean = CglibDynamicBeanProxy.newInstance(context, beanRule, argTypes, args);
			} else {
				if(argTypes != null && args != null)
					bean = newInstance(beanRule.getBeanClass(), argTypes, args);
				else
					bean = newInstance(beanRule.getBeanClass());
				
				if(log.isTraceEnabled())
					log.trace("JdkDynamicBeanProxy " + beanRule);
				
				bean = JdkDynamicBeanProxy.newInstance(context, beanRule, bean);
			}
		} else {
			if(argTypes != null && args != null)
				bean = newInstance(beanRule.getBeanClass(), argTypes, args);
			else
				bean = newInstance(beanRule.getBeanClass());
		}
		
		return bean;
	}
	
	public synchronized void initialize() {
		if(initialized) {
			throw new UnsupportedOperationException("ContextBeanRegistry has already been initialized.");
		}

		CoreActivity activity = new VoidActivity(context);
		context.setCurrentActivity(activity);

		for(BeanRule beanRule : beanRuleMap) {
			if(!beanRule.isRegistered()) {
				ScopeType scope = beanRule.getScopeType();
	
				if(scope == ScopeType.SINGLETON) {
					if(!beanRule.isRegistered() && !beanRule.isLazyInit()) {
						Object bean = createBean(beanRule, activity);
						beanRule.setBean(bean);
						beanRule.setRegistered(true);
					}
				}
			}
		}
		
		context.removeCurrentActivity();
		
		initialized = true;
		
		log.info("ContextBeanRegistry has been initialized successfully.");
	}
	
	public synchronized void destroy() {
		if(!initialized) {
			throw new UnsupportedOperationException("ContextBeanRegistry has not yet initialized.");
		}

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
		
		aspectRuleListCache.clear();
		
		initialized = false;
		
		log.info("ContextBeanRegistry has been destroyed successfully.");
	}

	private static Object newInstance(Class<?> beanClass, Class<?>[] argTypes, Object[] args) throws BeanInstantiationException {
		Constructor<?> constructorToUse;

		try {
			constructorToUse = getMatchConstructor(beanClass, args);

			if(constructorToUse == null) {
				constructorToUse = beanClass.getDeclaredConstructor(argTypes);
			}
		} catch(NoSuchMethodException e) {
			throw new BeanInstantiationException(beanClass, "No default constructor found.", e);
		}

		Object obj = newInstance(constructorToUse, args);

		return obj;
	}

	private static Object newInstance(Class<?> beanClass) throws BeanInstantiationException {
		return newInstance(beanClass, MethodUtils.EMPTY_CLASS_PARAMETERS, MethodUtils.EMPTY_OBJECT_ARRAY);
	}

	private static Object newInstance(Constructor<?> ctor, Object[] args) throws BeanInstantiationException {
		try {
			if(!Modifier.isPublic(ctor.getModifiers()) ||
					!Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) {
				ctor.setAccessible(true);
			}
	
			return ctor.newInstance(args);
		} catch(InstantiationException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(), "Is it an abstract class?", ex);
		} catch(IllegalAccessException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(), "Has the class definition changed? Is the constructor accessible?", ex);
		} catch(IllegalArgumentException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(), "Illegal arguments for constructor", ex);
		} catch(InvocationTargetException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(), "Constructor threw exception", ex.getTargetException());
		}
	}

	private static Constructor<?> getMatchConstructor(Class<?> clazz, Object[] args) {
		Constructor<?>[] candidates = clazz.getDeclaredConstructors();

		Constructor<?> constructorToUse = null;
		float bestMatchWeight = Float.MAX_VALUE;
		float matchWeight = Float.MAX_VALUE;
		
		for(Constructor<?> candidate : candidates) {
			matchWeight = ClassUtils.getTypeDifferenceWeight(candidate.getParameterTypes(), args);
			
			if(matchWeight < bestMatchWeight) {
				constructorToUse = candidate;
				bestMatchWeight = matchWeight;
			}
		}
		
		return constructorToUse;
	}
	
}
