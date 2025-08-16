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
package com.aspectran.core.component.bean.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares response dispatching metadata for an action/request method.
 * Allows specifying a view/target, optional name, dispatcher type,
 * response content type, and character encoding.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Dispatch {

    /** The dispatch target or path (e.g., view/template). */
    String value() default "";

    /** Optional name for this dispatch mapping. */
    String name() default "";

    /** Dispatcher name or type to use. */
    String dispatcher() default "";

    /** Response content type (e.g., text/html, application/json). */
    String contentType() default "";

    /** Character encoding for the response (e.g., UTF-8). */
    String encoding() default "";

}
