package com.aspectran.core.context.loader.reload;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.ActivityContext;

public class ActivityContextReloadingTimerTask extends TimerTask {
	
	private final Logger logger = LoggerFactory.getLogger(ActivityContextReloadingTimerTask.class);
	
	private final boolean debugEnabled = logger.isDebugEnabled();

	private ActivityContextReloadable activityContextReloadable;
	
	private Enumeration<URL> resources;
	
	private Map<URL, Long> modifyTimeMap;
	
	private boolean modified = false;
	
	public ActivityContextReloadingTimerTask(ActivityContextReloadable activityContextReloadable) {
		this.activityContextReloadable = activityContextReloadable;
		this.resources = activityContextReloadable.getResources();
	}
	
	public void run() {
		if(resources == null || modified)
			return;
		
		if(modifyTimeMap == null) {
			modifyTimeMap = new HashMap<URL, Long>();
		}
		
		while(resources.hasMoreElements()) {
			URL url = resources.nextElement();
			
			try {
				File file = new File(url.toURI());

				long modifiedTime = file.lastModified();
				long modifiedTime2 = modifyTimeMap.get(url);
				
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
			ActivityContext newActivityContext = activityContextReloadable.reload();
			modified = false;
		}
	}

}

