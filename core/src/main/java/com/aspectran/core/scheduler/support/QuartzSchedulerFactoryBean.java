/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.core.scheduler.support;

import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.SchedulerRepository;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Properties;

/**
 * A {@link FactoryBean} that creates and configures a Quartz {@link Scheduler}.
 * <p>This factory bean is the recommended way to create a Quartz Scheduler instance
 * within an Aspectran application, as it provides a bean-style usage of the scheduler
 * and handles its configuration and lifecycle. It allows for the scheduler to be
 * managed by the Aspectran container and injected into other beans, such as the
 * {@link com.aspectran.core.scheduler.service.SchedulerService}.</p>
 *
 * @since 3.0.0
 */
public class QuartzSchedulerFactoryBean implements InitializableFactoryBean<Scheduler> {

    private String schedulerName;

    private Properties quartzProperties;

    private boolean exposeSchedulerInRepository;

    private Scheduler scheduler;

    /**
     * Sets the name of the Scheduler to create.
     * @param schedulerName the name of the scheduler
     * @see org.quartz.SchedulerFactory#getScheduler(String)
     */
    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    /**
     * Sets the native Quartz properties for the scheduler, such as thread pool settings.
     * @param quartzProperties the Quartz properties
     */
    public void setQuartzProperties(Properties quartzProperties) {
        this.quartzProperties = quartzProperties;
    }

    /**
     * Sets whether to expose the Aspectran-managed {@link Scheduler} instance in the
     * global Quartz {@link SchedulerRepository}.
     * <p>Default is "false", to prevent global state and ensure the scheduler is exclusively
     * managed by the Aspectran context. Switch this to "true" only if you need to access
     * the scheduler from outside the Aspectran container.</p>
     * @param exposeSchedulerInRepository {@code true} to expose the scheduler globally; {@code false} otherwise
     */
    public void setExposeSchedulerInRepository(boolean exposeSchedulerInRepository) {
        this.exposeSchedulerInRepository = exposeSchedulerInRepository;
    }

    /**
     * Creates and configures the Quartz {@link Scheduler} instance.
     * @return the newly created scheduler instance
     * @throws SchedulerException if the scheduler cannot be created
     */
    protected Scheduler createScheduler() throws SchedulerException {
        Properties props;
        if (quartzProperties != null) {
            props = new Properties(quartzProperties);
        } else {
            props = new Properties();
        }
        if (schedulerName != null) {
            props.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, schedulerName);
        }

        String schedulerName = props.getProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME);
        if (schedulerName != null) {
            SchedulerRepository repository = SchedulerRepository.getInstance();
            Scheduler existingScheduler = repository.lookup(schedulerName);
            if (existingScheduler != null) {
                throw new IllegalStateException("Active Scheduler of name '" + schedulerName + "' already registered " +
                        "in Quartz SchedulerRepository. Cannot create a new Aspectran-managed Scheduler of the same name!");
            }
        }

        SchedulerFactory schedulerFactory = new StdSchedulerFactory(props);
        Scheduler newScheduler = schedulerFactory.getScheduler();
        if (!exposeSchedulerInRepository) {
            // Need to remove it in this case, since Quartz shares the Scheduler instance by default!
            SchedulerRepository.getInstance().remove(newScheduler.getSchedulerName());
        }
        return newScheduler;
    }

    /**
     * Initializes the bean by creating the scheduler instance.
     * This method is called by the Aspectran bean lifecycle.
     * @throws Exception if an error occurs during initialization
     */
    @Override
    public void initialize() throws Exception {
        if (scheduler == null) {
            scheduler = createScheduler();
        }
    }

    /**
     * Returns the created Quartz {@link Scheduler} instance.
     * @return the scheduler object, or {@code null} if not initialized
     */
    @Override
    public Scheduler getObject() {
        return scheduler;
    }

}
