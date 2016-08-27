/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.activity;

import java.lang.reflect.Constructor;
import java.util.List;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.aspect.AspectAdviceRulePostRegister;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.aspect.AspectRuleRegistry;
import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.context.bean.scope.RequestScope;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.JoinpointType;
import com.aspectran.core.context.template.TemplateProcessor;
import com.aspectran.core.context.translet.TransletInstantiationException;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AbstractActivity.
 * 
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public abstract class AbstractActivity implements Activity {

	private static final Log log = LogFactory.getLog(AbstractActivity.class);
	
	private final ActivityContext context;

	private boolean included;

	private Activity outerActivity;
	
	/** Whether the current activity is completed or interrupted. */
	private boolean activityEnded;

	private Throwable raisedException;

	private SessionAdapter sessionAdapter;
	
	private RequestAdapter requestAdapter;

	private ResponseAdapter responseAdapter;
	
	private Class<? extends Translet> transletInterfaceClass;
	
	private Class<? extends CoreTranslet> transletImplementClass;

	private Scope requestScope;
	
	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;
	
	/**
	 * Instantiates a new abstract activity.
	 *
	 * @param context the activity context
	 */
	protected AbstractActivity(ActivityContext context) {
		this.context = context;
	}

	@Override
	public ActivityContext getActivityContext() {
		return context;
	}

	/**
	 * Gets the current activity.
	 *
	 * @return the current activity
	 */
	protected Activity getCurrentActivity() {
		return context.getCurrentActivity();
	}
	
	/**
	 * Sets the current activity.
	 *
	 * @param activity the new current activity
	 */
	protected void setCurrentActivity(Activity activity) {
		context.setCurrentActivity(activity);
	}

	protected void backupCurrentActivity() {
		outerActivity = getCurrentActivity();
		setCurrentActivity(this);
	}

	/**
	 * Removes the current activity.
	 */
	protected void removeCurrentActivity() {
		if(outerActivity != null) {
			setCurrentActivity(outerActivity);
		} else {
			removeCurrentActivity();
		}
	}
	
	/**
	 * Returns whether or not contained in other activity.
	 *
	 * @return true, if this activity is included in the other activity
	 */
	public boolean isIncluded() {
		return included;
	}

	/**
	 * Sets whether this activity is included in other activity.
	 *
	 * @param included whether or not included in other activity
	 */
	public void setIncluded(boolean included) {
		this.included = included;
	}

	public boolean isActivityEnded() {
		return activityEnded;
	}

	public void activityEnd() {
		this.activityEnded = true;
	}
	
	protected void continueActivity() {
		this.activityEnded = false;
	}

	@Override
	public Throwable getRaisedException() {
		return raisedException;
	}

	@Override
	public void setRaisedException(Throwable raisedException) {
		if(this.raisedException == null) {
			if(log.isDebugEnabled()) {
				log.error("Raised exception: ", raisedException);
			}
			this.raisedException = raisedException;
		}
	}

	@Override
	public boolean isExceptionRaised() {
		return (this.raisedException != null);
	}

	@Override
	public Throwable getOriginRaisedException() {
		if(raisedException != null) {
			for(Throwable t = raisedException; t != null; t = t.getCause()) {
				if(t.getCause() == null)
					return t;
			}
		}
		return null;
	}

	/**
	 * Gets the application adapter.
	 *
	 * @return the application adapter
	 */
	public ApplicationAdapter getApplicationAdapter() {
		return context.getApplicationAdapter();
	}

	/**
	 * Gets the session adapter.
	 *
	 * @return the session adapter
	 */
	public SessionAdapter getSessionAdapter() {
		return sessionAdapter;
	}
	
	/**
	 * Sets the session adapter.
	 *
	 * @param sessionAdapter the new session adapter
	 */
	protected void setSessionAdapter(SessionAdapter sessionAdapter) {
		this.sessionAdapter = sessionAdapter;
	}

	/**
	 * Gets the request adapter.
	 *
	 * @return the request adapter
	 */
	public RequestAdapter getRequestAdapter() {
		return requestAdapter;
	}
	
	/**
	 * Sets the request adapter.
	 *
	 * @param requestAdapter the new request adapter
	 */
	protected void setRequestAdapter(RequestAdapter requestAdapter) {
		this.requestAdapter = requestAdapter;
	}
	
	/**
	 * Gets the response adapter.
	 *
	 * @return the response adapter
	 */
	public ResponseAdapter getResponseAdapter() {
		return responseAdapter;
	}

	/**
	 * Sets the response adapter.
	 *
	 * @param responseAdapter the new response adapter
	 */
	protected void setResponseAdapter(ResponseAdapter responseAdapter) {
		this.responseAdapter = responseAdapter;
	}

	/**
	 * Gets the translet interface class.
	 *
	 * @return the translet interface class
	 */
	public Class<? extends Translet> getTransletInterfaceClass() {
		return transletInterfaceClass;
	}

	/**
	 * Sets the translet interface class.
	 *
	 * @param transletInterfaceClass the new translet interface class
	 */
	protected void setTransletInterfaceClass(Class<? extends Translet> transletInterfaceClass) {
		this.transletInterfaceClass = transletInterfaceClass;
	}

	/**
	 * Gets the translet implement class.
	 *
	 * @return the translet implement class
	 */
	public Class<? extends CoreTranslet> getTransletImplementationClass() {
		return transletImplementClass;
	}

	/**
	 * Sets the translet implement class.
	 *
	 * @param transletImplementClass the new translet implement class
	 */
	protected void setTransletImplementationClass(Class<? extends CoreTranslet> transletImplementClass) {
		this.transletImplementClass = transletImplementClass;
	}
	
	/**
	 * Create a new {@code Translet} instance.
	 *
	 * @return the new {@code Translet} instance
	 */
	protected Translet newTranslet() {
		if(this.transletInterfaceClass == null) {
			this.transletInterfaceClass = Translet.class;
		}
		if(this.transletImplementClass == null) {
			this.transletImplementClass = CoreTranslet.class;
			return new CoreTranslet(this);
		}
		
		//create a custom translet instance
		try {
			Constructor<?> transletImplementConstructor = transletImplementClass.getConstructor(Activity.class);
			Object[] args = new Object[] { this };
			
			return (Translet)transletImplementConstructor.newInstance(args);
		} catch(Exception e) {
			throw new TransletInstantiationException(transletImplementClass, e);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Activity> T newActivity() {
		CoreActivity activity = new CoreActivity(context);
		activity.setIncluded(true);
		return (T)activity;
	}

	@Override
	public String determineRequestCharacterEncoding() {
		String characterEncoding = getRequestRule().getCharacterEncoding();
		if(characterEncoding == null) {
			characterEncoding = getSetting(RequestRule.CHARACTER_ENCODING_SETTING_NAME);
		}
		return characterEncoding;
	}

	@Override
	public String determineResponseCharacterEncoding() {
		String characterEncoding = getResponseRule().getCharacterEncoding();
		if(characterEncoding == null) {
			characterEncoding = determineRequestCharacterEncoding();
		}
		return characterEncoding;
	}

	/**
	 * Gets the request scope.
	 *
	 * @return the request scope
	 */
	public Scope getRequestScope() {
		return getRequestScope(true);
	}

	/**
	 * Gets the request scope.
	 *
	 * @param create {@code true} to create a new reqeust scope for this
	 * 		request if necessary; {@code false} to return {@code null}
	 * @return the request scope
	 */
	public Scope getRequestScope(boolean create) {
		if(requestScope == null && create) {
			requestScope = new RequestScope();
		}
		return requestScope;
	}

	/**
	 * Sets the request scope.
	 *
	 * @param requestScope the new request scope
	 */
	public void setRequestScope(Scope requestScope) {
		this.requestScope = requestScope;
	}
	
	protected void prepareAspectAdviceRule(TransletRule transletRule) {
		if(transletRule.getNameTokens() == null) {
			this.aspectAdviceRuleRegistry = transletRule.replicateAspectAdviceRuleRegistry();
		} else {
			AspectAdviceRulePostRegister aarPostRegister = new AspectAdviceRulePostRegister();

			for(AspectRule aspectRule : getAspectRuleRegistry().getAspectRules()) {
				JoinpointType joinpointScope = aspectRule.getJoinpointType();

				if(!aspectRule.isBeanRelevanted() && joinpointScope != JoinpointType.SESSION) {
					if(isAcceptable(aspectRule)) {
						Pointcut pointcut = aspectRule.getPointcut();
						if(pointcut == null || pointcut.matches(transletRule.getName())) {
							aarPostRegister.register(aspectRule);

							if(log.isDebugEnabled()) {
								log.debug("registered AspectRule " + aspectRule);
							}
						}
					}
				}
			}

			this.aspectAdviceRuleRegistry = aarPostRegister.getAspectAdviceRuleRegistry();
		}
	}
	
	public AspectAdviceRuleRegistry touchAspectAdviceRuleRegistry() {
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
		}
		return aspectAdviceRuleRegistry;
	}
	
	protected List<AspectAdviceRule> getBeforeAdviceRuleList() {
		if(aspectAdviceRuleRegistry != null) {
			return aspectAdviceRuleRegistry.getBeforeAdviceRuleList();
		}
		return null;
	}

	protected List<AspectAdviceRule> getAfterAdviceRuleList() {
		if(aspectAdviceRuleRegistry != null) {
			return aspectAdviceRuleRegistry.getAfterAdviceRuleList();
		}
		return null;
	}
	
	protected List<AspectAdviceRule> getFinallyAdviceRuleList() {
		if(aspectAdviceRuleRegistry != null) {
			return aspectAdviceRuleRegistry.getFinallyAdviceRuleList();
		}
		return null;
	}

	protected List<ExceptionRule> getExceptionRuleList() {
		if(aspectAdviceRuleRegistry != null) {
			return aspectAdviceRuleRegistry.getExceptionRuleList();
		}
		return null;
	}

	@Override
	public <T> T getSetting(String settingName) {
		return (aspectAdviceRuleRegistry != null) ? aspectAdviceRuleRegistry.getSetting(settingName) : null;
	}
	
	/**
	 * Gets the aspect rule registry.
	 *
	 * @return the aspect rule registry
	 */
	protected AspectRuleRegistry getAspectRuleRegistry() {
		return context.getAspectRuleRegistry();
	}

	/**
	 * Gets the translet rule registry.
	 *
	 * @return the translet rule registry
	 */
	protected TransletRuleRegistry getTransletRuleRegistry() {
		return context.getTransletRuleRegistry();
	}

	@Override
	public TemplateProcessor getTemplateProcessor() {
		return context.getTemplateProcessor();
	}

	@Override
	public BeanRegistry getBeanRegistry() {
		return context.getBeanRegistry();
	}

	@Override
	public <T> T getBean(String id) {
		return context.getBeanRegistry().getBean(id);
	}

	@Override
	public <T> T getBean(Class<T> requiredType) {
		return context.getBeanRegistry().getBean(requiredType);
	}

	@Override
	public <T> T getBean(String id, Class<T> requiredType) {
		return context.getBeanRegistry().getBean(id, requiredType);
	}

	@Override
	public <T> T getBean(Class<T> requiredType, String id) {
		return context.getBeanRegistry().getBean(requiredType, id);
	}

	@Override
	public <T> T getConfigBean(Class<T> classType) {
		return context.getBeanRegistry().getConfigBean(classType);
	}

	@Override
	public boolean containsBean(String id) {
		return context.getBeanRegistry().containsBean(id);
	}

	@Override
	public boolean containsBean(Class<?> requiredType) {
		return context.getBeanRegistry().containsBean(requiredType);
	}
	
	@Override
	public boolean isAcceptable(AspectRule aspectRule) {
		if(aspectRule.getTargetMethods() != null) {
			if(getRequestMethod() == null || !getRequestMethod().containsTo(aspectRule.getTargetMethods()))
				return false;
		}

		if(aspectRule.getTargetHeaders() != null) {
			boolean contained = false;
			for(String header : aspectRule.getTargetHeaders()) {
				if(getRequestAdapter().containsHeader(header)) {
					contained = true;
					break;
				}
			}
			if(!contained)
				return false;
		}

		return true;
	}

	@Override
	public void registerAspectRule(AspectRule aspectRule) {
		if(!isAcceptable(aspectRule))
			return;

		JoinpointType joinpointType = aspectRule.getJoinpointType();

		/*
		 * The before advice is excluded because it was already executed.
		 */
		if(joinpointType == JoinpointType.TRANSLET) {
			if(log.isDebugEnabled()) {
				log.debug("register AspectRule " + aspectRule);
			}
			
			touchAspectAdviceRuleRegistry().register(aspectRule, AspectAdviceType.BEFORE);

			List<AspectAdviceRule> aspectAdviceRuleList = aspectRule.getAspectAdviceRuleList();
			if(aspectAdviceRuleList != null) {
				for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
					if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.BEFORE) {
						execute(aspectAdviceRule);
					}
				}
			}
		}
	}
	
}
