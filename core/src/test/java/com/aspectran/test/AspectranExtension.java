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
package com.aspectran.test;

import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.utils.ResourceUtils;
import com.aspectran.utils.StringUtils;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.io.File;

/**
 * JUnit 5 extension for Aspectran.
 *
 * <p>Created: 2026. 3. 16.</p>
 */
public class AspectranExtension implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(AspectranExtension.class);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        AspectranTest annotation = testClass.getAnnotation(AspectranTest.class);
        if (annotation != null) {
            HybridActivityContextBuilder builder = new HybridActivityContextBuilder();

            // Determine Base Path
            if (StringUtils.hasText(annotation.basePath())) {
                builder.setBasePath(annotation.basePath());
            } else {
                File baseDir = ResourceUtils.getResourceAsFile(".");
                builder.setBasePath(baseDir.getCanonicalPath());
            }

            AspectranConfig aspectranConfig = null;
            String configFile = annotation.configFile();
            Class<? extends AspectranConfigProvider> configProviderClass = annotation.configProvider();
            boolean isProviderImplemented = AspectranConfigProvider.class.isAssignableFrom(testClass);

            // Check for mutual exclusivity
            if (StringUtils.hasText(configFile) && (configProviderClass != AspectranConfigProvider.class || isProviderImplemented)) {
                throw new IllegalArgumentException("Only one of 'configFile' or 'configProvider' can be specified in @AspectranTest");
            }

            if (configProviderClass != AspectranConfigProvider.class) {
                // Load from specified ConfigProvider class
                AspectranConfigProvider provider = configProviderClass.getDeclaredConstructor().newInstance();
                aspectranConfig = provider.getConfig();
            } else if (isProviderImplemented) {
                // Load from test class instance if it implements AspectranConfigProvider
                AspectranConfigProvider provider = (AspectranConfigProvider)context.getTestInstance().orElse(null);
                if (provider != null) {
                    aspectranConfig = provider.getConfig();
                } else {
                    // If instance not available in beforeAll, try to create a temporary one if it has a default constructor
                    aspectranConfig = ((AspectranConfigProvider)testClass.getDeclaredConstructor().newInstance()).getConfig();
                }
            } else if (StringUtils.hasText(configFile)) {
                // Load from APON file
                File file = ResourceUtils.getResourceAsFile(configFile);
                aspectranConfig = new AspectranConfig(file);
            }

            if (aspectranConfig == null) {
                aspectranConfig = new AspectranConfig();
            }

            // Apply annotation settings to ContextConfig
            ContextConfig contextConfig = aspectranConfig.touchContextConfig();
            if (annotation.profiles().length > 0) {
                contextConfig.touchProfilesConfig().setActiveProfiles(annotation.profiles());
            }
            if (annotation.basePackages().length > 0) {
                contextConfig.setBasePackage(annotation.basePackages());
            }
            if (annotation.async()) {
                contextConfig.touchAsyncConfig().setEnabled(true);
            }

            builder.configure(aspectranConfig.getContextConfig());
            if (annotation.debugMode()) {
                builder.setDebugMode(true);
            }

            // Build ActivityContext with rules
            ActivityContext activityContext;
            String[] configs = (annotation.rules().length > 0 ? annotation.rules() : annotation.value());
            if (configs.length > 0) {
                activityContext = builder.build(configs);
            } else {
                activityContext = builder.build();
            }

            context.getStore(NAMESPACE).put(ActivityContextBuilder.class, builder);
            context.getStore(NAMESPACE).put(ActivityContext.class, activityContext);
            context.getStore(NAMESPACE).put(AspectranConfig.class, aspectranConfig);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        ActivityContextBuilder builder = context.getStore(NAMESPACE).get(ActivityContextBuilder.class, ActivityContextBuilder.class);
        if (builder != null) {
            builder.destroy();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        return (type == ActivityContext.class || type == InstantActivity.class || type == AspectranConfig.class);
    }

    @Override
    public Object resolveParameter(@NonNull ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        ActivityContext activityContext = extensionContext.getStore(NAMESPACE).get(ActivityContext.class, ActivityContext.class);
        if (activityContext != null) {
            if (type == ActivityContext.class) {
                return activityContext;
            } else if (type == InstantActivity.class) {
                return new InstantActivity(activityContext);
            } else if (type == AspectranConfig.class) {
                return extensionContext.getStore(NAMESPACE).get(AspectranConfig.class, AspectranConfig.class);
            }
        }
        return null;
    }

}
