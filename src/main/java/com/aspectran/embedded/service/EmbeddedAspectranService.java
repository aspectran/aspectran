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
package com.aspectran.embedded.service;

import java.io.IOException;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.loader.config.AspectranConfig;
import com.aspectran.core.context.loader.config.AspectranContextConfig;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.context.translet.TransletNotFoundException;
import com.aspectran.core.service.AspectranServiceControllerListener;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.BasicAspectranService;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.embedded.activity.EmbeddedActivity;
import com.aspectran.embedded.adapter.EmbeddedApplicationAdapter;
import com.aspectran.embedded.adapter.EmbeddedSessionAdapter;

/**
 * The Class ConsoleAspectranService.
 */
public class EmbeddedAspectranService extends BasicAspectranService {

	private static final String DEFAULT_ROOT_CONTEXT = "classpath:embedded-aspectran-config.xml";

	private final SessionAdapter sessionAdapter;
	
	private long pauseTimeout;

	private EmbeddedAspectranService() {
		super(new EmbeddedApplicationAdapter());
		this.sessionAdapter = new EmbeddedSessionAdapter();
	}
	
	@Override
	protected void initialize(AspectranConfig aspectranConfig) throws AspectranServiceException {
		Parameters contextParameters = aspectranConfig.getParameters(AspectranConfig.context);
		if(contextParameters == null) {
			contextParameters = aspectranConfig.newParameters(AspectranConfig.context);
		}

		String rootContext = contextParameters.getString(AspectranContextConfig.root);
		if(rootContext == null || rootContext.isEmpty()) {
			contextParameters.putValue(AspectranContextConfig.root, DEFAULT_ROOT_CONTEXT);
		}
		
		super.initialize(aspectranConfig);
	}
	
	/**
	 * Process the actual dispatching to the activity. 
	 *
	 * @param transletName the translet name
	 */
	public void translet(String transletName, MethodType method) {
		if(pauseTimeout != 0L) {
			if(pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
				System.out.println("Aspectran Service has been paused, so did not respond to the command \"" + transletName + "\".");
				return;
			} else {
				pauseTimeout = 0L;
			}
		}

		Activity activity = null;

		try {
			activity = new EmbeddedActivity(getActivityContext(), sessionAdapter);
			activity.prepare(transletName, method);
			activity.perform();
		} catch(TransletNotFoundException e) {
			System.out.println("Translet is not found.");
		} finally {
			if(activity != null) {
				activity.finish();
			}
		}
	}

	@Override
	public void shutdown() {
		Scope scope = sessionAdapter.getSessionScope();
		scope.destroy();
		
		super.shutdown();
	}
	
	/**
	 * Returns a new instance of EmbeddedAspectranService.
	 *
	 * @param rootContextLocation the root context location
	 * @return the embedded aspectran service
	 * @throws AspectranServiceException the aspectran service exception
	 * @throws IOException if an I/O error has occurred
	 */
	public static EmbeddedAspectranService newInstance(String rootContextLocation) throws AspectranServiceException, IOException {
		AspectranConfig aspectranConfig = new AspectranConfig();
		aspectranConfig.updateRootContextLocation(rootContextLocation);
		return newInstance(aspectranConfig);
	}
	
	/**
	 * Returns a new instance of EmbeddedAspectranService.
	 *
	 * @param aspectranConfigText the aspectran configuration text
	 * @return the embedded aspectran service
	 * @throws AspectranServiceException the aspectran service exception
	 * @throws IOException if an I/O error has occurred
	 */
	public static EmbeddedAspectranService newInstance(AspectranConfig aspectranConfig) throws AspectranServiceException, IOException {
		EmbeddedAspectranService aspectranService = new EmbeddedAspectranService();
		aspectranService.initialize(aspectranConfig);
		
		setAspectranServiceControllerListener(aspectranService);
		
		aspectranService.startup();
		
		return aspectranService;
	}

	private static void setAspectranServiceControllerListener(final EmbeddedAspectranService aspectranService) {
		aspectranService.setAspectranServiceControllerListener(new AspectranServiceControllerListener() {
			@Override
			public void started() {
				aspectranService.pauseTimeout = 0;
			}
			
			@Override
			public void restarted(boolean hardReload) {
				started();
			}
			
			@Override
			public void paused(long millis) {
				if(millis < 0L) {
					throw new IllegalArgumentException("Pause timeout in milliseconds needs to be set to a value of greater than 0.");
				}
				aspectranService.pauseTimeout = System.currentTimeMillis() + millis;
			}
			
			@Override
			public void paused() {
				aspectranService.pauseTimeout = -1L;
			}
			
			@Override
			public void resumed() {
				started();
			}
			
			@Override
			public void stopped() {
				paused();
			}
		});
	}

}
