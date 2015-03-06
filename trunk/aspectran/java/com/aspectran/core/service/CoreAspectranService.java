package com.aspectran.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.loader.AspectranClassLoader;

public class CoreAspectranService extends AbstractAspectranService {

	private static final Logger logger = LoggerFactory.getLogger(CoreAspectranService.class);
	
	private static final long DEFAULT_PAUSE_TIMEOUT = 500L;
	
	private AspectranServiceControllerListener activityContextServiceListener;
	
	private boolean started;
	
	public synchronized ActivityContext start() {
		loadActivityContext();

		logger.info("AspectranService was started.");

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
		
		if(isHardReload())
			cleanlyDestoryed = dispose();
		else
			cleanlyDestoryed = stop();
		
		reloadActivityContext();
		
		started = true;
		
		logger.info("AspectranService was restarted.");

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
		
		boolean cleanlyDestoryed = destroyActivityContext();
			
		logger.info("AspectranService was stoped.");

		if(activityContextServiceListener != null)
			activityContextServiceListener.stopped();
		
		started = false;
		
		return cleanlyDestoryed;
	}
	
	public synchronized boolean dispose() {
		boolean cleanlyDestoryed = stop();
		
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
		}
		
		logger.info("AspectranService was disposed.");
		
		return cleanlyDestoryed;
	}
	
	public void setAspectranServiceControllerListener(AspectranServiceControllerListener activityContextServiceListener) {
		this.activityContextServiceListener = activityContextServiceListener;
	}
	
	public static AspectranClassLoader newAspectranClassLoader(String[] resourceLocations) {
		String[] excludePackageNames = new String[] {
				"com.aspectran.core",
				"com.aspectran.scheduler",
				"com.aspectran.support",
				"com.aspectran.web"
			};

		AspectranClassLoader acl = new AspectranClassLoader();
		acl.excludePackage(excludePackageNames);
		
		if(resourceLocations != null && resourceLocations.length > 0) {
			acl.setResourceLocations(resourceLocations);
		}
		
		return acl;
	}
	
}
