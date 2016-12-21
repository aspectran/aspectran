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
import com.aspectran.console.adapter.ConsoleSessionAdapter;
import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.aspect.SessionScopeAdvisor;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.loader.config.AspectranConfig;
import com.aspectran.core.context.loader.config.AspectranContextConfig;
import com.aspectran.core.context.translet.TransletNotFoundException;
import com.aspectran.core.service.AspectranServiceControllerListener;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.BasicAspectranService;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class ConsoleAspectranService.
 *
 * @since 2016. 1. 18.
 */
public class ConsoleAspectranService extends BasicAspectranService {

	private static final Log log = LogFactory.getLog(ConsoleAspectranService.class);

	private static final String DEFAULT_ROOT_CONTEXT = "config/aspectran-config.xml";

	private final SessionAdapter sessionAdapter;

	private SessionScopeAdvisor sessionScopeAdvisor;
	
	private long pauseTimeout;

	private ConsoleAspectranService() throws IOException {
		super(new ConsoleApplicationAdapter());
		this.sessionAdapter = new ConsoleSessionAdapter();
	}

	@Override
	public void afterStartup() {
		sessionScopeAdvisor = SessionScopeAdvisor.newInstance(getActivityContext(), this.sessionAdapter);
		if (sessionScopeAdvisor != null) {
			sessionScopeAdvisor.executeBeforeAdvice();
		}
	}

	@Override
	public void beforeShutdown() {
		if (sessionScopeAdvisor != null) {
			sessionScopeAdvisor.executeAfterAdvice();
		}
		Scope sessionScope = sessionAdapter.getSessionScope();
		sessionScope.destroy();
	}

	/**
	 * Process the actual dispatching to the activity. 
	 *
	 * @param command the translet name
	 */
	public void service(String command) {
		if (pauseTimeout != 0L) {
			if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
				System.out.println("AspectranService has been paused, so did not respond to the command \"" + command + "\".");
				return;
			} else {
				pauseTimeout = 0L;
			}
		}

		CommandParser commandParser = CommandParser.parseCommand(command);
		Activity activity = null;

		try {
			activity = new ConsoleActivity(getActivityContext(), sessionAdapter);
			activity.prepare(commandParser.getTransletName(), commandParser.getRequestMethod());
			activity.perform();
		} catch (TransletNotFoundException e) {
			System.out.println("Translet is not found.");
		} catch (ActivityTerminatedException e) {
			if (log.isDebugEnabled()) {
				log.debug("Translet activity was terminated.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (activity != null) {
				activity.finish();
			}
		}
	}

	/**
	 * Returns a new instance of ConsoleAspectranService.
	 *
	 * @param aspectranConfigFile the aspectran configuration file
	 * @return the console aspectran service
	 * @throws AspectranServiceException the aspectran service exception
	 * @throws IOException if an I/O error has occurred
	 */
	public static ConsoleAspectranService newInstance(String aspectranConfigFile)
			throws AspectranServiceException, IOException {
		AspectranConfig aspectranConfig = new AspectranConfig();
		if (aspectranConfigFile != null && !aspectranConfigFile.isEmpty()) {
			AponReader.parse(new File(aspectranConfigFile), aspectranConfig);
		}

		Parameters contextParameters = aspectranConfig.getParameters(AspectranConfig.context);
		if (contextParameters == null) {
			contextParameters = aspectranConfig.newParameters(AspectranConfig.context);
		}

		String rootContext = contextParameters.getString(AspectranContextConfig.root);
		if (rootContext == null || rootContext.length() == 0) {
			contextParameters.putValue(AspectranContextConfig.root, DEFAULT_ROOT_CONTEXT);
		}

		ConsoleAspectranService aspectranService = new ConsoleAspectranService();
		aspectranService.initialize(aspectranConfig);
		
		setAspectranServiceControllerListener(aspectranService);
		
		aspectranService.startup();

		return aspectranService;
	}

	private static void setAspectranServiceControllerListener(final ConsoleAspectranService aspectranService) {
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
				if (millis < 0L) {
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
