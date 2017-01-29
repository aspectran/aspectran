/**
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.scheduler.service;

import com.aspectran.core.context.ActivityContext;

/**
 * The Interface SchedulerService.
 */
public interface SchedulerService {

	int getStartDelaySeconds();
	
	void setStartDelaySeconds(int startDelaySeconds);
	
	boolean isWaitOnShutdown();
	
	void setWaitOnShutdown(boolean waitOnShutdown);
	
	void startup() throws SchedulerServiceException;
	
	void startup(int delaySeconds) throws SchedulerServiceException;
	
	void shutdown() throws SchedulerServiceException;
	
	void shutdown(boolean waitForJobsToComplete) throws SchedulerServiceException;

	void restart(ActivityContext context) throws SchedulerServiceException;

	void pause() throws SchedulerServiceException;

	void pause(String schedulerId) throws SchedulerServiceException;

	void resume() throws SchedulerServiceException;

	void resume(String schedulerId) throws SchedulerServiceException;

}
