package com.aspectran.core.context.loader.reload;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.service.AspectranService;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

public class ActivityContextReloadingTimerTask extends TimerTask {
	
	private final Log log = LogFactory.getLog(ActivityContextReloadingTimerTask.class);
	
	private final boolean debugEnabled = log.isDebugEnabled();

	private final AspectranService aspectranService;
	
	private final AspectranClassLoader aspectranClassLoader;
	
	private final URL[] resources;
	
	private Map<String, Long> modifyTimeMap = new HashMap<String, Long>();
	
	private boolean modified = false;
	
	private int cycle;
	
	public ActivityContextReloadingTimerTask(AspectranService aspectranService) {
		this.aspectranService = aspectranService;
		this.aspectranClassLoader = aspectranService.getAspectranClassLoader();
		
		if(aspectranClassLoader != null)
			this.resources = aspectranClassLoader.extractResources();
		else
			this.resources = null;
	}
	
	public void run() {
		if(resources == null || modified)
			return;
		
		for(URL url : resources) {
			//log.debug("Check File: " + url);
			try {
				File file = new File(url.toURI());
				String filePath = file.getAbsolutePath();
				
				long modifiedTime = file.lastModified();
				
				if(cycle == 0) {
					modifyTimeMap.put(filePath, modifiedTime);
				} else {
					Long modifiedTime2 = modifyTimeMap.get(filePath);
					
					if(modifiedTime2 != null) {
						if(modifiedTime != modifiedTime2.longValue()) {
							modified = true;
							
							if(debugEnabled) {
								log.debug("File Modification Detected: " + url);
							}
							
							break;
						}
					}
				}
			} catch(URISyntaxException e) {
				log.error(e.getMessage(), e);
			}
			
			cycle++;
		}
		
		if(modified) {
			aspectranService.restart();
			modified = false;
		}
	}

}

