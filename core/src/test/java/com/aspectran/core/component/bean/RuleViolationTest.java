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

import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.ResourceUtils;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.aspectran.core.context.rule.parser.xml.AspectranNodeletGroup.MAX_INNER_BEAN_DEPTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test case for building ActivityContext.
 *
 * <p>Created: 2016. 3. 26.</p>
 */
class RuleViolationTest {

    @Test
    void overNestedInnerBeans() {
        assertThrows(IllegalRuleException.class, () -> {
            try {
                File baseDir = ResourceUtils.getResourceAsFile(".");
                ActivityContextBuilder builder = new HybridActivityContextBuilder();
                builder.setBasePath(baseDir.getCanonicalPath());
                builder.build("/config/bean/rule-violation-test1-config.xml");
                throw new Exception("No errors");
            } catch (Exception e) {
                Throwable cause = ExceptionUtils.getRootCause(e);
                assertEquals("Inner beans can be nested up to " + MAX_INNER_BEAN_DEPTH + " times", cause.getMessage());
                throw cause;
            }
        });
    }

    @Test
    void overChoose() {
        assertThrows(IllegalRuleException.class, () -> {
            try {
                File baseDir = ResourceUtils.getResourceAsFile(".");
                ActivityContextBuilder builder = new HybridActivityContextBuilder();
                builder.setBasePath(baseDir.getCanonicalPath());
                builder.build("/config/bean/rule-violation-test2-config.xml");
                throw new Exception("No errors");
            } catch (Exception e) {
                Throwable cause = ExceptionUtils.getRootCause(e);
                assertEquals("The <choose> element can be nested up to 2 times", cause.getMessage());
                throw cause;
            }
        });
    }

}
