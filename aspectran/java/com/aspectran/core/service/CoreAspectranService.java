package com.aspectran.core.service;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.config.AspectranConfig;

public class CoreAspectranService extends AbstractAspectranService {

	private static final long DEFAULT_PAUSE_TIMEOUT = 500L;
	
	private AspectranServiceListener activityContextServiceListener;
	
	public CoreAspectranService(AspectranConfig aspectranConfig) {
		super(aspectranConfig);

		initActivityContext();
	}
	
	public void setActivityContextServiceListener(AspectranServiceListener activityContextServiceListener) {
		this.activityContextServiceListener = activityContextServiceListener;
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
