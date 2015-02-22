package com.aspectran.core.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.aspectran.scheduler.AspectranScheduler;
import com.aspectran.scheduler.quartz.QuartzAspectranScheduler;

public abstract class AbstractAspectranService implements AspectranService {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractAspectranService.class);

	private Parameters aspectranConfig;
	
	private Parameters aspectranSchedulerConfig;
	
	protected AspectranClassLoader aspectranClassLoader;
	
	protected ApplicationAdapter applicationAdapter;
	
	private ActivityContextLoader activityContextLoader;
	
	private String rootContext;

	private String encoding;
	
	private String[] resourceLocations;
	
	private boolean hybridLoading;

	private boolean hardReload;

	private boolean autoReloadingStartup;
	
	private int observationInterval;
	
	protected ActivityContext activityContext;

	private AspectranScheduler aspectranScheduler;
	
	private ActivityContextReloadingTimer reloadingTimer;
	
	protected AbstractAspectranService() {
	}
	
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

	public ActivityContextLoader getActivityContextLoader() {
		return activityContextLoader;
	}
	
	public void setActivityContextLoader(ActivityContextLoader activityContextLoader) {
		this.activityContextLoader = activityContextLoader;
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
		
		logger.info("Initializing AspectranService...");
		
		this.aspectranConfig = aspectranConfig;

		try {
			if(aspectranClassLoader == null)
				aspectranClassLoader = new AspectranClassLoader();
			
			Parameters aspectranContextConfig = aspectranConfig.getParameters(AspectranConfig.context);
			Parameters aspectranContextAutoReloadingConfig = aspectranContextConfig.getParameters(AspectranContextConfig.autoReloading);
			Parameters aspectranSchedulerConfig = aspectranConfig.getParameters(AspectranConfig.scheduler);
			
			String rootContext = aspectranContextConfig.getString(AspectranContextConfig.root);
			String encoding = aspectranContextConfig.getString(AspectranContextConfig.encoding);
			String[] resourceLocations = aspectranContextConfig.getStringArray(AspectranContextConfig.resources);
			boolean hybridLoading = aspectranContextConfig.getBoolean(AspectranContextConfig.hybridLoading, false);
			String reloadMethod = aspectranContextAutoReloadingConfig.getString(AspectranContextAutoReloadingConfig.reloadMethod);
			int observationInterval = aspectranContextAutoReloadingConfig.getInt(AspectranContextAutoReloadingConfig.observationInterval, -1);
			boolean autoReloadingStartup = aspectranContextAutoReloadingConfig.getBoolean(AspectranContextAutoReloadingConfig.startup, true);

			if(autoReloadingStartup && resourceLocations == null || resourceLocations.length == 0)
				autoReloadingStartup = false;
			
			this.rootContext = rootContext;
			this.encoding = encoding;
			this.resourceLocations = checkResourceLocations(getApplicationBasePath(), aspectranClassLoader.getResourceLocation(), resourceLocations);
			this.hybridLoading = hybridLoading;
			this.hardReload = "hard".equals(reloadMethod);
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
			
			if(resourceLocations != null && resourceLocations.length > 0)
				aspectranClassLoader.setResourceLocations(resourceLocations);
		} catch(Exception e) {
			throw new AspectranServiceException("Failed to initialize AspectranService " + aspectranConfig, e);
		}
	}
	
	protected synchronized ActivityContext loadActivityContext() throws ActivityContextException {
		if(activityContext != null)
			throw new ActivityContextException("Already loaded the AspectranContext. Destroy the old AspectranContext before loading.");
		
		logger.info("Loading ActivityContext...");
		
		try {
			if(activityContextLoader == null) {
				activityContextLoader = new HybridActivityContextLoader(encoding);
				activityContextLoader.setAspectranClassLoader(aspectranClassLoader);
				activityContextLoader.setApplicationAdapter(applicationAdapter);
				activityContextLoader.setHybridLoading(hybridLoading);
			}
			
			activityContextLoader.setAspectranClassLoader(aspectranClassLoader);
			activityContext = activityContextLoader.load(rootContext);
			
			startupAspectranScheduler();
			startReloadingTimer();
			
			return activityContext;
			
		} catch(Exception e) {
			throw new AspectranServiceException("Failed to load the ActivityContext " + aspectranConfig, e);
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
				if(hardReload) {
					aspectranClassLoader = new AspectranClassLoader(aspectranClassLoader.getResourceLocation());

					if(resourceLocations != null && resourceLocations.length > 0)
						aspectranClassLoader.setResourceLocations(resourceLocations);
					
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
		
		logger.info("Starting the AspectranScheduler: " + this.aspectranSchedulerConfig);
		
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
						logger.info("");
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
	
	public static void main(String argv[]) {
		String applicationBasePath = "c:/Users/Gulendol/Projects/aspectran/ADE/workspace/aspectran.example/webapp";
		String[] resourceLocations = new String[3];
		resourceLocations[0] = "file:/c:/Users/Gulendol/Projects/aspectran/ADE/workspace/aspectran.example/webapp/WEB-INF/aspectran/classes";
		resourceLocations[1] = "/WEB-INF/aspectran/lib";
		resourceLocations[2] = "/WEB-INF/aspectran/xml";
		String rootResourceLocation = "/WEB-INF/classes";
		
		try {
			resourceLocations = AbstractAspectranService.checkResourceLocations(applicationBasePath, rootResourceLocation, resourceLocations);
			for(String r : resourceLocations) {
				System.out.println(r);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
