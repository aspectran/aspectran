package com.aspectran.web.startup.loader;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.ActivityContextException;
import com.aspectran.core.context.AspectranClassLoader;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.AspectranContextAutoReloadingConfig;
import com.aspectran.core.context.config.AspectranContextConfig;
import com.aspectran.core.context.config.AspectranSchedulerConfig;
import com.aspectran.core.context.loader.ActivityContextLoader;
import com.aspectran.core.context.loader.reload.ActivityContextReloadingHandler;
import com.aspectran.core.context.loader.reload.ActivityContextReloadingTimer;
import com.aspectran.core.var.apon.Parameters;
import com.aspectran.scheduler.AspectranScheduler;
import com.aspectran.scheduler.quartz.QuartzAspectranScheduler;
import com.aspectran.web.adapter.WebApplicationAdapter;
import com.aspectran.web.startup.servlet.WebActivityServlet;

public class WebActivityContextLoadingManager {

	private final Logger logger = LoggerFactory.getLogger(WebActivityServlet.class);

	protected ActivityContext activityContext;

	private AspectranScheduler aspectranScheduler;
	
	private ActivityContextReloadingTimer contextReloadingTimer;
	
	private Parameters aspectranSchedulerConfig;
	
	public void init() throws ServletException {
		logger.info("initializing WebActivityServlet...");

		try {
			ServletContext servletContext = getServletContext();
			
			String aspectranConfigText = getServletConfig().getInitParameter(WebActivityContextLoader.ASPECTRAN_CONFIG_PARAM);
			
			Parameters aspectranConfig = new AspectranConfig(aspectranConfigText);
			Parameters aspectranContextConfig = aspectranConfig.getParameters(AspectranConfig.context.getName());
			Parameters aspectranContextAutoReloadingConfig = aspectranContextConfig.getParameters(AspectranContextConfig.autoReloading.getName());
			Parameters aspectranSchedulerConfig = aspectranConfig.getParameters(AspectranConfig.scheduler.getName());
			
			String rootContext = aspectranContextConfig.getString(AspectranContextConfig.root.getName());
			String[] resourceLocations = aspectranContextConfig.getStringArray(AspectranContextConfig.resources.getName());
			int observationInterval = aspectranContextAutoReloadingConfig.getInt(AspectranContextAutoReloadingConfig.observationInterval.getName(), -1);
			boolean autoReloadingStartup = aspectranContextAutoReloadingConfig.getBoolean(AspectranContextAutoReloadingConfig.startup.getName(), true);

			if(autoReloadingStartup && resourceLocations == null || resourceLocations.length == 0)
				autoReloadingStartup = false;
			
			AspectranClassLoader aspectranClassLoader = new AspectranWebClassLoader();
			aspectranClassLoader.setResourceLocations(resourceLocations);
			
			ApplicationAdapter applicationAdapter = WebApplicationAdapter.determineWebApplicationAdapter(servletContext);
			
			if(!autoReloadingStartup) {
				WebActivityContextLoader loader = new WebActivityContextLoader(applicationAdapter, aspectranClassLoader);
				activityContext = loader.load(rootContext, resourceLocations);
			} else {
				if(observationInterval == -1) {
					logger.info("[Aspectran Config] 'observationInterval' is not specified, defaulting to 10 seconds.");
					observationInterval = 10;
				}

				ActivityContextReloadingHandler contextReloadingHandler = new ActivityContextReloadingHandler() {
					public void handle(ActivityContext newActivityContext) {
						reload(newActivityContext);
					}
				};
				
				WebActivityContextLoadingObserver observer = new WebActivityContextLoadingObserver(applicationAdapter, aspectranClassLoader);
				activityContext = observer.load(rootContext, resourceLocations);
				contextReloadingTimer = observer.startTimer(contextReloadingHandler, observationInterval);
			}
			
			if(activityContext == null)
				new ActivityContextException("ActivityContext is not loaded.");
			
			initAspectranScheduler(aspectranSchedulerConfig);
			
		} catch(Exception e) {
			logger.error("WebActivityServlet was failed to initialize: " + e.toString(), e);
			throw new UnavailableException(e.getMessage());
		}
	}
	
	protected void initAspectranScheduler(Parameters aspectranSchedulerConfig) throws Exception {
		if(aspectranSchedulerConfig != null)
			this.aspectranSchedulerConfig = aspectranSchedulerConfig;
		
		if(this.aspectranSchedulerConfig == null)
			return;
		
		boolean startup = this.aspectranSchedulerConfig.getBoolean(AspectranSchedulerConfig.startup.getName());
		int startDelaySeconds = this.aspectranSchedulerConfig.getInt(AspectranSchedulerConfig.startDelaySeconds.getName(), -1);
		boolean waitOnShutdown = this.aspectranSchedulerConfig.getBoolean(AspectranSchedulerConfig.waitOnShutdown.getName());
		
		if(startup) {
			aspectranScheduler = new QuartzAspectranScheduler(activityContext);
			
			if(waitOnShutdown)
				aspectranScheduler.setWaitOnShutdown(true);
			
			if(startDelaySeconds == -1) {
				logger.info("Scheduler option 'startDelaySeconds' is not specified. So defaulting to 5 seconds.");
				startDelaySeconds = 5;
			}
			
			aspectranScheduler.startup(startDelaySeconds);
		}
	}
	
	
	protected boolean shutdownScheduler() {
		if(aspectranScheduler != null) {
			try {
				aspectranScheduler.shutdown();
				logger.info("AspectranScheduler has been shutdown successfully.");
			} catch(Exception e) {
				logger.error("AspectranScheduler was failed to shutdown cleanly: " + e.toString(), e);
				return false;
			}
		}
		
		return true;
	}
	
	protected boolean destroyContext() {
		if(activityContext != null) {
			try {
				activityContext.destroy();
				logger.info("AspectranContext was destroyed successfully.");
			} catch(Exception e) {
				logger.error("AspectranContext was failed to destroy: " + e.toString(), e);
				return false;
			}
		}
		
		return true;
	}
	
	protected void reload(ActivityContext newActivityContext) {
		if(contextReloadingTimer != null)
			contextReloadingTimer.cancel();
		
		shutdownScheduler();
		destroyContext();
		
		activityContext = newActivityContext;
		
		try {
			if(this.aspectranSchedulerConfig != null)
				initAspectranScheduler(null);
		} catch(Exception e) {
			logger.error("Scheduler was failed to initialize: " + e.toString(), e);
		}
		
		if(contextReloadingTimer != null)
			contextReloadingTimer.start();
	}
	
}
