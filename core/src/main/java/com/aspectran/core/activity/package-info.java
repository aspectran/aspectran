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
 * Provides the core abstractions and implementations for the Activity model.
 * <p>
 * This package defines the 'Activity' as the central context for a unit of work in
 * Aspectran. It is used to process requests, execute business logic, and render
 * responses. This package includes the main contracts and base classes for all
 * activity-related operations.
 * </p>
 *
 * <p>Key types include:</p>
 * <ul>
 *   <li>{@link com.aspectran.core.activity.Activity} — The primary contract representing the
 *       context for a single, executable unit of work.</li>
 *   <li>{@link com.aspectran.core.activity.Translet} — The core executable plan derived
 *       from a user request, containing the actions to be performed.</li>
 *   <li>{@link com.aspectran.core.activity.InstantActivity} — A specialized activity that
 *       can be created and run programmatically for ad-hoc tasks requiring access to the
 *       application context.</li>
 *   <li>{@link com.aspectran.core.activity.FlashMap} and
 *       {@link com.aspectran.core.activity.FlashMapManager} — Utilities for passing
 *       attributes between requests, typically across a redirect.</li>
 * </ul>
 */
package com.aspectran.core.activity;
