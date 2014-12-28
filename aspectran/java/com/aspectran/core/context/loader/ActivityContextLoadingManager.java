package com.aspectran.core.context.loader;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.ActivityContextException;
import com.aspectran.core.context.loader.config.AspectranConfig;
import com.aspectran.core.context.loader.config.AspectranContextAutoReloadingConfig;
import com.aspectran.core.context.loader.config.AspectranContextConfig;
import com.aspectran.core.context.loader.config.AspectranSchedulerConfig;
import com.aspectran.core.context.loader.reload.ActivityContextReloadable;
import com.aspectran.core.context.loader.reload.ActivityContextReloadingTimer;
import com.aspectran.core.context.loader.resource.InvalidResourceException;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.var.apon.Parameters;
import com.aspectran.scheduler.AspectranScheduler;
import com.aspectran.scheduler.quartz.QuartzAspectranScheduler;

public class ActivityContextLoadingManager implements ActivityContextReloadable {

	private final Logger logger = LoggerFactory.getLogger(ActivityContextLoadingManager.class);

	private final Parameters aspectranConfig;
	
	private Parameters aspectranSchedulerConfig;
	
	private final ActivityContextLoader activityContextLoader;

	private String rootContext;

	private String[] resourceLocations;
	
	private boolean isHardReload;

	private boolean autoReloadingStartup;
	
	private int observationInterval;
	
	protected ActivityContext activityContext;

	private AspectranScheduler aspectranScheduler;
	
	private ActivityContextReloadingTimer reloadingTimer;
	
	protected ActivityContextLoadingManager(AspectranConfig aspectranConfig, ActivityContextLoader activityContextLoader) {
		this.aspectranConfig = aspectranConfig;
		this.activityContextLoader = activityContextLoader;
	}
	
	protected synchronized ActivityContext createActivityContext() throws ActivityContextException {
		if(activityContext != null)
			throw new ActivityContextException("Already loaded the AspectranContext. Destroy the old AspectranContext before loading.");
		
		logger.info("loading ActivityContext...");

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
			
			AspectranClassLoader spectranClassLoader = new AspectranClassLoader(resourceLocations);
			activityContextLoader.setAspectranClassLoader(spectranClassLoader);
			
			activityContext = activityContextLoader.load(rootContext);

			startupAspectranScheduler();
			
			if(autoReloadingStartup) {
				if(observationInterval == -1) {
					observationInterval = 10;
					this.observationInterval = observationInterval;
					logger.info("[Aspectran Config] 'observationInterval' is not specified, defaulting to 10 seconds.");
				}
				
				startReloadingTimer();
			}

			return activityContext;
			
		} catch(Exception e) {
			throw new ActivityContextLoadingFailedException("Failed to load the ActivityContext", e);
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
				logger.info("AspectranContext was destroyed successfully.");
			} catch(Exception e) {
				logger.error("AspectranContext was failed to destroy: " + e.toString(), e);
				cleanlyDestoryed = false;
			}
		}
		
		return cleanlyDestoryed;
	}

	public synchronized ActivityContext reloadActivityContext() {
		destroyActivityContext();
		
		if(activityContextLoader.getAspectranClassLoader() != null) {
			if(isHardReload) {
				AspectranClassLoader aspectranClassLoader = new AspectranClassLoader(resourceLocations);
				activityContextLoader.setAspectranClassLoader(aspectranClassLoader);
			} else {
				activityContextLoader.getAspectranClassLoader().reload();
			}
		}
		
		activityContext = activityContextLoader.load(rootContext);
		
		try {
			startupAspectranScheduler();
		} catch(Exception e) {
			logger.error("AspectranScheduler was failed to initialize: " + e.toString(), e);
		}

		startReloadingTimer();

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
				logger.info("AspectranScheduler has been shutdown successfully.");
			} catch(Exception e) {
				logger.error("AspectranScheduler was failed to shutdown cleanly: " + e.toString(), e);
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
		return activityContextLoader.getApplicationAdapter();
	}
	
	public String getApplicationBasePath() {
		return activityContextLoader.getApplicationAdapter().getApplicationBasePath();
	}
	
	public AspectranClassLoader getAspectranClassLoader() {
		return activityContextLoader.getAspectranClassLoader();
	}
	
	public ActivityContext getActivityContext() {
		return activityContext;
	}
	
	protected static String[] checkResourceLocations(String applicationBasePath, String[] resourceLocations) throws FileNotFoundException {
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
			resourceLocations = ActivityContextLoadingManager.checkResourceLocations(applicationBasePath, resourceLocations);
			for(String r : resourceLocations) {
				System.out.println(r);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
