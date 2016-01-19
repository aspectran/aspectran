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
package com.aspectran.core.service;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

public class CoreAspectranService extends AbstractAspectranService {

	private static final Log log = LogFactory.getLog(CoreAspectranService.class);
	
	private static final long DEFAULT_PAUSE_TIMEOUT = 321L;
	
	private AspectranServiceControllerListener aspectranServiceControllerListener;
	
	private boolean started;
	
	public synchronized ActivityContext startup() {
		loadActivityContext();

		log.info("AspectranService was started successfully.");

		if(aspectranServiceControllerListener != null)
			aspectranServiceControllerListener.started();

		started = true;
		
		return activityContext;
	}
	
	public synchronized boolean refresh() {
		if(!started) {
			log.debug("Cannot refresh AspectranService, because it is currently stopped.");
			return true;
		}

		boolean cleanlyDestoryed;
		
		if(isHardReload())
			cleanlyDestoryed = dispose();
		else
			cleanlyDestoryed = stop();
		
		reloadActivityContext();
		
		started = true;
		
		log.info("AspectranService was refreshed.");

		if(aspectranServiceControllerListener != null)
			aspectranServiceControllerListener.refreshed();
		
		return cleanlyDestoryed;
	}

	public synchronized void pause() {
		if(aspectranServiceControllerListener != null)
			aspectranServiceControllerListener.paused(-1L);
	}
	
	public synchronized void pause(long timeout) {
		if(aspectranServiceControllerListener != null)
			aspectranServiceControllerListener.paused(timeout);
	}
	
	public synchronized void resume() {
		if(aspectranServiceControllerListener != null)
			aspectranServiceControllerListener.resumed();
	}

	public synchronized boolean stop() {
		if(!started)
			return true;

		if(aspectranServiceControllerListener != null)
			aspectranServiceControllerListener.paused(DEFAULT_PAUSE_TIMEOUT);
		
		boolean cleanlyDestoryed = destroyActivityContext();
			
		log.info("AspectranService was stopped.");

		if(aspectranServiceControllerListener != null)
			aspectranServiceControllerListener.stopped();
		
		started = false;
		
		return cleanlyDestoryed;
	}
	
	public synchronized boolean dispose() {
		boolean cleanlyDestoryed = stop();
		
		ApplicationAdapter applicationAdapter = getApplicationAdapter();
		
		if(applicationAdapter != null) {
			try {
				Scope scope = getApplicationAdapter().getApplicationScope();
		
				if(scope != null)
					scope.destroy();
			} catch(Exception e) {
				cleanlyDestoryed = false;
				log.error("ApplicationAdapter Scope has not been destroyed cleanly.", e);
			}
		}
		
		log.info("AspectranService has been destroyed.");
		
		return cleanlyDestoryed;
	}
	
	public void setAspectranServiceControllerListener(AspectranServiceControllerListener aspectranServiceControllerListener) {
		this.aspectranServiceControllerListener = aspectranServiceControllerListener;
	}
	
	public static AspectranClassLoader newAspectranClassLoader(String[] resourceLocations) {
		String[] excludePackageNames = new String[] {
				"com.aspectran.core",
				"com.aspectran.scheduler",
				"com.aspectran.web",
				"com.aspectran.console"
			};

		AspectranClassLoader acl = new AspectranClassLoader();
		acl.excludePackage(excludePackageNames);
		
		if(resourceLocations != null && resourceLocations.length > 0) {
			acl.setResourceLocations(resourceLocations);
		}
		
		return acl;
	}
	
}
