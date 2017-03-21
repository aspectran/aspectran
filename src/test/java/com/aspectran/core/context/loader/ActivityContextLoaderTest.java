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

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.BasicApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.loader.resource.InvalidResourceException;

/**
 * <p>Created: 2016. 3. 26.</p>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ActivityContextLoaderTest {

	private File base;

	private ApplicationAdapter applicationAdapter;

	@Before
	public void ready() throws IOException {
		base = new File("./target/test-classes");
		BasicApplicationAdapter applicationAdapter = new BasicApplicationAdapter();
		applicationAdapter.setBasePath(base.getCanonicalPath());
		this.applicationAdapter = applicationAdapter;
	}

	@Test
	public void test1HybridLoading() throws ActivityContextBuilderException, InvalidResourceException, IOException {
		System.out.println("================ HybridActivityContextLoading ===============");

		ActivityContextLoader activityContextLoader = new HybridActivityContextLoader(applicationAdapter);
		activityContextLoader.setHybridLoad(true);
		activityContextLoader.setActiveProfiles("dev", "local");

		File apon1 = new File(base, "config/test-config.xml.apon");
		File apon2 = new File(base, "config/scheduler-config.xml.apon");

		apon1.delete();
		apon2.delete();
		
		System.out.println("================ load ===============");

		ActivityContext context = activityContextLoader.load("/config/test-config.xml");
		context.destroy();

		System.out.println("=============== reload ==============");

		context = activityContextLoader.reload(false);
		context.destroy();
	}

//	@Test
//	public void test2XmlLoading() throws ActivityContextBuilderException, InvalidResourceException, IOException {
//		System.out.println("================ XMLActivityContextLoading ===============");
//
//		File file = new File("./target/test-classes");
//		BasicApplicationAdapter applicationAdapter = new BasicApplicationAdapter();
//		applicationAdapter.setBasePath(file.getCanonicalPath());
//
//		ActivityContextLoader activityContextLoader = new XmlActivityContextLoader(applicationAdapter);
//
//		System.out.println("================ load ===============");
//
//		activityContextLoader.load("/config/test-config.xml");
//
//		System.out.println("=============== reload ==============");
//
//		activityContextLoader.reload(false);
//	}
//
//	@Test
//	public void test3AponLoading() throws ActivityContextBuilderException, InvalidResourceException, IOException {
//		System.out.println("================ APONActivityContextLoading ===============");
//
//		File file = new File("./target/test-classes");
//		BasicApplicationAdapter applicationAdapter = new BasicApplicationAdapter();
//		applicationAdapter.setBasePath(file.getCanonicalPath());
//
//		ActivityContextLoader activityContextLoader = new AponActivityContextLoader(applicationAdapter);
//
//		System.out.println("================ load ===============");
//
//		activityContextLoader.load("/config/test-config.xml.apon");
//
//		System.out.println("=============== reload ==============");
//
//		activityContextLoader.reload(false);
//	}

	@After
	public void finish() {
	}

//	private File getResource(ClassLoader classLoader, String resouceName) {
//		URL url = classLoader.getResource(resouceName);
//		assert url != null;
//		return new File(url.getFile());
//	}
//
//	private void deleteResource(ClassLoader classLoader, String resouceName) {
//		URL url = classLoader.getResource(resouceName);
//		if (url != null) {
//			File file = new File(url.getFile());
//			file.delete();
//		}
//	}

}