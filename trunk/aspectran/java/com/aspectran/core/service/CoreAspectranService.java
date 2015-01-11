package com.aspectran.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.Scope;

public class CoreAspectranService extends AbstractAspectranService {

	private static final Logger logger = LoggerFactory.getLogger(CoreAspectranService.class);
	
	private static final long DEFAULT_PAUSE_TIMEOUT = 500L;
	
	private AspectranService rootAspectranService;
	
	private AspectranServiceControllerListener activityContextServiceListener;
	
	private boolean started;
	
	public CoreAspectranService() {
	}
	
	public CoreAspectranService(AspectranService rootAspectranService) {
		this.rootAspectranService = rootAspectranService;
		super.aspectranClassLoader = rootAspectranService.getAspectranClassLoader();
		super.applicationAdapter = rootAspectranService.getApplicationAdapter();
		super.activityContext = rootAspectranService.getActivityContext();
	}
	
	public void setAspectranServiceControllerListener(AspectranServiceControllerListener activityContextServiceListener) {
		this.activityContextServiceListener = activityContextServiceListener;
	}
	
	public synchronized ActivityContext start() {
		if(rootAspectranService == null) {
			loadActivityContext();
	
			if(getApplicationAdapter().getAspectranServiceController(activityContext) != this)
				getApplicationAdapter().putAspectranServiceController(activityContext, this);
			
			logger.info("AspectranService was started.");
		}

		if(activityContextServiceListener != null)
			activityContextServiceListener.started();

		started = true;
		
		return activityContext;
	}
	
	public synchronized boolean restart() {
		if(!started) {
			logger.debug("Cannot restart the AspectranService, because it is currently stopped.");
			return true;
		}

		boolean cleanlyDestoryed;
		
		if(rootAspectranService == null) {
			cleanlyDestoryed = stop();
			
			reloadActivityContext();
			
			logger.info("AspectranService was restarted.");
		} else {
			cleanlyDestoryed = rootAspectranService.restart();
			
			if(rootAspectranService.isHardReload())
				super.aspectranClassLoader = rootAspectranService.getAspectranClassLoader();
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
		if(!started)
			return true;

		if(activityContextServiceListener != null)
			activityContextServiceListener.paused(DEFAULT_PAUSE_TIMEOUT);
		
		boolean cleanlyDestoryed;
		
		if(rootAspectranService == null) {
			cleanlyDestoryed = destroyActivityContext();
			
			logger.info("AspectranService was stoped.");
		} else {
			cleanlyDestoryed = rootAspectranService.stop();
		}

		if(activityContextServiceListener != null)
			activityContextServiceListener.stopped();
		
		started = false;
		
		return cleanlyDestoryed;
	}
	
	public synchronized boolean dispose() {
		boolean cleanlyDestoryed = stop();
		
		if(rootAspectranService == null) {
			ApplicationAdapter applicationAdapter = getApplicationAdapter();
			
			if(applicationAdapter != null) {
				try {
					Scope scope = getApplicationAdapter().getScope();
			
					if(scope != null)
						scope.destroy();
				} catch(Exception e) {
					cleanlyDestoryed = false;
					logger.error("WebApplicationAdapter were not destroyed cleanly.", e);
				}

				applicationAdapter.removeActivityContextServiceController(activityContext);
			}
			
			logger.info("AspectranService was disposed.");
		}
		
		return cleanlyDestoryed;
	}

}
