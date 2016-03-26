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
package com.aspectran.core.context.loader;

import java.io.File;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.CommonApplicationAdapter;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.loader.resource.InvalidResourceException;

/**
 * <p>Created: 2016. 3. 26.</p>
 */
public class HybridActivityContextLoaderTest {

	@Before
	public void ready() {

	}

	@Test
	public void perform() {
		try {
			ApplicationAdapter applicationAdapter = new CommonApplicationAdapter(null);

			ActivityContextLoader activityContextLoader = new HybridActivityContextLoader(applicationAdapter, "utf-8");
			activityContextLoader.setHybridLoading(true);

			ClassLoader classLoader = activityContextLoader.newAspectranClassLoader();

			URL url = classLoader.getResource("config/test-config.xml");
			assert url != null;
			File file = new File(url.getFile());

			activityContextLoader.load(file.getAbsolutePath());

			System.out.println("=============== reload ==============");

			activityContextLoader.reload(true);
		} catch(ActivityContextBuilderException | InvalidResourceException e) {
			e.printStackTrace();
		}
	}

	@After
	public void finish() {

	}

}