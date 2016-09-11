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
package com.aspectran.core.embedded;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.embedded.service.EmbeddedAspectranService;

/**
 * <p>Created: 2016. 9. 7.</p>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmbeddedAspectranServiceTest {

	private EmbeddedAspectranService aspectranService;

	@Before
	public void ready() throws IOException, AspectranServiceException {
		String rootContextLocation = "classpath:config/embedded-service-config.xml";
		aspectranService = EmbeddedAspectranService.newInstance(rootContextLocation);
	}

	@After
	public void finish() {
		if (aspectranService != null) {
			aspectranService.shutdown();
		}
	}

	@Test
	public void beanRegistryTest() throws AspectranServiceException, IOException {
		ActivityContext activityContext = aspectranService.getActivityContext();
		BeanRegistry beanRegistry = activityContext.getBeanRegistry();
		FirstBean firstBean = beanRegistry.getBean("thirdBean");

		//System.out.println(firstBean);
		//System.out.println(firstBean.getMessage());

		Assert.assertEquals(firstBean.getMessage(), SecondBean.message);
	}

	@Test
	public void transeltRunTest() throws AspectranServiceException, IOException {
		Translet translet = aspectranService.translet("echo");
		System.out.println(translet.getResponseAdapter().getWriter().toString());
	}

}