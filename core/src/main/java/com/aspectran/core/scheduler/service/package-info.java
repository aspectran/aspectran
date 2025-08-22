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
/**
 * Provides classes for integrating the Quartz scheduler with the Aspectran framework.
 * <p>This package allows for the definition and management of scheduled jobs as part
 * of the core Aspectran configuration. The central component is the
 * {@link com.aspectran.core.scheduler.service.SchedulerService}, which is a sub-service
 * managed by the main {@link com.aspectran.core.service.CoreService}. It uses the
 * Quartz library to execute Aspectran translets at specified times or intervals.
 */
package com.aspectran.core.scheduler.service;
