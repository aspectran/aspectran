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
 * Provides the Activity implementation for scheduled jobs.
 * <p>This package contains the necessary classes to bridge the gap between a scheduler's
 * job execution (e.g., from Quartz) and Aspectran's activity lifecycle. When a scheduled
 * trigger fires, the classes in this package are responsible for initiating an
 * {@link com.aspectran.core.activity.Activity} that runs the specified translet.</p>
 */
package com.aspectran.core.scheduler.activity;
