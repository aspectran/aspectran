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
import com.aspectran.core.context.loader.ActivityContextLoader;
import com.aspectran.core.context.loader.HybridActivityContextLoader;
import com.aspectran.core.context.loader.config.AspectranConfig;
import com.aspectran.core.context.loader.config.AspectranContextAutoReloadConfig;
import com.aspectran.core.context.loader.config.AspectranContextConfig;
import com.aspectran.core.context.loader.config.AspectranContextProfilesConfig;
import com.aspectran.core.context.loader.config.AspectranSchedulerConfig;
import com.aspectran.core.context.loader.reload.ActivityContextReloadingTimer;
import com.aspectran.core.context.loader.resource.AspectranClassLoader;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.scheduler.service.QuartzSchedulerService;
import com.aspectran.scheduler.service.SchedulerService;

/**
 * The Class AbstractAspectranService.
 */
public abstract class AbstractAspectranService implements AspectranService {

	protected final Log log = LogFactory.getLog(getClass());

	private final ApplicationAdapter applicationAdapter;

	private AspectranConfig aspectranConfig;
	
	private AspectranSchedulerConfig aspectranSchedulerConfig;

	private ActivityContextLoader activityContextLoader;
	
	private String rootContext;

	private boolean hardReload;

	private boolean autoReloadingStartup;
	
	private int scanIntervalSeconds;

	private ActivityContext activityContext;

	private SchedulerService schedulerService;
	
	private ActivityContextReloadingTimer reloadingTimer;

	AbstractAspectranService(ApplicationAdapter applicationAdapter) {
		if (applicationAdapter == null) {
			throw new IllegalArgumentException("The applicationAdapter argument must not be null.");
		}
		this.applicationAdapter = applicationAdapter;
	}

	AbstractAspectranService(AspectranService rootAspectranService) {
		this.applicationAdapter = rootAspectranService.getApplicationAdapter();
		this.activityContext = rootAspectranService.getActivityContext();
		this.aspectranConfig = rootAspectranService.getAspectranConfig();
	}

	@Override
	public ApplicationAdapter getApplicationAdapter() {
		return applicationAdapter;
	}

	@Override
	public ActivityContext getActivityContext() {
		return activityContext;
	}

	@Override
	public AspectranClassLoader getAspectranClassLoader() {
		if (activityContextLoader == null) {
			throw new UnsupportedOperationException("ActivityContextLoader is not initialized. Call initialize() method first.");
		}
		return activityContextLoader.getAspectranClassLoader();
	}

	@Override
	public boolean isHardReload() {
		return hardReload;
	}

	@Override
	public AspectranConfig getAspectranConfig() {
		return aspectranConfig;
	}

	protected synchronized void initialize(AspectranConfig aspectranConfig) throws AspectranServiceException {
		if (activityContext != null) {
			throw new AspectranServiceException("AspectranContext has already been loaded. Must destroy the current AspectranContext before reloading.");
		}

		log.info("Initializing AspectranService...");

		try {
			this.aspectranConfig = aspectranConfig;
			Parameters aspectranContextConfig = aspectranConfig.getParameters(AspectranConfig.context);
			Parameters aspectranContextAutoReloadConfig = aspectranContextConfig.getParameters(AspectranContextConfig.autoReload);
			Parameters aspectranContextProfilesConfig = aspectranContextConfig.getParameters(AspectranContextConfig.profiles);

			if (aspectranContextAutoReloadConfig != null) {
				String reloadMode = aspectranContextAutoReloadConfig.getString(AspectranContextAutoReloadConfig.reloadMode);
				int scanIntervalSeconds = aspectranContextAutoReloadConfig.getInt(AspectranContextAutoReloadConfig.scanIntervalSeconds, -1);
				boolean autoReloadStartup = aspectranContextAutoReloadConfig.getBoolean(AspectranContextAutoReloadConfig.startup, false);
				this.hardReload = "hard".equals(reloadMode);
				this.autoReloadingStartup = autoReloadStartup;
				this.scanIntervalSeconds = scanIntervalSeconds;
			}

			this.rootContext = aspectranContextConfig.getString(AspectranContextConfig.root);
			this.aspectranSchedulerConfig = aspectranConfig.getParameters(AspectranConfig.scheduler);
			
			String encoding = aspectranContextConfig.getString(AspectranContextConfig.encoding);
			boolean hybridLoad = aspectranContextConfig.getBoolean(AspectranContextConfig.hybridLoad, false);
			String[] resourceLocations = aspectranContextConfig.getStringArray(AspectranContextConfig.resources);

			String[] activeProfiles = null;
			String[] defaultProfiles = null;
			
			if (aspectranContextProfilesConfig != null) {
				activeProfiles = aspectranContextProfilesConfig.getStringArray(AspectranContextProfilesConfig.activeProfiles);
				defaultProfiles = aspectranContextProfilesConfig.getStringArray(AspectranContextProfilesConfig.defaultProfiles);
			}
			
			resourceLocations = AspectranClassLoader.checkResourceLocations(resourceLocations, applicationAdapter.getBasePath());

			activityContextLoader = new HybridActivityContextLoader(applicationAdapter, encoding);
			activityContextLoader.setResourceLocations(resourceLocations);
			activityContextLoader.setActiveProfiles(activeProfiles);
			activityContextLoader.setDefaultProfiles(defaultProfiles);
			activityContextLoader.setHybridLoad(hybridLoad);

			if (autoReloadingStartup && (resourceLocations == null || resourceLocations.length == 0)) {
				autoReloadingStartup = false;
			}
			if (autoReloadingStartup) {
				if (scanIntervalSeconds == -1) {
					scanIntervalSeconds = 10;
					String contextAutoReloadingParamName = AspectranConfig.context.getName() + "." + AspectranContextConfig.autoReload.getName();
					log.info("'" + contextAutoReloadingParamName + "' is not specified, defaulting to 10 seconds.");
				}
			}
		} catch (Exception e) {
			throw new AspectranServiceException("Could not initialize AspectranService.", e);
		}
	}
	
