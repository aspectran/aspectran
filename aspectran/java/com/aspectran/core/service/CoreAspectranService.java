package com.aspectran.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.Scope;

public class CoreAspectranService extends AbstractAspectranService {

	private final Logger logger = LoggerFactory.getLogger(CoreAspectranService.class);
	
	private static final long DEFAULT_PAUSE_TIMEOUT = 500L;
	
	private AspectranService aspectranService;
	
	private AspectranServiceControllerListener activityContextServiceListener;
	
	public CoreAspectranService() {
	}
	
	public CoreAspectranService(AspectranService aspectranService) {
		this.aspectranService = aspectranService;
		setAspectranClassLoader(aspectranService.getAspectranClassLoader());
		setApplicationAdapter(aspectranService.getApplicationAdapter());
	}
	
	public void setAspectranServiceControllerListener(AspectranServiceControllerListener activityContextServiceListener) {
		this.activityContextServiceListener = activityContextServiceListener;
	}
	
	public synchronized ActivityContext start() {
		if(aspectranService == null) {
			loadActivityContext();
	
			if(getApplicationAdapter().getAspectranServiceController(activityContext) != this)
				getApplicationAdapter().putAspectranServiceController(activityContext, this);
		} else {
			aspectranService.start();
		}

		if(activityContextServiceListener != null)
			activityContextServiceListener.started();

		return activityContext;
	}
	
	public synchronized boolean restart() {
		boolean cleanlyDestoryed;
		
		if(aspectranService == null) {
			cleanlyDestoryed = stop();
			
			reloadActivityContext();
		} else {
			cleanlyDestoryed = aspectranService.restart();
		}

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
		
		boolean cleanlyDestoryed;
		
		if(aspectranService == null) {
			cleanlyDestoryed = destroyActivityContext();
		} else {
			cleanlyDestoryed = aspectranService.stop();
		}

		if(activityContextServiceListener != null)
			activityContextServiceListener.stopped();
		
		return cleanlyDestoryed;
	}
	
	public synchronized boolean dispose() {
		boolean cleanlyDestoryed;
		
		if(getApplicationAdapter() != null) {
			try {
				Scope scope = getApplicationAdapter().getScope();
		
				if(scope != null)
					scope.destroy();
			} catch(Exception e) {
				cleanlyDestoryed = false;
				logger.error("WebApplicationAdapter were not destroyed cleanly.", e);
			}
		}
		
		if(aspectranService == null) {
			getApplicationAdapter().removeActivityContextServiceController(activityContext);
		}
		
		cleanlyDestoryed = stop();
		
		return cleanlyDestoryed;
	}
	
	public AspectranService createWrapperAspectranService() {
		return new CoreAspectranService(this);
	}

}
