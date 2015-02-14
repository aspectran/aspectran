package com.aspectran.core.adapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.ApplicationScope;
import com.aspectran.core.service.AspectranServiceController;

/**
 * The Class AbstractApplicationAdapter.
  *
 * @since 2011. 3. 13.
*/
public abstract class AbstractApplicationAdapter implements ApplicationAdapter {
	
	/** The adaptee. */
	protected Object adaptee;

	protected ApplicationScope scope = new ApplicationScope();

	protected Map<ActivityContext, AspectranServiceController> aspectranServiceControllers = Collections.synchronizedMap(new HashMap<ActivityContext, AspectranServiceController>());
	
	/**
	 * Instantiates a new abstract application adapter.
	 *
	 * @param adaptee the adaptee
	 */
	public AbstractApplicationAdapter(Object adaptee) {
		this.adaptee = adaptee;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getAdaptee() {
		return (T)adaptee;
	}
	
	public ApplicationScope getScope() {
		return scope;
	}

	public Map<ActivityContext, AspectranServiceController> getActivityContextServiceHandlers() {
		return aspectranServiceControllers;
	}
	
	public AspectranServiceController getAspectranServiceController(ActivityContext activityContext) {
		return aspectranServiceControllers.get(activityContext);
	}

	public void putAspectranServiceController(ActivityContext activityContext, AspectranServiceController aspectranServiceController) {
		aspectranServiceControllers.put(activityContext, aspectranServiceController);
	}

	public void removeActivityContextServiceController(ActivityContext activityContext) {
		if(activityContext != null)
			aspectranServiceControllers.remove(activityContext);
	}
	
}
