package com.aspectran.core.context.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.CoreActivityException;
import com.aspectran.core.activity.VoidActivityImpl;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.aspect.AspectRuleRegistry;
import com.aspectran.core.context.bean.proxy.CglibDynamicBeanProxy;
import com.aspectran.core.context.bean.proxy.JdkDynamicBeanProxy;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.ReflectionUtils;
import com.aspectran.core.var.ValueMap;
import com.aspectran.core.var.rule.AspectRule;
import com.aspectran.core.var.rule.BeanRule;
import com.aspectran.core.var.rule.BeanRuleMap;
import com.aspectran.core.var.rule.ItemRule;
import com.aspectran.core.var.rule.ItemRuleMap;
import com.aspectran.core.var.rule.PointcutPatternRule;
import com.aspectran.core.var.token.ItemTokenExpression;
import com.aspectran.core.var.token.ItemTokenExpressor;
import com.aspectran.core.var.type.BeanProxyModeType;
import com.aspectran.core.var.type.JoinpointScopeType;
import com.aspectran.core.var.type.ScopeType;

/**
 * SINGLETON: 모든 singleton 빈은context 생성시 초기화 된다.
 * APPLICATION: 최초 참조시 초기화 된다.
 * 초기화 시점이 다르지만, 소멸 시점은 동일하다.(context 소멸시) 
 * <p>
 * Created: 2009. 03. 09 오후 23:48:09
 * </p>
 */
public abstract class AbstractContextBeanRegistry implements ContextBeanRegistry {
	
	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(AbstractBeanRegistryBak.class);
	
	protected final ActivityContext context;

	protected final BeanRuleMap beanRuleMap;

	private final BeanProxyModeType beanProxyMode;

	private final AspectRuleRegistry aspectRuleRegistry;
	
	private final Map<String, List<AspectRule>> aspectRuleListCache = new HashMap<String, List<AspectRule>>();
	
	public AbstractContextBeanRegistry(ActivityContext context, BeanRuleMap beanRuleMap, BeanProxyModeType beanProxyMode) {
		this.context = context;
		this.beanRuleMap = beanRuleMap;
		this.beanProxyMode = (beanProxyMode == null ? BeanProxyModeType.CGLIB_PROXY : beanProxyMode);
		this.aspectRuleRegistry = context.getAspectRuleRegistry();
		
		createSingletonBean();
	}
	
	private void createSingletonBean() {
		for(BeanRule beanRule : beanRuleMap) {
			if(!beanRule.isRegistered()) {
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
	}

	protected Object createBean(BeanRule beanRule) {
		CoreActivity activity = null;
		
		if(context != null)
			activity = context.getLocalCoreActivity();
		
		return createBean(beanRule, activity);
	}

	private Object createBean(BeanRule beanRule, CoreActivity activity) {
		try {
			ItemRuleMap constructorArgumentItemRuleMap = beanRule.getConstructorArgumentItemRuleMap();
			
			if(constructorArgumentItemRuleMap != null) {
				ItemTokenExpressor expressor = null;
				
				if(activity == null)
					expressor = new ItemTokenExpression(this);
				else
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
				
				return createBean(beanRule, argTypes, args, activity);
			} else {
				return createBean(beanRule, null, null, activity);
			}
		} catch(Exception e) {
			throw new BeanCreationException(beanRule, e);
		}
	}

	private Object createBean(BeanRule beanRule, Class<?>[] argTypes, Object[] args, CoreActivity activity) {
		/*
		 * 0. DynamicProxy 빈을 만들 것인지를 먼저 결정하라. 
		 * 1. 빈과 관련된 AspectRule을 추출하고, 캐슁하라.
		 * 2. 추출된 AspectRule을 AspectAdviceRegistry로 변환하고, DynamicProxy에 넘겨라
		 * 3. DynamicProxy에서 현재 실행 시점의 JoinScope에 따라 해당 JoinScope의 AspectAdviceRegistry에 Advice를 등록하라.
		 * 4. DynamicProxy에서 일치하는 메쏘드를 가진 AspectRule의 Advice를 실행하라.
		 */

		/*
		 * Bean과 관련된 AspectRule을 모두 추출.
		 */
		List<AspectRule> aspectRuleList = retrieveAspectRuleList(beanRule, activity);
		
		Object obj = null;
		
		if(aspectRuleList != null) {
			if(activity == null) {
				try {
					activity = new VoidActivityImpl(context);
					activity.ready(null);
				} catch(CoreActivityException e) {
					throw new BeanCreationException(beanRule, e);
				}
			}
			
			if(beanProxyMode == BeanProxyModeType.JDK_PROXY) {
				logger.debug("JdkDynamicBeanProxy " + beanRule);
				obj = JdkDynamicBeanProxy.newInstance(activity, aspectRuleList, beanRule, obj);
			} else {
				logger.debug("CglibDynamicBeanProxy " + beanRule);
				obj = CglibDynamicBeanProxy.newInstance(activity, aspectRuleList, beanRule, argTypes, args);
			}
		} else {
			if(argTypes != null && args != null)
				obj = newInstance(beanRule, argTypes, args);
			else
				obj = newInstance(beanRule, new Class[0], new Object[0]);
		}
		
		return obj;
	}
	
	/**
	 * Retrieve all Aaspect Rules associated with a bean.
	 * Bean과 관련된 모든 AspectRule을 모두 추출하라.
	 *
	 * @param activity the activity
	 * @param beanRule the bean rule
	 * @return the list
	 */
	private List<AspectRule> retrieveAspectRuleList(BeanRule beanRule, CoreActivity activity) {
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
		
		String joinpointScopeString = joinpointScope == null ? null : joinpointScope.toString();
		String patternString = PointcutPatternRule.combinePatternString(joinpointScopeString, transletName, beanId, null);

		List<AspectRule> aspectRuleList;
		
		synchronized(aspectRuleListCache) {
			aspectRuleList = aspectRuleListCache.get(patternString);
			
			if(aspectRuleList == null) {
				aspectRuleList = aspectRuleRegistry.getBeanRelevantedAspectRuleList(joinpointScope, transletName, beanId);
				aspectRuleListCache.put(patternString, aspectRuleList);
			}
		}
		
		if(aspectRuleList.size() == 0)
			return null;
		else
			return aspectRuleList;
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
	
	private static Object newInstance(BeanRule beanRule, Class<?>[] argTypes, Object[] args) throws BeanInstantiationException {
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
			throw new BeanInstantiationException(ctor.getDeclaringClass(),
					"Has the class definition changed? Is the constructor accessible?", ex);
		} catch(IllegalArgumentException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(), "Illegal arguments for constructor", ex);
		} catch(InvocationTargetException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(), "Constructor threw exception", ex
					.getTargetException());
		}
	}
	
	private static Constructor<?> getMatchConstructor(Class<?> clazz, Object[] args) {
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
