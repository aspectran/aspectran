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
package com.aspectran.core.support;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.ablility.FactoryBean;
import com.aspectran.core.context.bean.aware.ActivityContextAware;

/**
 * {@code BasePathFactoryBean} that returns the base path under which the application is running.
 * This can be used to reference the base path by declaring it as a bean in the Aspectran configuration metadata.
 *
 * <p>Created: 2017. 1. 29.</p>
 */
public class BasePathFactoryBean implements ActivityContextAware, FactoryBean<String> {

	private String basePath;

	@Override
	public void setActivityContext(ActivityContext context) {
		this.basePath = context.getApplicationAdapter().getBasePath();
	}

	@Override
	public String getObject() throws Exception {
		return basePath;
	}

}
