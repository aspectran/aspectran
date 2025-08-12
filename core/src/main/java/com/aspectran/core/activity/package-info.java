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
 * Core activity abstractions and implementations.
 * <p>
 * This package defines the Activity model used throughout Aspectran to
 * perform requests, execute business logic, and render responses. It includes
 * the main contracts and base classes for activities and their lightweight
 * instant variants used for programmatic, transactional work.
 * </p>
 *
 * <p>Key types:</p>
 * <ul>
 *   <li>{@link com.aspectran.core.activity.Activity Activity} — primary contract representing an executable unit</li>
 *   <li>{@link com.aspectran.core.activity.InstantActivity InstantActivity} and
 *       {@link com.aspectran.core.activity.InstantActivitySupport InstantActivitySupport} — helpers for
 *       ad-hoc, short-lived work that requires access to the activity context</li>
 *   <li>{@link com.aspectran.core.activity.Translet Translet} — an activity scoped to a single request</li>
 *   <li>{@link com.aspectran.core.activity.FlashMap FlashMap} and
 *       {@link com.aspectran.core.activity.FlashMapManager FlashMapManager} — utilities for passing
 *       attributes across redirects or successive interactions</li>
 * </ul>
 */
package com.aspectran.core.activity;
