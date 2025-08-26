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
 * Provides adapter classes for integrating the Quartz scheduler with Aspectran's activity model.
 * <p>These classes adapt Quartz's {@link org.quartz.JobExecutionContext} to Aspectran's
 * standard {@link com.aspectran.core.adapter.RequestAdapter} and
 * {@link com.aspectran.core.adapter.ResponseAdapter} interfaces, allowing scheduled jobs
 * to be treated like any other request within the framework.</p>
 */
package com.aspectran.core.scheduler.adapter;
