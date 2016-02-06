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
package com.aspectran.core.service;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.context.loader.resource.InvalidResourceException;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class CoreAspectranService.
 */
public class CoreAspectranService extends AbstractAspectranService {

	private static final Log log = LogFactory.getLog(CoreAspectranService.class);
	
	private static final long DEFAULT_PAUSE_TIMEOUT = 321L;
	
	private AspectranServiceControllerListener aspectranServiceControllerListener;
	
	private boolean active;

	@Override
	public synchronized ActivityContext startup() throws AspectranServiceException {
		loadActivityContext();

		log.info("AspectranService was started successfully.");

		if(aspectranServiceControllerListener != null)
			aspectranServiceControllerListener.started();

		this.active = true;
		
		return activityContext;
	}

	@Override
	public synchronized boolean restart() throws AspectranServiceException {
		if(!this.active) {
			log.debug("Cannot restart AspectranService, because it is currently stopped.");
			return true;
		}

		boolean cleanlyDestoryed = dispose();
		
		reloadActivityContext();

		this.active = true;
		
		log.info("AspectranService was restarted.");

		if(aspectranServiceControllerListener != null)
			aspectranServiceControllerListener.restarted();
		
		return cleanlyDestoryed;
	}

	@Override
	public synchronized boolean reload() throws AspectranServiceException {
		if(!this.active) {
			log.debug("Cannot restart AspectranService, because it is currently stopped.");
			return true;
		}
		
		boolean cleanlyDestoryed = stop();
		
		reloadActivityContext();

		this.active = true;
		
		log.info("AspectranService was reloaded.");
		
		if(aspectranServiceControllerListener != null)
			aspectranServiceControllerListener.reloaded();
		
		return cleanlyDestoryed;
	}

	@Override
	public synchronized boolean refresh() throws AspectranServiceException {
		if(isHardReload())
			return restart();
		else
			return reload();
	}

	@Override
	public synchronized void pause() {
		if(aspectranServiceControllerListener != null)
			aspectranServiceControllerListener.paused(-1L);
		
		log.info("AspectranService was paused.");
	}

	@Override
	public synchronized void pause(long timeout) {
		if(aspectranServiceControllerListener != null)
			aspectranServiceControllerListener.paused(timeout);
	}

	@Override
	public synchronized void resume() {
		if(aspectranServiceControllerListener != null)
			aspectranServiceControllerListener.resumed();
		
		log.info("AspectranService was resumed.");
	}

	@Override
	public synchronized boolean stop() {
		if(!this.active)
			return true;

		if(aspectranServiceControllerListener != null)
			aspectranServiceControllerListener.paused(DEFAULT_PAUSE_TIMEOUT);

		boolean cleanlyDestoryed = destroyActivityContext();
			
		log.info("AspectranService was stopped.");

		if(aspectranServiceControllerListener != null)
			aspectranServiceControllerListener.stopped();

		this.active = false;
		
		return cleanlyDestoryed;
	}

	@Override
	public synchronized boolean dispose() {
		ApplicationAdapter applicationAdapter = getApplicationAdapter();
		if(applicationAdapter != null) {
			Scope scope = getApplicationAdapter().getApplicationScope();
			if(scope != null)
				scope.destroy();
		}

		boolean cleanlyDestoryed = stop();

		log.info("AspectranService has been shut down successfully.");

		return cleanlyDestoryed;
	}
	
	public void setAspectranServiceControllerListener(AspectranServiceControllerListener aspectranServiceControllerListener) {
		this.aspectranServiceControllerListener = aspectranServiceControllerListener;
	}
	
	public static AspectranClassLoader newAspectranClassLoader(String[] resourceLocations) throws InvalidResourceException {
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
