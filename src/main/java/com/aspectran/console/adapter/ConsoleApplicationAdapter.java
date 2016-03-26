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
package com.aspectran.console.adapter;

import java.io.File;

import com.aspectran.core.adapter.CommonApplicationAdapter;
import com.aspectran.core.service.AspectranService;

/**
 * The Class ConsoleApplicationAdapter.
 * 
 * @author Juho Jeong
 * @since 2016. 1. 18.
 */
public class ConsoleApplicationAdapter extends CommonApplicationAdapter {
	
	/**
	 * Instantiates a new ConsoleApplicationAdapter.
	 *
	 * @param aspectranService the aspectran service
	 */
	public ConsoleApplicationAdapter(AspectranService aspectranService) {
		super(aspectranService, null);
		
		String applicationBasePath = System.getProperty("com.aspectran.console.workingDir");
		if(applicationBasePath == null)
			applicationBasePath = new File(".").getAbsolutePath();
		
		super.setApplicationBasePath(applicationBasePath);
	}

}
