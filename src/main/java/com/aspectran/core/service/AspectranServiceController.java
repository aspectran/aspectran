/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.service;

import com.aspectran.core.context.ActivityContext;

/**
 * The Interface AspectranServiceController.
 */
public interface AspectranServiceController {
	
	/**
	 * Starts a Aspectran Service.
	 *
	 * @return the activity context
	 * @throws AspectranServiceException the aspectran service exception
	 */
	public ActivityContext startup() throws AspectranServiceException;

	/**
	 * Restarts a Aspectran Service.
	 *
	 * @return returns whether or not the AcivityContext has been destroyed cleanly.
	 * @throws AspectranServiceException the aspectran service exception
	 */
	public boolean restart() throws AspectranServiceException;
	
	/**
	 * Reloads a Aspectran Configurations without restarting.
	 *
	 * @return returns whether or not the AcivityContext has been destroyed cleanly.
	 * @throws AspectranServiceException the aspectran service exception
	 */
	public boolean reload() throws AspectranServiceException;
	
	/**
	 * Refresh a Aspectran Service.
	 *
	 * @return returns whether or not the AcivityContext has been destroyed cleanly.
	 * @throws AspectranServiceException the aspectran service exception
	 */
	public boolean refresh() throws AspectranServiceException;
	
	/**
	 * Suspends a service's operation.
	 *
	 * @throws AspectranServiceException the aspectran service exception
	 */
	public void pause() throws AspectranServiceException;
	
	/**
	 * Suspends a service's operation.
	 *
	 * @param timeout - the maximum time to wait in milliseconds.
	 * @throws AspectranServiceException the aspectran service exception
	 */
	public void pause(long timeout) throws AspectranServiceException;
	
	/**
	 * Continues a service after it has been paused.
	 *
	 * @throws AspectranServiceException the aspectran service exception
	 */
	public void resume() throws AspectranServiceException;
	
	/**
	 * Stops this service and any services that are dependent on this service.
	 *
	 * @return returns whether or not the AcivityContext has been destroyed cleanly.
	 * @throws AspectranServiceException the aspectran service exception
	 */
	public boolean stop() throws AspectranServiceException;
	
}
