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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.ActivityContextException;
import com.aspectran.core.context.loader.ActivityContextLoader;
import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.context.loader.HybridActivityContextLoader;
import com.aspectran.core.context.loader.config.AspectranConfig;
import com.aspectran.core.context.loader.config.AspectranContextAutoReloadingConfig;
import com.aspectran.core.context.loader.config.AspectranContextConfig;
import com.aspectran.core.context.loader.config.AspectranSchedulerConfig;
import com.aspectran.core.context.loader.reload.ActivityContextReloadingTimer;
import com.aspectran.core.context.loader.resource.InvalidResourceException;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.scheduler.service.SchedulerService;
import com.aspectran.scheduler.service.QuartzSchedulerService;

public abstract class AbstractAspectranService implements AspectranService {

	protected static final Log log = LogFactory.getLog(AbstractAspectranService.class);

	private Parameters aspectranConfig;
	
	private Parameters aspectranSchedulerConfig;
	
	private AspectranClassLoader aspectranClassLoader;
	
	private ApplicationAdapter applicationAdapter;
	
	private ActivityContextLoader activityContextLoader;
	
	private String rootContext;

	private String[] resourceLocations;
	
	private boolean hardReload;

	private boolean autoReloadingStartup;
	
	private int observationInterval;
	
	protected ActivityContext activityContext;

	private SchedulerService schedulerService;
	
	private ActivityContextReloadingTimer reloadingTimer;
	
	public Parameters getAspectranConfig() {
		return aspectranConfig;
	}

	public String getApplicationBasePath() {
		if(applicationAdapter == null)
			return null;

		return applicationAdapter.getApplicationBasePath();
	}

	public String getRootContext() {
		return rootContext;
	}

	public AspectranClassLoader getAspectranClassLoader() {
		return aspectranClassLoader;
	}

	protected void setAspectranClassLoader(AspectranClassLoader aspectranClassLoader) {
		this.aspectranClassLoader = aspectranClassLoader;
	}

	public ApplicationAdapter getApplicationAdapter() {
		return applicationAdapter;
	}
	
