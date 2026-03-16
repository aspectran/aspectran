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
package com.aspectran.test.web;

import com.aspectran.test.AspectranConfigProvider;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used on a test class to bootstrap an Aspectran
 * WebService for testing.
 *
 * <p>Created: 2026. 3. 16.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(WebAspectranExtension.class)
public @interface WebAspectranTest {

    /**
     * Alias for {@link #rules()}.
     * @return the context rules
     */
    String[] value() default {};

    /**
     * The Aspectran context rules to use for loading the activity context.
     * @return the context rules
     */
    String[] rules() default {};

    /**
     * The Aspectran configuration file (APON) to use for loading the activity context.
     * @return the configuration file
     */
    String configFile() default "";

    /**
     * The class that provides the Aspectran configuration object.
     * @return the config provider class
     */
    Class<? extends AspectranConfigProvider> configProvider() default AspectranConfigProvider.class;

    /**
     * The base path of the application.
     * @return the base path
     */
    String basePath() default "";

    /**
     * The profiles to activate.
     * @return the profiles
     */
    String[] profiles() default {};

    /**
     * The base packages to scan for components.
     * @return the base packages
     */
    String[] basePackages() default {};

    /**
     * Whether to enable asynchronous processing.
     * @return true if asynchronous processing is enabled, false otherwise
     */
    boolean async() default false;

    /**
     * Whether to enable debug mode.
     * @return true if debug mode is enabled, false otherwise
     */
    boolean debugMode() default false;

}
