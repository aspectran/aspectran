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
 * Provides interfaces and classes for asynchronous task execution.
 * <p>This package contains the core {@link com.aspectran.core.component.bean.async.AsyncTaskExecutor}
 * interface, along with implementations like {@link com.aspectran.core.component.bean.async.SimpleAsyncTaskExecutor}
 * (which creates a new thread for each task) and {@link com.aspectran.core.component.bean.async.ThreadPoolAsyncTaskExecutor}
 * (which uses a thread pool). It also includes supporting classes for exception handling and task decoration.</p>
 */
package com.aspectran.core.component.bean.async;