	protected void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
	}

	public ActivityContext getActivityContext() {
		return activityContext;
	}
	
	public boolean isHardReload() {
		return hardReload;
	}

	protected synchronized void initialize(AspectranConfig aspectranConfig) throws ActivityContextException {
		if(activityContext != null)
			throw new ActivityContextException("Already loaded the AspectranContext. Destroy the old AspectranContext before loading.");
		
		log.info("Initializing AspectranService...");
		

		try {
			this.aspectranConfig = aspectranConfig;
			Parameters aspectranContextConfig = aspectranConfig.getParameters(AspectranConfig.context);
			Parameters aspectranContextAutoReloadingConfig = aspectranContextConfig.getParameters(AspectranContextConfig.autoReloading);

			if(aspectranContextAutoReloadingConfig != null) {
				String reloadMethod = aspectranContextAutoReloadingConfig.getString(AspectranContextAutoReloadingConfig.reloadMethod);
				int observationInterval = aspectranContextAutoReloadingConfig.getInt(AspectranContextAutoReloadingConfig.observationInterval, -1);
				boolean autoReloadingStartup = aspectranContextAutoReloadingConfig.getBoolean(AspectranContextAutoReloadingConfig.startup, true);
				this.hardReload = "hard".equals(reloadMethod);
				this.autoReloadingStartup = autoReloadingStartup;
				this.observationInterval = observationInterval;
			}

			if(autoReloadingStartup && (resourceLocations == null || resourceLocations.length == 0))
				autoReloadingStartup = false;

			if(autoReloadingStartup) {
				if(observationInterval == -1) {
					observationInterval = 10;
					log.info("'" + aspectranContextAutoReloadingConfig.getQualifiedName() + "' is not specified, defaulting to 10 seconds.");
				}
			}

			this.rootContext = aspectranContextConfig.getString(AspectranContextConfig.root);
			String[] resourceLocations = aspectranContextConfig.getStringArray(AspectranContextConfig.resources);
			this.resourceLocations = checkResourceLocations(getApplicationBasePath(), null, resourceLocations);
			this.aspectranSchedulerConfig = aspectranConfig.getParameters(AspectranConfig.scheduler);
			
			aspectranClassLoader = CoreAspectranService.newAspectranClassLoader(this.resourceLocations);
			
			String encoding = aspectranContextConfig.getString(AspectranContextConfig.encoding);
			boolean hybridLoading = aspectranContextConfig.getBoolean(AspectranContextConfig.hybridLoading, false);
			
			activityContextLoader = new HybridActivityContextLoader(encoding);
			activityContextLoader.setApplicationAdapter(applicationAdapter);
			activityContextLoader.setHybridLoading(hybridLoading);
		} catch(Exception e) {
			throw new AspectranServiceException("Failed to initialize AspectranService " + aspectranConfig, e);
		}
	}
	
	protected synchronized ActivityContext loadActivityContext() throws ActivityContextException {
		if(activityContext != null)
			throw new ActivityContextException("Already loaded the AspectranContext. Destroy the old AspectranContext before loading.");
		
		log.info("Loading ActivityContext...");
		
		try {
			activityContext = activityContextLoader.load(rootContext);
			
			startupSchedulerService();
			startReloadingTimer();
			
			return activityContext;
			
		} catch(Exception e) {
			throw new AspectranServiceException("Failed to load the ActivityContext.", e);
		}
	}
	
	protected synchronized boolean destroyActivityContext() {
		stopReloadingTimer();
		
		boolean cleanlyDestoryed = true;

		if(!shutdownSchedulerService())
			cleanlyDestoryed = false;

		if(activityContext != null) {
			try {
				activityContext.destroy();
				activityContext = null;
				log.info("Successfully destroyed AspectranContext.");
			} catch(Exception e) {
				log.error("Failed to destroy AspectranContext " + activityContext, e);
				cleanlyDestoryed = false;
			}
		}
		
		return cleanlyDestoryed;
	}

	public synchronized ActivityContext reloadActivityContext() {
		try {
			if(activityContextLoader == null)
				throw new IllegalArgumentException("activityContextLoader must not be null");

			if(hardReload) {
				aspectranClassLoader = CoreAspectranService.newAspectranClassLoader(this.resourceLocations);
				//activityContextLoader.setAspectranClassLoader(aspectranClassLoader);
			} else {
				aspectranClassLoader.reload();
			}
			
			activityContext = activityContextLoader.load(rootContext);
			
			startupSchedulerService();
	
			startReloadingTimer();
		} catch(Exception e) {
			throw new AspectranServiceException("Failed to reload the Aspectran's ActivityContext.", e);
		}

		return activityContext;
	}
	
	private void startupSchedulerService() throws Exception {
		if(this.aspectranSchedulerConfig == null)
			return;
		
		log.info("Starting the SchedulerService " + this.aspectranSchedulerConfig.describe());
		
		boolean startup = this.aspectranSchedulerConfig.getBoolean(AspectranSchedulerConfig.startup);
		int startDelaySeconds = this.aspectranSchedulerConfig.getInt(AspectranSchedulerConfig.startDelaySeconds.getName(), -1);
		boolean waitOnShutdown = this.aspectranSchedulerConfig.getBoolean(AspectranSchedulerConfig.waitOnShutdown);
		
		if(startup) {
			SchedulerService schedulerService = new QuartzSchedulerService(activityContext);
			
			if(waitOnShutdown)
				schedulerService.setWaitOnShutdown(true);
			
			if(startDelaySeconds == -1) {
				log.info("Scheduler option 'startDelaySeconds' is not specified. So defaulting to 5 seconds.");
				startDelaySeconds = 5;
			}
			
			schedulerService.startup(startDelaySeconds);
			this.schedulerService = schedulerService;
		}
	}
	
	private boolean shutdownSchedulerService() {
		if(schedulerService != null) {
			try {
				schedulerService.shutdown();
				schedulerService = null;
				log.info("Aspectran's SchedulerService has not been shutdown successfully.");
			} catch(Exception e) {
				log.error("Aspectran's SchedulerService has not been shutdown cleanly.", e);
				return false;
			}
		}
		
		return true;
	}
	
	private void startReloadingTimer() {
		if(autoReloadingStartup) {
			reloadingTimer = new ActivityContextReloadingTimer(this);
			reloadingTimer.start(observationInterval);
		}
	}
	
	private void stopReloadingTimer() {
		if(reloadingTimer != null)
			reloadingTimer.cancel();
		
		reloadingTimer = null;
	}
	

	private static String[] checkResourceLocations(String applicationBasePath, String rootResourceLocation, String[] resourceLocations) throws IOException {
		if(resourceLocations == null)
			return null;
		
		for(int i = 0; i < resourceLocations.length; i++) {
			if(resourceLocations[i].startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
				String path = resourceLocations[i].substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
				URL url = AspectranClassLoader.getDefaultClassLoader().getResource(path);
				if(url == null)
					throw new InvalidResourceException("class path resource [" + resourceLocations[i] + "] cannot be resolved to URL because it does not exist");
				resourceLocations[i] = url.getFile();
			} else if(resourceLocations[i].startsWith(ResourceUtils.FILE_URL_PREFIX)) {
				try {
					URL url = new URL(resourceLocations[i]);
					resourceLocations[i] = url.getFile();
				} catch (MalformedURLException e) {
					throw new InvalidResourceException("Resource location [" + resourceLocations[i] + "] is neither a URL not a well-formed file path");
				}
			} else {
				resourceLocations[i] = applicationBasePath + resourceLocations[i];
			}
			
			if(resourceLocations[i].indexOf('\\') != -1)
				resourceLocations[i] = resourceLocations[i].replace('\\', '/');
			
			if(resourceLocations[i].endsWith(ResourceUtils.RESOURCE_NAME_SPEPARATOR))
				resourceLocations[i] = resourceLocations[i].substring(0, resourceLocations[i].length() - ResourceUtils.RESOURCE_NAME_SPEPARATOR.length());
		}
		
		String resourceLocation = null;
		
		try {
			if(rootResourceLocation != null) {
				resourceLocation = rootResourceLocation;
				
				File f1 = new File(rootResourceLocation);
				String l1 = f1.getCanonicalPath();
				
				for(int i = 0; i < resourceLocations.length - 1; i++) {
					File f2 = new File(resourceLocations[i]);
					String l2 = f2.getCanonicalPath();
					
					if(l1.equals(l2)) {
						resourceLocations[i] = null;
					}
				}
			}
			
			for(int i = 0; i < resourceLocations.length - 1; i++) {
				if(resourceLocations[i] != null) {
					resourceLocation = resourceLocations[i];
					File f1 = new File(resourceLocations[i]);
					String l1 = f1.getCanonicalPath();

					for(int j = i + 1; j < resourceLocations.length; j++) {
						if(resourceLocations[j] != null) {
							resourceLocation = resourceLocations[j];
							File f2 = new File(resourceLocations[j]);
							String l2 = f2.getCanonicalPath();
	
							if(l1.equals(l2)) {
								resourceLocations[j] = null;
							}
						}
					}
				}
			}
		} catch(IOException e) {
			throw new InvalidResourceException("invalid resource location: " + resourceLocation, e);
		}
		
		return resourceLocations;
	}
	
}
