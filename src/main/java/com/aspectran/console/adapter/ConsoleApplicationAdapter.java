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
import java.io.IOException;

import com.aspectran.core.adapter.BasicApplicationAdapter;
import com.aspectran.core.util.SystemUtils;

/**
 * The Class ConsoleApplicationAdapter.
 * 
 * @since 2016. 1. 18.
 */
public class ConsoleApplicationAdapter extends BasicApplicationAdapter {
	
	private static final String WORKING_DIR_PROPERTY_NAME = "com.aspectran.console.workingDir";

	/**
	 * Instantiates a new ConsoleApplicationAdapter.
	 *
	 * @throws IOException if an I/O error has occurred
	 */
	public ConsoleApplicationAdapter() throws IOException {
		super(null);
		
		String applicationBasePath = SystemUtils.getProperty(WORKING_DIR_PROPERTY_NAME);
		if (applicationBasePath == null) {
			applicationBasePath = new File(".").getCanonicalPath();
		}

		super.setApplicationBasePath(applicationBasePath);
	}

}
