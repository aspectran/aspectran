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
 * Provides classes for holding Aspectran's various configuration settings.
 *
 * <p>This package contains a set of Plain Old Java Objects (POJOs) that model the
 * configuration for different parts of the Aspectran framework. These classes are
 * designed to be populated from external sources like APON or XML files.
 *
 * <p>The configuration is hierarchical and modular, with
 * {@link com.aspectran.core.context.config.AspectranConfig} serving as the root container.
 * The primary configuration modules include:
 * <ul>
 *   <li>{@link com.aspectran.core.context.config.SystemConfig}: For system-level properties.</li>
 *   <li>{@link com.aspectran.core.context.config.ContextConfig}: The core and mandatory
 *       configuration for the ActivityContext, defining rules, component scanning, profiles, etc.</li>
 *   <li>{@link com.aspectran.core.context.config.SchedulerConfig}: For the built-in job scheduler.</li>
 *   <li>{@link com.aspectran.core.context.config.WebConfig}: For web application environments.</li>
 *   <li>{@link com.aspectran.core.context.config.DaemonConfig}: For background daemon services.</li>
 *   <li>{@link com.aspectran.core.context.config.ShellConfig}: For interactive CLI applications.</li>
 *   <li>{@link com.aspectran.core.context.config.EmbedConfig}: For embedded mode integration.</li>
 * </ul>
 *
 * These classes serve as data containers, making configuration information
 * easily and safely accessible to the framework's components during the
 * application startup process.
 */
package com.aspectran.core.context.config;
