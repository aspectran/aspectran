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
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.service.CoreService;
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

    protected static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(AspectranExtension.class);

    @Override
    public void beforeAll(@NonNull ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        AspectranTest annotation = testClass.getAnnotation(AspectranTest.class);
        if (annotation != null) {
            AspectranConfig aspectranConfig = loadConfig(context, annotation);
            applyAnnotationSettings(annotation, aspectranConfig);
            bootstrap(context, annotation, aspectranConfig);
        }
    }

    protected AspectranConfig loadConfig(@NonNull ExtensionContext context, @NonNull AspectranTest annotation)
            throws Exception {
        AspectranConfig aspectranConfig = null;
        String configFile = annotation.configFile();
        Class<? extends AspectranConfigProvider> configProviderClass = annotation.configProvider();
        boolean isProviderImplemented = AspectranConfigProvider.class.isAssignableFrom(context.getRequiredTestClass());

        if (StringUtils.hasText(configFile) && (configProviderClass != AspectranConfigProvider.class || isProviderImplemented)) {
            throw new IllegalArgumentException("Only one of 'configFile' or 'configProvider' can be specified in @AspectranTest");
        }

        if (configProviderClass != AspectranConfigProvider.class) {
            aspectranConfig = configProviderClass.getDeclaredConstructor().newInstance().getConfig();
        } else if (isProviderImplemented) {
            AspectranConfigProvider provider = (AspectranConfigProvider)context.getTestInstance().orElse(null);
            if (provider != null) {
                aspectranConfig = provider.getConfig();
            } else {
                aspectranConfig = ((AspectranConfigProvider)context.getRequiredTestClass().getDeclaredConstructor().newInstance()).getConfig();
            }
        } else if (StringUtils.hasText(configFile)) {
            aspectranConfig = new AspectranConfig(ResourceUtils.getResourceAsFile(configFile));
        }

        return (aspectranConfig != null ? aspectranConfig : new AspectranConfig());
    }

    protected void applyAnnotationSettings(@NonNull AspectranTest annotation, @NonNull AspectranConfig aspectranConfig)
            throws Exception {
        ContextConfig contextConfig = aspectranConfig.touchContextConfig();
        if (StringUtils.hasText(annotation.basePath())) {
            contextConfig.setBasePath(annotation.basePath());
        } else {
            File baseDir = ResourceUtils.getResourceAsFile(".");
            contextConfig.setBasePath(baseDir.getCanonicalPath());
        }
        if (annotation.profiles().length > 0) {
            contextConfig.touchProfilesConfig().setActiveProfiles(annotation.profiles());
        }
        if (annotation.basePackages().length > 0) {
            contextConfig.setBasePackage(annotation.basePackages());
        }
        if (annotation.async()) {
            contextConfig.touchAsyncConfig().setEnabled(true);
        }
        String[] rules = (annotation.rules().length > 0 ? annotation.rules() : annotation.value());
        if (rules.length > 0) {
            contextConfig.setContextRules(rules);
        }
    }

    protected void bootstrap(@NonNull ExtensionContext context, @NonNull AspectranTest annotation, @NonNull AspectranConfig aspectranConfig)
            throws Exception {
        TestCoreService coreService = TestCoreService.build(aspectranConfig);
        ActivityContext activityContext = coreService.getActivityContext();

        context.getStore(NAMESPACE).put(TestCoreService.class, coreService);
        context.getStore(NAMESPACE).put(ActivityContext.class, activityContext);
        context.getStore(NAMESPACE).put(AspectranConfig.class, aspectranConfig);
    }

    @Override
    public void afterAll(@NonNull ExtensionContext context) throws Exception {
        TestCoreService coreService = context.getStore(NAMESPACE).get(TestCoreService.class, TestCoreService.class);
        if (coreService != null) {
            coreService.stop();
        }
    }

    @Override
    public boolean supportsParameter(@NonNull ParameterContext parameterContext, @NonNull ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        return (type == ActivityContext.class || type == InstantActivity.class ||
                type == ActivityTester.class || type == AspectranConfig.class ||
                type == TestCoreService.class || type == CoreService.class);
    }

    @Override
    public Object resolveParameter(@NonNull ParameterContext parameterContext, @NonNull ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        TestCoreService coreService = extensionContext.getStore(NAMESPACE).get(TestCoreService.class, TestCoreService.class);
        if (coreService != null) {
            if (type == ActivityContext.class) {
                return coreService.getActivityContext();
            } else if (type == InstantActivity.class) {
                return new InstantActivity(coreService.getActivityContext());
            } else if (type == ActivityTester.class) {
                return new ActivityTester(coreService.getActivityContext());
            } else if (type == AspectranConfig.class) {
                return extensionContext.getStore(NAMESPACE).get(AspectranConfig.class, AspectranConfig.class);
            } else if (type == TestCoreService.class || type == CoreService.class) {
                return coreService;
            }
        }
        return null;
    }

}