	protected synchronized ActivityContext loadActivityContext() throws AspectranServiceException {
		if (activityContextLoader == null) {
			throw new UnsupportedOperationException("ActivityContextLoader is not initialized. Please call initialize() method first.");
		}

		if (activityContext != null) {
			throw new AspectranServiceException("ActivityContext has already been loaded. Must destroy the current ActivityContext before reloading.");
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Loading ActivityContext...");
		}
		
		try {
			activityContext = activityContextLoader.load(rootContext);
			activityContext.initialize(this);

			startupSchedulerService();
			startReloadingTimer();
			
			return activityContext;
		} catch (Exception e) {
			throw new AspectranServiceException("Could not load ActivityContext.", e);
		}
	}

	protected synchronized boolean destroyActivityContext() {
		stopReloadingTimer();
		
		boolean cleanlyDestoryed = true;

		if (!shutdownSchedulerService()) {
			cleanlyDestoryed = false;
		}

		destroyApplicationScope();

		if (activityContext != null) {
			try {
				activityContext.destroy();
				activityContext = null;
				log.info("AspectranContext has been destroyed.");
			} catch (Exception e) {
				log.error("Could not destroy AspectranContext " + activityContext, e);
				cleanlyDestoryed = false;
			}
		}
		
		return cleanlyDestoryed;
	}

	/**
	 * Destroys the application scope.
	 */
	private void destroyApplicationScope() {
		ApplicationAdapter applicationAdapter = getApplicationAdapter();
		if (applicationAdapter != null) {
			Scope scope = applicationAdapter.getApplicationScope();
			if (scope != null) {
				scope.destroy();
			}
		}
	}

	protected synchronized ActivityContext reloadActivityContext() throws AspectranServiceException {
		if (activityContextLoader == null) {
			throw new UnsupportedOperationException("ActivityContextLoader is not initialized. Please call initialize() method first.");
		}

		try {
			activityContext = activityContextLoader.reload(hardReload);
			activityContext.initialize(this);

			startupSchedulerService();
	
			startReloadingTimer();
		} catch (Exception e) {
			throw new AspectranServiceException("Could not reload ActivityContext.", e);
		}

		return activityContext;
	}
	
	private void startupSchedulerService() throws Exception {
		if (this.aspectranSchedulerConfig == null) {
			return;
		}
		
		boolean startup = this.aspectranSchedulerConfig.getBoolean(AspectranSchedulerConfig.startup);
		int startDelaySeconds = this.aspectranSchedulerConfig.getInt(AspectranSchedulerConfig.startDelaySeconds.getName(), -1);
		boolean waitOnShutdown = this.aspectranSchedulerConfig.getBoolean(AspectranSchedulerConfig.waitOnShutdown);

		if (startup) {
			SchedulerService schedulerService = new QuartzSchedulerService(activityContext);
			
			if (waitOnShutdown) {
				schedulerService.setWaitOnShutdown(true);
			}

			if (startDelaySeconds == -1) {
				log.info("Scheduler option 'startDelaySeconds' not specified. So defaulting to 5 seconds.");
				startDelaySeconds = 5;
			}
			
			schedulerService.startup(startDelaySeconds);
			this.schedulerService = schedulerService;

			log.info("SchedulerService has been started.");
		}
	}
	
	private boolean shutdownSchedulerService() {
		if (schedulerService != null) {
			try {
				schedulerService.shutdown();
				schedulerService = null;
				log.info("SchedulerService has been shut down.");
			} catch (Exception e) {
				log.error("SchedulerService did not shutdown cleanly.", e);
				return false;
			}
		}
		
		return true;
	}

	protected boolean pauseSchedulerService() {
		if (schedulerService != null) {
			try {
				schedulerService.pause();
				log.info("SchedulerService has been paused.");
			} catch (Exception e) {
				log.error("SchedulerService pause failed.", e);
				return false;
			}
		}

		return true;
	}

	protected boolean resumeSchedulerService() {
		if (schedulerService != null) {
			try {
				schedulerService.resume();
				log.info("SchedulerService has been resumed.");
			} catch (Exception e) {
				log.error("SchedulerService resume failed.", e);
				return false;
			}
		}

		return true;
	}

	private void startReloadingTimer() {
		if (autoReloadingStartup) {
			AspectranClassLoader aspectranClassLoader = activityContextLoader.getAspectranClassLoader();
			if (aspectranClassLoader != null) {
				reloadingTimer = new ActivityContextReloadingTimer(this, aspectranClassLoader.extractResources());
				reloadingTimer.start(scanIntervalSeconds);
			}
		}
	}
	
	private void stopReloadingTimer() {
		if (reloadingTimer != null) {
			reloadingTimer.cancel();
		}
		reloadingTimer = null;
	}

}
