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
 * Provides adapter implementations that bridge the Aspectran framework with the
 * standard Jakarta Servlet API, enabling Aspectran to operate in a web environment.
 * <p>This package contains adapters for {@code HttpServletRequest},
 * {@code HttpServletResponse}, and {@code HttpSession}, which wrap the native
 * Servlet objects to expose them through Aspectran's unified adapter interfaces.
 * This abstraction allows the core framework to remain independent of the
 * specific execution environment.
 */
package com.aspectran.web.adapter;
