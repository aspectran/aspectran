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
package com.aspectran.console;

import com.aspectran.console.service.ConsoleAspectranService;

/**
 * The Aspectran Console Application.
 *
 * @since 2016. 1. 17.
 */
public class AspectranConsole {

	private static final String DEFAULT_ASPECTRAN_CONFIG_FILE = "aspectran-config.apon";

	public static void main(String[] args) {
		String aspectranConfigFile;

		if (args.length > 0) {
			aspectranConfigFile = args[0];
		} else {
			aspectranConfigFile = DEFAULT_ASPECTRAN_CONFIG_FILE;
		}

		int exitStatus = 0;

		try {
			ConsoleAspectranService.build(aspectranConfigFile).service();
		} catch (Exception e) {
			e.printStackTrace();
			exitStatus = 1;
		}

		System.exit(exitStatus);
	}

}
