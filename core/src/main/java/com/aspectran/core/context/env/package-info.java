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
 * Provides classes for managing the application's runtime environment.
 *
 * <p>The core of this package is the {@link com.aspectran.core.context.env.Environment}
 * interface, which models two key aspects of the application environment:
 * <ul>
 *     <li><b>Profiles:</b> Logical groupings of bean definitions that can be
 *     conditionally registered based on the active environment (e.g., 'development',
 *     'production'). The {@link com.aspectran.core.context.env.Profiles} class
 *     provides a powerful expression language for complex profile logic.</li>
 *     <li><b>Properties:</b> A mechanism for managing configuration. In Aspectran,
 *     properties are defined as {@link com.aspectran.core.context.rule.ItemRule}s,
 *     allowing their values to be dynamically evaluated at runtime within the
 *     context of an {@link com.aspectran.core.activity.Activity}.</li>
 * </ul>
 * <p>This package enables the separation of configuration from application logic,
 * allowing applications to be easily adapted to different deployment scenarios.
 */
package com.aspectran.core.context.env;
