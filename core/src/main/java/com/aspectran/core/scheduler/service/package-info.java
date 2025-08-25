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
 * Provides the core interfaces and classes for the Aspectran Scheduler service.
 * <p>This package defines the abstract framework for scheduling jobs within Aspectran.
 * The central component is the {@link com.aspectran.core.scheduler.service.SchedulerService},
 * which defines the contract for managing the lifecycle of a scheduler. Concrete implementations
 * (e.g., a module using the Quartz library) can be plugged into this framework to execute
 * Aspectran translets at specified times or intervals.</p>
 */
package com.aspectran.core.scheduler.service;
