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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.GenericApplicationAdapter;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.loader.resource.InvalidResourceException;

/**
 * <p>Created: 2016. 3. 26.</p>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ActivityContextLoaderTest {

	private ApplicationAdapter applicationAdapter;

	@Before
	public void ready() {
		applicationAdapter = new GenericApplicationAdapter();
	}

	@Test
	public void test1HybridLoading() {
		try {
			System.out.println("================ HybridActivityContextLoading ===============");

			ActivityContextLoader activityContextLoader = new HybridActivityContextLoader(applicationAdapter, "utf-8");
			activityContextLoader.setHybridLoad(true);
			activityContextLoader.setActiveProfiles("dev", "local");

			ClassLoader classLoader = activityContextLoader.newAspectranClassLoader();

			deleteResource(classLoader, "config/test-config.xml.apon");

			File file = getResource(classLoader, "config/test-config.xml");

			System.out.println("================ load ===============");

			activityContextLoader.load(file.getAbsolutePath());

			System.out.println("=============== reload ==============");

			activityContextLoader.reload(false);
		} catch(ActivityContextBuilderException | InvalidResourceException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test2XmlLoading() {
		try {
			System.out.println("================ XMLActivityContextLoading ===============");

			ActivityContextLoader activityContextLoader = new XmlActivityContextLoader(applicationAdapter);
			ClassLoader classLoader = activityContextLoader.newAspectranClassLoader();

			File file = getResource(classLoader, "config/test-config.xml");

			System.out.println("================ load ===============");

			activityContextLoader.load(file.getAbsolutePath());

			System.out.println("=============== reload ==============");

			activityContextLoader.reload(false);
		} catch(ActivityContextBuilderException | InvalidResourceException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test3AponLoading() {
		try {
			System.out.println("================ APONActivityContextLoading ===============");

			ActivityContextLoader activityContextLoader = new AponActivityContextLoader(applicationAdapter, "utf-8");
			ClassLoader classLoader = activityContextLoader.newAspectranClassLoader();

			File file = getResource(classLoader, "config/test-config.xml.apon");

			System.out.println("================ load ===============");

			activityContextLoader.load(file.getAbsolutePath());

			System.out.println("=============== reload ==============");

			activityContextLoader.reload(false);
		} catch(ActivityContextBuilderException | InvalidResourceException e) {
			e.printStackTrace();
		}
	}

	@After
	public void finish() {

	}

	private File getResource(ClassLoader classLoader, String resouceName) {
		URL url = classLoader.getResource(resouceName);
		assert url != null;
		return new File(url.getFile());
	}

	private void deleteResource(ClassLoader classLoader, String resouceName) {
		URL url = classLoader.getResource(resouceName);
		if(url != null) {
			File file = new File(url.getFile());
			file.delete();
		}
	}

}