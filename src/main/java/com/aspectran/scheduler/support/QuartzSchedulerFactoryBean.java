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
package com.aspectran.scheduler.support;

import java.util.Properties;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import com.aspectran.core.context.bean.ablility.FactoryBean;

/**
 * The type Quartz scheduler factory bean.
 *
 * @since 3.0.0
 */
public class QuartzSchedulerFactoryBean implements FactoryBean<Scheduler> {

	private Properties props;

	public void setProps(Properties props) {
		this.props = props;
	}

	public Scheduler getObject() throws SchedulerException {
		if (props == null) {
			return StdSchedulerFactory.getDefaultScheduler();
		} else {
			SchedulerFactory schedulerFactory = new StdSchedulerFactory(props);
			return schedulerFactory.getScheduler();
		}
	}

}