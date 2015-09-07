/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.service;

import com.aspectran.core.context.ActivityContext;


public interface AspectranServiceController {
	
	public ActivityContext startup();

	public boolean refresh();
	
	/**
	 * Suspends a service's operation.
	 */
	public void pause();
	
	/**
	 * Suspends a service's operation.
	 * 
	 * @param timeout - the maximum time to wait in milliseconds. 
	 */
	public void pause(long timeout);
	
	/**
	 * Continues a service after it has been paused.
	 */
	public void resume();
	
	/**
	 * Stops this service and any services that are dependent on this service.
	 *
	 * @return true, if successful
	 */
	public boolean stop();
	
}
