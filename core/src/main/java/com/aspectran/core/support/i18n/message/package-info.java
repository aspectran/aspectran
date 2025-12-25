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
 * Provides classes and interfaces for internationalization (i18n) support, allowing
 * applications to resolve text messages for different locales.
 *
 * <p>The central interface in this package is {@link com.aspectran.core.support.i18n.message.MessageSource},
 * which provides a standard way to resolve messages. The {@link com.aspectran.core.support.i18n.message.HierarchicalMessageSource}
 * sub-interface adds support for nested message resolution, allowing message sources
 * to be arranged in a parent-child hierarchy.</p>
 *
 * <p>The primary implementation is {@link com.aspectran.core.support.i18n.message.ResourceBundleMessageSource},
 * which resolves messages from standard Java {@link java.util.ResourceBundle} files.</p>
 */
package com.aspectran.core.support.i18n.message;
