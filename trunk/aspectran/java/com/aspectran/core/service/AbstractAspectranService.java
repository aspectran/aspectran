package com.aspectran.core.service;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.ActivityContextException;
import com.aspectran.core.context.loader.ActivityContextLoader;
import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.context.loader.config.AspectranConfig;
import com.aspectran.core.context.loader.config.AspectranContextAutoReloadingConfig;
import com.aspectran.core.context.loader.config.AspectranContextConfig;
import com.aspectran.core.context.loader.config.AspectranSchedulerConfig;
import com.aspectran.core.context.loader.reload.ActivityContextReloadingTimer;
import com.aspectran.core.context.loader.resource.InvalidResourceException;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.var.apon.Parameters;
import com.aspectran.scheduler.AspectranScheduler;
import com.aspectran.scheduler.quartz.QuartzAspectranScheduler;

public abstract class AbstractAspectranService implements AspectranService {

	private final Logger logger = LoggerFactory.getLogger(AbstractAspectranService.class);

	private final Parameters aspectranConfig;
	
	private Parameters aspectranSchedulerConfig;
	
	private ActivityContextLoader activityContextLoader;

	private ApplicationAdapter applicationAdapter;
	
	private AspectranClassLoader aspectranClassLoader;
	
	private String rootContext;

	private String[] resourceLocations;
	
	private boolean isHardReload;

	private boolean autoReloadingStartup;
	
	private int observationInterval;
	
	protected ActivityContext activityContext;

	private AspectranScheduler aspectranScheduler;
	
	private ActivityContextReloadingTimer reloadingTimer;
	
	protected AbstractAspectranService(AspectranConfig aspectranConfig) {
		this.aspectranConfig = aspectranConfig;
	}
	
	public Parameters getAspectranConfig() {
		return aspectranConfig;
	}

	public ActivityContextLoader getActivityContextLoader() {
		return activityContextLoader;
	}
	
	public void setActivityContextLoader(ActivityContextLoader activityContextLoader) {
		this.activityContextLoader = activityContextLoader;
		this.applicationAdapter = activityContextLoader.getApplicationAdapter();
		this.aspectranClassLoader = activityContextLoader.getAspectranClassLoader();
	}

	protected synchronized void initActivityContext() throws ActivityContextException {
		if(activityContext != null)
			throw new ActivityContextException("Already loaded the AspectranContext. Destroy the old AspectranContext before loading.");
		
		logger.info("init ActivityContext...");

		try {
			Parameters aspectranContextConfig = aspectranConfig.getParameters(AspectranConfig.context);
			Parameters aspectranContextAutoReloadingConfig = aspectranContextConfig.getParameters(AspectranContextConfig.autoReloading);
			Parameters aspectranSchedulerConfig = aspectranConfig.getParameters(AspectranConfig.scheduler);
			
			String rootContext = aspectranContextConfig.getString(AspectranContextConfig.root);
			String[] resourceLocations = aspectranContextConfig.getStringArray(AspectranContextConfig.resources);
			String reloadMethod = aspectranContextAutoReloadingConfig.getString(AspectranContextAutoReloadingConfig.reloadMethod);
			int observationInterval = aspectranContextAutoReloadingConfig.getInt(AspectranContextAutoReloadingConfig.observationInterval, -1);
			boolean autoReloadingStartup = aspectranContextAutoReloadingConfig.getBoolean(AspectranContextAutoReloadingConfig.startup, true);

			if(autoReloadingStartup && resourceLocations == null || resourceLocations.length == 0)
				autoReloadingStartup = false;
			
			this.rootContext = rootContext;
			this.resourceLocations = checkResourceLocations(getApplicationBasePath(), resourceLocations);
			this.isHardReload = "hard".equals(reloadMethod);
			this.autoReloadingStartup = autoReloadingStartup;
			this.observationInterval = observationInterval;
			this.aspectranSchedulerConfig = aspectranSchedulerConfig;

			if(autoReloadingStartup) {
				if(observationInterval == -1) {
					observationInterval = 10;
					this.observationInterval = observationInterval;
					logger.info("[Aspectran Config] 'observationInterval' is not specified, defaulting to 10 seconds.");
				}
			}
		} catch(Exception e) {
			throw new AspectranServiceException("Aspectran's ActivityContext Service failed to initialize.", e);
		}
	}
	
