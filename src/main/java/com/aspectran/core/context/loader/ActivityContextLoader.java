/*
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
package com.aspectran.core.context.loader;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.loader.resource.AspectranClassLoader;
import com.aspectran.core.context.loader.resource.InvalidResourceException;

/**
 * Strategy interface for loading ActivityContext.
 */
public interface ActivityContextLoader {

	ApplicationAdapter getApplicationAdapter();
	
	AspectranClassLoader getAspectranClassLoader();
	
	void setResourceLocations(String[] resourceLocations) throws InvalidResourceException;

	String[] getActiveProfiles();

	void setActiveProfiles(String... activeProfiles);

	String[] getDefaultProfiles();

	void setDefaultProfiles(String... defaultProfiles);

	boolean isHybridLoad();

	void setHybridLoad(boolean hybridLoad);

	ActivityContext load(String rootContext) throws ActivityContextBuilderException, InvalidResourceException;
	
	ActivityContext reload(boolean hardReload) throws ActivityContextBuilderException, InvalidResourceException;

}
