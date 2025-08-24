/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import com.aspectran.core.component.bean.async.AsyncTaskExecutor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a method as a candidate for asynchronous execution.
 *
 * <p>Created: 2024. 8. 24.</p>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Async {

    /**
     * The bean ID of the {@link AsyncTaskExecutor} to be used.
     * @return the bean ID of the {@link AsyncTaskExecutor}
     */
    String value() default "";

    /**
     * The type of the {@link AsyncTaskExecutor} to be used.
     * @return the type of the {@link AsyncTaskExecutor}
     */
    Class<? extends AsyncTaskExecutor> executor() default AsyncTaskExecutor.class;

}
