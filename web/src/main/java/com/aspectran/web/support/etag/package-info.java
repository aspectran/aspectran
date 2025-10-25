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
 * Provides server-side support for HTTP ETag (entity tag) generation and validation.
 * <p>ETags are used for web cache validation, allowing a client to make conditional
 * GET requests via the {@code If-None-Match} header. If the content has not changed,
 * the server can respond with a {@code 304 Not Modified} status, saving bandwidth.
 * This package contains the core components for integrating ETag support into an
 * Aspectran application.</p>
 */
package com.aspectran.web.support.etag;
