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
package com.aspectran.core.component.bean;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.sample.profile.ProfiledBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProfiledBeanTest {

    private final File baseDir = new File("./target/test-classes");

    @Test
    void testProfiledBean() throws Exception {
        // Test with 'dev' profile
        ActivityContextBuilder devBuilder = new HybridActivityContextBuilder();
        devBuilder.setBasePath(baseDir.getCanonicalPath());
        devBuilder.setActiveProfiles("dev");
        ActivityContext devContext = devBuilder.build("/config/bean/call/profile-bean-test-config.xml");
        BeanRegistry devBeanRegistry = devContext.getBeanRegistry();

        ProfiledBean devBean = devBeanRegistry.getBean(ProfiledBean.class);
        assertNotNull(devBean);
        assertEquals("dev", devBean.getProfile());
        devBuilder.destroy();

        // Test with 'prod' profile
        ActivityContextBuilder prodBuilder = new HybridActivityContextBuilder();
        prodBuilder.setBasePath(baseDir.getCanonicalPath());
        prodBuilder.setActiveProfiles("prod");
        ActivityContext prodContext = prodBuilder.build("/config/bean/call/profile-bean-test-config.xml");
        BeanRegistry prodBeanRegistry = prodContext.getBeanRegistry();

        ProfiledBean prodBean = prodBeanRegistry.getBean(ProfiledBean.class);
        assertNotNull(prodBean);
        assertEquals("prod", prodBean.getProfile());
        prodBuilder.destroy();
    }

}
