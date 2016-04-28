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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.AspectranClassLoader;

/**
 * The Interface AspectranService.
 */
public interface AspectranService extends AspectranServiceController {

	/**
	 * Gets the application adapter.
	 *
	 * @return the application adapter
	 */
	ApplicationAdapter getApplicationAdapter();
	
	/**
	 * Gets the activity context.
	 *
	 * @return the activity context
	 */
	ActivityContext getActivityContext();

	/**
	 * Gets the aspectran class loader.
	 *
	 * @return the aspectran class loader
	 */
	AspectranClassLoader getAspectranClassLoader();

	/**
	 * Sets the aspectran service controller listener.
	 *
	 * @param aspectranServiceControllerListener the new aspectran service controller listener
	 */
	void setAspectranServiceControllerListener(AspectranServiceControllerListener aspectranServiceControllerListener);
	
	/**
	 * Returns whether hard-reloading.
	 *
	 * @return true, if is hard reload
	 */
	boolean isHardReload();
	
	@Override
	void destroy();

	/**
	 * Returns whether this AspectranService is currently active.
	 *
	 * @return whether the AspectranService is still active
	 */
	boolean isActive();

}
