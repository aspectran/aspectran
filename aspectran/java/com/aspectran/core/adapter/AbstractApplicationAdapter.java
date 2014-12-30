package com.aspectran.core.adapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.ApplicationScope;
import com.aspectran.core.context.service.ActivityContextServiceHandler;

/**
 * The Class AbstractApplicationAdapter.
  *
 * @since 2011. 3. 13.
*/
public abstract class AbstractApplicationAdapter implements ApplicationAdapter {
	
	protected ApplicationScope scope = new ApplicationScope();
	
	/** The adaptee. */
	protected Object adaptee;
	
	protected Map<ActivityContext, ActivityContextServiceHandler> activityContextServiceHandlers = Collections.synchronizedMap(new HashMap<ActivityContext, ActivityContextServiceHandler>());
	
	/**
	 * Instantiates a new abstract session adapter.
	 *
	 * @param adaptee the adaptee
	 */
	public AbstractApplicationAdapter(Object adaptee) {
		this.adaptee = adaptee;
	}
	
	public ApplicationScope getScope() {
		return scope;
	}

	public Object getAdaptee() {
		return adaptee;
	}
	
	public abstract Object getAttribute(String name);

	public abstract void setAttribute(String name, Object value);
	
	
	public Map<ActivityContext, ActivityContextServiceHandler> getActivityContextServiceHandlers() {
		return activityContextServiceHandlers;
	}
	
	public ActivityContextServiceHandler getActivityContextServiceHandler(ActivityContext activityContext) {
		return activityContextServiceHandlers.get(activityContext);
	}

	public void putActivityContextServiceHandler(ActivityContext activityContext, ActivityContextServiceHandler activityContextServiceHandler) {
		activityContextServiceHandlers.put(activityContext, activityContextServiceHandler);
	}

	public void removeActivityContextServiceHandler(ActivityContext activityContext) {
		if(activityContext != null)
			activityContextServiceHandlers.remove(activityContext);
	}
	
}
