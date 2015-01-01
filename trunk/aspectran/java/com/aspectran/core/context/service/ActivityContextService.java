package com.aspectran.core.context.service;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.ActivityContextLoader;
import com.aspectran.core.context.loader.config.AspectranConfig;
import com.aspectran.core.context.loader.reload.ActivityContextReloadDelegate;

public class ActivityContextService extends AbstractActivityContextService implements ActivityContextServiceController {

	private static final long DEFAULT_PAUSE_TIMEOUT = 500L;
	
	private ActivityContextServiceListener activityContextServiceListener;
	
	public ActivityContextService(AspectranConfig aspectranConfig, ActivityContextLoader activityContextLoader) {
		super(aspectranConfig, activityContextLoader);

		initActivityContext();
	}
	
	public void setActivityContextServiceListener(ActivityContextServiceListener activityContextServiceListener) {
		this.activityContextServiceListener = activityContextServiceListener;
	}
	
	protected ActivityContextReloadDelegate getActivityContextReloadDelegate() {
		return new ActivityContextReloadDelegate(this);
	}

	public synchronized ActivityContext start() {
		loadActivityContext();

		if(getApplicationAdapter().getActivityContextServiceController(activityContext) != this)
			getApplicationAdapter().putActivityContextServiceController(activityContext, this);

		if(activityContextServiceListener != null)
			activityContextServiceListener.started();

		return activityContext;
	}
	
	public synchronized boolean restart() {
		boolean cleanlyDestoryed = stop();
		
		reloadActivityContext();

		if(activityContextServiceListener != null)
			activityContextServiceListener.restarted();
		
		return cleanlyDestoryed;
	}

	public synchronized void pause() {
		if(activityContextServiceListener != null)
			activityContextServiceListener.paused(-1L);
	}
	
	public synchronized void pause(long timeout) {
		if(activityContextServiceListener != null)
			activityContextServiceListener.paused(timeout);
	}
	
	public synchronized void resume() {
		if(activityContextServiceListener != null)
			activityContextServiceListener.resumed();
	}

	public synchronized boolean stop() {
		if(activityContextServiceListener != null)
			activityContextServiceListener.paused(DEFAULT_PAUSE_TIMEOUT);
		
		boolean cleanlyDestoryed = destroyActivityContext();

		if(activityContextServiceListener != null)
			activityContextServiceListener.stopped();
		
		return cleanlyDestoryed;
	}
	
	public synchronized boolean dispose() {
		getApplicationAdapter().removeActivityContextServiceController(activityContext);
		
		return stop();
	}

}