	protected synchronized ActivityContext loadActivityContext() throws ActivityContextException {
		if(activityContext != null)
			throw new ActivityContextException("Already loaded the AspectranContext. Destroy the old AspectranContext before loading.");
		
		logger.info("loading ActivityContext...");
		
		try {
			if(activityContextLoader == null)
				throw new IllegalArgumentException("activityContextLoader must not be null");
			
			if(activityContextLoader.getAspectranClassLoader() == null) {
				AspectranClassLoader aspectranClassLoader = new AspectranClassLoader(resourceLocations);
				activityContextLoader.setAspectranClassLoader(aspectranClassLoader);
			}

			activityContext = activityContextLoader.load(rootContext);
			
			startupAspectranScheduler();
			
			startReloadingTimer();
			
			return activityContext;
			
		} catch(Exception e) {
			throw new AspectranServiceException("Failed to load the Aspectran's ActivityContext Service.", e);
		}
	}
	
	protected synchronized boolean destroyActivityContext() {
		stopReloadingTimer();
		
		boolean cleanlyDestoryed = true;

		if(!shutdownAspectranScheduler())
			cleanlyDestoryed = false;

		if(activityContext != null) {
			try {
				activityContext.destroy();
				activityContext = null;
				logger.info("Successfully destroyed AspectranContext.");
			} catch(Exception e) {
				logger.error("Failed to destroy AspectranContext " + activityContext, e);
				cleanlyDestoryed = false;
			}
		}
		
		return cleanlyDestoryed;
	}

	public synchronized ActivityContext reloadActivityContext() {
		try {
			if(activityContextLoader == null)
				throw new IllegalArgumentException("activityContextLoader must not be null");

			if(activityContextLoader.getAspectranClassLoader() != null) {
				if(isHardReload) {
					AspectranClassLoader aspectranClassLoader = new AspectranClassLoader(resourceLocations);
					activityContextLoader.setAspectranClassLoader(aspectranClassLoader);
				} else {
					activityContextLoader.getAspectranClassLoader().reload();
				}
			}
			
			activityContext = activityContextLoader.load(rootContext);
			
			startupAspectranScheduler();
	
			startReloadingTimer();
		} catch(Exception e) {
			throw new AspectranServiceException("Failed to reload the Aspectran's ActivityContext Service.", e);
		}

		return activityContext;
	}
	
	private void startupAspectranScheduler() throws Exception {
		if(this.aspectranSchedulerConfig == null)
			return;
		
		logger.info("starting the AspectranScheduler: " + this.aspectranSchedulerConfig);
		
		boolean startup = this.aspectranSchedulerConfig.getBoolean(AspectranSchedulerConfig.startup);
		int startDelaySeconds = this.aspectranSchedulerConfig.getInt(AspectranSchedulerConfig.startDelaySeconds.getName(), -1);
		boolean waitOnShutdown = this.aspectranSchedulerConfig.getBoolean(AspectranSchedulerConfig.waitOnShutdown);
		
		if(startup) {
			AspectranScheduler aspectranScheduler = new QuartzAspectranScheduler(activityContext);
			
			if(waitOnShutdown)
				aspectranScheduler.setWaitOnShutdown(true);
			
			if(startDelaySeconds == -1) {
				logger.info("Scheduler option 'startDelaySeconds' is not specified. So defaulting to 5 seconds.");
				startDelaySeconds = 5;
			}
			
			aspectranScheduler.startup(startDelaySeconds);
			this.aspectranScheduler = aspectranScheduler;
		}
	}
	
	private boolean shutdownAspectranScheduler() {
		if(aspectranScheduler != null) {
			try {
				aspectranScheduler.shutdown();
				aspectranScheduler = null;
				logger.info("Successfully destroyed AspectranScheduler " + aspectranScheduler);
			} catch(Exception e) {
				logger.error("AspectranScheduler were not destroyed cleanly.", e);
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
	
	public ApplicationAdapter getApplicationAdapter() {
		return applicationAdapter;
	}
	
	public String getApplicationBasePath() {
		if(applicationAdapter == null)
			return null;

		return applicationAdapter.getApplicationBasePath();
	}
	
	public AspectranClassLoader getAspectranClassLoader() {
		return aspectranClassLoader;
	}
	
	public ActivityContext getActivityContext() {
		return activityContext;
	}
	
	private static String[] checkResourceLocations(String applicationBasePath, String[] resourceLocations) throws FileNotFoundException {
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
		}
		
		return resourceLocations;
	}
	
	public static void main(String argv[]) {
		String applicationBasePath = "c:/Users/Gulendol/Projects/aspectran/ADE/workspace/aspectran.example/webapp";
		String[] resourceLocations = new String[3];
		resourceLocations[0] = "file:/c:/Users/Gulendol/Projects/aspectran/ADE/workspace/aspectran.example/webapp/WEB-INF/aspectran/classes";
		resourceLocations[1] = "/WEB-INF/aspectran/lib";
		resourceLocations[2] = "/WEB-INF/aspectran/xml";
		
		try {
			resourceLocations = AbstractAspectranService.checkResourceLocations(applicationBasePath, resourceLocations);
			for(String r : resourceLocations) {
				System.out.println(r);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
