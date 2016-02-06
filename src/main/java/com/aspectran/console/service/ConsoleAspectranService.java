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
package com.aspectran.console.service;

import java.io.File;
import java.io.IOException;

import com.aspectran.console.activity.ConsoleActivity;
import com.aspectran.console.adapter.ConsoleApplicationAdapter;
import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.loader.config.AspectranConfig;
import com.aspectran.core.context.loader.config.AspectranContextConfig;
import com.aspectran.core.context.translet.TransletNotFoundException;
import com.aspectran.core.service.AspectranServiceControllerListener;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreAspectranService;
import com.aspectran.core.util.apon.AponDeserializer;
import com.aspectran.core.util.apon.Parameters;

/**
 * The Class ConsoleAspectranService.
 *
 * @since 2016. 1. 18.
 * @author Juho Jeong
 */
public class ConsoleAspectranService extends CoreAspectranService {

	private static final String DEFAULT_ROOT_CONTEXT = "config/aspectran-config.xml";

	protected long pauseTimeout;

	public ConsoleAspectranService() {
		ConsoleApplicationAdapter caa = new ConsoleApplicationAdapter(this);
		setApplicationAdapter(caa);
	}
	
	/**
	 * Process the actual dispatching to the activity. 
	 *
	 * @param command the translet name
	 */
	public void service(String command) {
		if(pauseTimeout > 0L) {
			if(pauseTimeout >= System.currentTimeMillis()) {
				System.out.println("Aspectran service is paused, did not respond to the command: " + command);
				return;
			} else {
				pauseTimeout = 0L;
			}
		}
		
		try {
			Activity activity = new ConsoleActivity(activityContext);
			activity.ready(command);
			activity.perform();
			activity.finish();
		} catch(TransletNotFoundException e) {
			System.out.println("Translet is not found.");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a new instance of ConsoleAspectranService.
	 *
	 * @param aspectranConfigFile the aspectran config file
	 * @return the web aspectran service
	 */
	public static ConsoleAspectranService newInstance(String aspectranConfigFile) throws AspectranServiceException, IOException {
		AspectranConfig aspectranConfig = new AspectranConfig();

		if(aspectranConfigFile != null && aspectranConfigFile.length() > 0) {
			AponDeserializer.deserialize(new File(aspectranConfigFile), aspectranConfig);
		}

		Parameters contextParameters = aspectranConfig.getParameters(AspectranConfig.context);

		if(contextParameters == null) {
			contextParameters = aspectranConfig.newParameters(AspectranConfig.context);
		}

		String rootContext = contextParameters.getString(AspectranContextConfig.root);

		if(rootContext == null || rootContext.length() == 0) {
			contextParameters.putValue(AspectranContextConfig.root, DEFAULT_ROOT_CONTEXT);
		}

		ConsoleAspectranService aspectranService = new ConsoleAspectranService();
		aspectranService.initialize(aspectranConfig);
		
		addAspectranServiceControllerListener(aspectranService);
		
		aspectranService.startup();
		
		return aspectranService;
	}

	private static void addAspectranServiceControllerListener(final ConsoleAspectranService aspectranService) {
		aspectranService.setAspectranServiceControllerListener(new AspectranServiceControllerListener() {
			public void started() {
				aspectranService.pauseTimeout = 0;
			}
			
			public void restarted() {
				started();
			}
			
			public void reloaded() {
				started();
			}
			
			public void paused(long timeout) {
				if(timeout <= 0)
					timeout = 315360000000L; //86400000 * 365 * 10 = 10 Years;
				
				aspectranService.pauseTimeout = System.currentTimeMillis() + timeout;
			}
			
			public void resumed() {
				aspectranService.pauseTimeout = 0;
			}
			
			public void stopped() {
				paused(-1L);
			}
		});
	}

}
