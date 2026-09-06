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
package com.aspectran.core.component.bean.annotation.multipart;

import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.context.rule.validation.BeanReferenceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvalidMultipartAnnotationTest {

    @Test
    void testNonExistentParserBean() {
        ActivityContextBuilderException e = assertThrows(ActivityContextBuilderException.class, () -> {
            HybridActivityContextBuilder builder = new HybridActivityContextBuilder();
            builder.setActiveProfiles("invalid-multipart-test");
            builder.setBasePackages("com.aspectran.core.component.bean.annotation.multipart");
            try {
                builder.build();
            } finally {
                builder.destroy();
            }
        });
        assertInstanceOf(BeanReferenceException.class, e.getCause());
        assertTrue(e.getCause().getMessage().contains("Cannot resolve reference to bean 'nonExistentParserBean'"));
        assertTrue(e.getCause().getMessage().contains("/test/invalid-upload"));
    }

}
