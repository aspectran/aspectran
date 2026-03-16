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

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.test.AspectranExtension;
import com.aspectran.test.AspectranTest;
import com.aspectran.test.web.mock.MockServletContext;
import com.aspectran.web.service.DefaultWebService;
import com.aspectran.web.service.DefaultWebServiceBuilder;
import com.aspectran.web.service.WebService;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

/**
 * JUnit 5 extension for Aspectran Web.
 *
 * <p>Created: 2026. 3. 16.</p>
 */
public class WebAspectranExtension extends AspectranExtension {

    @Override
    public void beforeAll(@NonNull ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        WebAspectranTest annotation = testClass.getAnnotation(WebAspectranTest.class);
        if (annotation != null) {
            AspectranTest adapted = adapt(annotation);
            AspectranConfig aspectranConfig = loadConfig(context, adapted);
            applyAnnotationSettings(adapted, aspectranConfig);
            bootstrap(context, adapted, aspectranConfig);
        }
    }

    @Override
    protected void bootstrap(@NonNull ExtensionContext context, @NonNull AspectranTest annotation, @NonNull AspectranConfig aspectranConfig)
            throws Exception {
        MockServletContext servletContext = new MockServletContext();
        servletContext.setInitParameter("aspectran:config", aspectranConfig.toString(false));

        DefaultWebService webService = DefaultWebServiceBuilder.build(servletContext);
        webService.start();

        context.getStore(NAMESPACE).put(DefaultWebService.class, webService);
        context.getStore(NAMESPACE).put(ActivityContext.class, webService.getActivityContext());
        context.getStore(NAMESPACE).put(AspectranConfig.class, aspectranConfig);
    }

    @Override
    public void afterAll(@NonNull ExtensionContext context) throws Exception {
        DefaultWebService webService = context.getStore(NAMESPACE).get(DefaultWebService.class, DefaultWebService.class);
        if (webService != null) {
            webService.stop();
        }
    }

    @Override
    public boolean supportsParameter(@NonNull ParameterContext parameterContext, @NonNull ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        return super.supportsParameter(parameterContext, extensionContext) ||
                type == WebService.class || type == DefaultWebService.class || type == WebAspectranTester.class;
    }

    @Override
    public Object resolveParameter(@NonNull ParameterContext parameterContext, @NonNull ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        DefaultWebService webService = extensionContext.getStore(NAMESPACE).get(DefaultWebService.class, DefaultWebService.class);
        if (webService != null) {
            if (type == WebService.class || type == DefaultWebService.class) {
                return webService;
            } else if (type == WebAspectranTester.class) {
                return new WebAspectranTester(webService);
            }
        }
        return super.resolveParameter(parameterContext, extensionContext);
    }

    private AspectranTest adapt(@NonNull final WebAspectranTest web) {
        return new AspectranTest() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return AspectranTest.class;
            }

            @Override
            public String[] value() {
                return web.value();
            }

            @Override
            public String[] rules() {
                return web.rules();
            }

            @Override
            public String configFile() {
                return web.configFile();
            }

            @Override
            public Class<? extends com.aspectran.test.AspectranConfigProvider> configProvider() {
                return web.configProvider();
            }

            @Override
            public String basePath() {
                return web.basePath();
            }

            @Override
            public String[] profiles() {
                return web.profiles();
            }

            @Override
            public String[] basePackages() {
                return web.basePackages();
            }

            @Override
            public boolean async() {
                return web.async();
            }

            @Override
            public boolean debugMode() {
                return web.debugMode();
            }
        };
    }

}
