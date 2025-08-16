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

import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.context.rule.type.MethodType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a join point definition for aspects to target.
 * Specifies the target type (e.g., activity), filtered request methods,
 * optional request headers, and pointcut expressions.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Joinpoint {

    /** The target type this join point applies to. */
    JoinpointTargetType target() default JoinpointTargetType.ACTIVITY;

    /** Allowed methods/HTTP verbs for this join point (if applicable). */
    MethodType[] methods() default {};

    /** Required request headers to match. */
    String[] headers() default {};

    /** Pointcut expressions for selecting join points. */
    String[] pointcut() default {};

}
