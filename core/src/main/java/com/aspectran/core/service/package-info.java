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
 * Provides the core service interfaces and classes for the Aspectran framework.
 * <p>This package defines the fundamental concepts of a "service" in Aspectran,
 * including its lifecycle management and how it interacts with the core application
 * context. The central component is the {@link com.aspectran.core.service.CoreService},
 * which acts as the main entry point for an Aspectran application and provides
 * access to the {@link com.aspectran.core.context.ActivityContext}.
 *
 * <p>It establishes a hierarchical service model, allowing for parent-child
 * relationships between services and providing a robust foundation for building
 * various types of Aspectran applications (e.g., web, daemon, shell, embedded).
 */
package com.aspectran.core.service;
