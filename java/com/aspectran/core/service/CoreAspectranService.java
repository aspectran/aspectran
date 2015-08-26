/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
	
	private AspectranServiceControllerListener activityContextServiceListener;
	
	private boolean started;
	
	public synchronized ActivityContext start() {
		loadActivityContext();

		log.info("AspectranService was started.");

		if(activityContextServiceListener != null)
			activityContextServiceListener.started();

		started = true;
		
		return activityContext;
	}
	
	public synchronized boolean restart() {
		if(!started) {
			log.debug("Cannot restart the AspectranService, because it is currently stopped.");
			return true;
		}

		boolean cleanlyDestoryed;
		
		if(isHardReload())
			cleanlyDestoryed = dispose();
		else
			cleanlyDestoryed = stop();
		
		reloadActivityContext();
		
		started = true;
		
		log.info("AspectranService was restarted.");

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
			
		log.info("AspectranService was stoped.");

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
				log.error("WebApplicationAdapter were not destroyed cleanly.", e);
			}
		}
		
		log.info("AspectranService was disposed.");
		
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
