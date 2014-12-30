package com.aspectran.core.context.loader.reload;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.loader.AspectranClassLoader;

public class ActivityContextReloadingTimerTask extends TimerTask {
	
	private final Logger logger = LoggerFactory.getLogger(ActivityContextReloadingTimerTask.class);
	
	private final boolean debugEnabled = logger.isDebugEnabled();

	private final ActivityContextReloadDelegate activityContextReloadDelegate;
	
	private final AspectranClassLoader aspectranClassLoader;
	
	private final URL[] resources;
	
	private Map<String, Long> modifyTimeMap = new HashMap<String, Long>();
	
	private boolean modified = false;
	
	public ActivityContextReloadingTimerTask(ActivityContextReloadDelegate activityContextReloadDelegate) {
		this.activityContextReloadDelegate = activityContextReloadDelegate;
		this.aspectranClassLoader = activityContextReloadDelegate.getAspectranClassLoader();
		
		if(aspectranClassLoader != null)
			this.resources = aspectranClassLoader.extractResources();
		else
			this.resources = null;
	}
	
	public void run() {
		if(resources == null || modified)
			return;
		
		for(URL url : resources) {
			try {
				File file = new File(url.toURI());
				
				long modifiedTime = file.lastModified();
				long modifiedTime2 = modifyTimeMap.get(file.getAbsoluteFile());
				
				if(modifiedTime != modifiedTime2) {
					modified = true;
					
					if(debugEnabled) {
						logger.debug("File Modification Detected: " + url);
					}
					
					break;
				}
			} catch(URISyntaxException e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		if(modified) {
			activityContextReloadDelegate.reload();
			modified = false;
		}
	}

}

