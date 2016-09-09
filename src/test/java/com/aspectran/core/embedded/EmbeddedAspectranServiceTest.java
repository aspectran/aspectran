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

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.embedded.service.EmbeddedAspectranService;

/**
 * <p>Created: 2016. 9. 7.</p>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmbeddedAspectranServiceTest {

//	@Test
//	public void embeddedAspectranServiceTest() throws AspectranServiceException, IOException {
//		String rootContextLocation = "classpath:config/embedded-service-config.xml";
//		EmbeddedAspectranService aspectranService = EmbeddedAspectranService.newInstance(rootContextLocation);
//		ActivityContext activityContext = aspectranService.getActivityContext();
//		BeanRegistry beanRegistry = activityContext.getBeanRegistry();
//		FirstBean firstBean = beanRegistry.getBean("thirdBean");
//
//		System.out.println(firstBean.getMessage());
//	}

}