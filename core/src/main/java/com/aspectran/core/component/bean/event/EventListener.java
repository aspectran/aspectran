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
package com.aspectran.core.component.bean.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a method as a listener for an application event.
 *
 * <p>The annotated method must have exactly one parameter, which is the event to
 * be handled. The container will inspect beans for this annotation and register
 * them as listeners for the specific event type declared in the method signature.
 *
 * <p>Example:
 * <pre class="code">
 * &#064;Component
 * public class MyEventListener {
 *     &#064;EventListener
 *     public void handleOrderCompleted(OrderCompletedEvent event) {
 *         // ... logic to handle the event
 *     }
 * }
 * </pre>
 *
 * @since 8.6.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventListener {
}
