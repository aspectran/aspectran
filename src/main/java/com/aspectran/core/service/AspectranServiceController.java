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
package com.aspectran.core.service;

/**
 * The Interface AspectranServiceController.
 */
public interface AspectranServiceController {
	
	/**
	 * Start the Aspectran Service.
	 *
	 * @throws AspectranServiceException the aspectran service exception
	 */
	void startup() throws AspectranServiceException;

	/**
	 * Restart the Aspectran Service.
	 *
	 * @throws AspectranServiceException the aspectran service exception
	 */
	void restart() throws AspectranServiceException;
	
	/**
	 * Pause the Aspectran service.
	 *
	 * @throws AspectranServiceException the aspectran service exception
	 */
	void pause() throws AspectranServiceException;
	
	/**
	 * Pause the Aspectran service for a specified period of time.
	 *
	 * @param timeout the maximum time to wait in milliseconds.
	 * @throws AspectranServiceException the aspectran service exception
	 */
	void pause(long timeout) throws AspectranServiceException;
	
	/**
	 * Continues a service after it has been paused.
	 *
	 * @throws AspectranServiceException the aspectran service exception
	 */
	void resume() throws AspectranServiceException;
	
	/**
	 * Destroys this service and any services that are dependent on this service.
	 *
	 * @throws AspectranServiceException the aspectran service exception
	 */
	void destroy() throws AspectranServiceException;
	
}
