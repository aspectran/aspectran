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
package com.aspectran.core.context.asel;

import com.aspectran.core.activity.ActivityPerformException;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.token.TokenEvaluator;
import com.aspectran.core.context.asel.token.TokenParser;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TokenEvaluationTest {

    private ActivityContext context;

    @BeforeAll
    void setUp() throws ActivityContextBuilderException {
        ActivityContextBuilder builder = new HybridActivityContextBuilder();
        context = builder.build("classpath:config/asel/asel-test-config.xml");
    }

    @Test
    void testEvaluateAsString() throws ActivityPerformException {
        InstantActivity activity = new InstantActivity(context);
        String result = activity.perform(() -> {
            activity.getRequestAdapter().setParameter("param1", "Apple");
            activity.getRequestAdapter().setAttribute("attr1", "Strawberry");

            Token[] tokens = TokenParser.parse("${param1}, ${param2:Tomato}, @{attr1}, @{attr2:Melon}");

            TokenEvaluator tokenEvaluator = activity.getTokenEvaluator();
            return tokenEvaluator.evaluateAsString(tokens);
        });
        assertEquals("Apple, Tomato, Strawberry, Melon", result);
    }

    @Test
    void testEvaluateArray() throws ActivityPerformException {
        InstantActivity activity = new InstantActivity(context);
        activity.perform(() -> {
            String[] stringArray = new String[] {"One", "Two", "Three"};
            List<String> stringList = Arrays.asList(stringArray);

            activity.getRequestAdapter().setAttribute("stringArray", stringArray);
            activity.getRequestAdapter().setAttribute("stringList", stringList);

            Token[] tokens1 = TokenParser.parse("@{stringArray}");
            Token[] tokens2 = TokenParser.parse("@{stringList}");
            Token[] tokens3 = TokenParser.parse("@{stringArray}@{stringList}");

            TokenEvaluator tokenEvaluator = activity.getTokenEvaluator();
            String result1 = tokenEvaluator.evaluateAsString(tokens1);
            String result2 = tokenEvaluator.evaluateAsString(tokens2);
            String result3 = tokenEvaluator.evaluateAsString(tokens3);

            assertEquals("[One, Two, Three]", result1);
            assertEquals("[One, Two, Three]", result2);
            assertEquals("[One, Two, Three][One, Two, Three]", result3);

            return null;
        });
    }

    @Test
    void testPropertyTokens() throws ActivityPerformException {
        InstantActivity activity = new InstantActivity(context);
        activity.perform(() -> {
            TokenEvaluator evaluator = activity.getTokenEvaluator();

            // Test environment property
            assertEquals("My Property", evaluator.evaluateAsString(TokenParser.parse("%{my-prop}")));

            // Test environment property with default value
            assertEquals("My Property", evaluator.evaluateAsString(TokenParser.parse("%{my-prop:Default}")));
            assertEquals("Default", evaluator.evaluateAsString(TokenParser.parse("%{non-existent-prop:Default}")));

            // Test system property
            String javaVersion = System.getProperty("java.version");
            assertEquals(javaVersion, evaluator.evaluateAsString(TokenParser.parse("%{system:java.version}")));

            return null;
        });
    }

    @Test
    void testBeanTokens() throws ActivityPerformException {
        InstantActivity activity = new InstantActivity(context);
        activity.perform(() -> {
            TokenEvaluator evaluator = activity.getTokenEvaluator();

            assertEquals("I am a bean", evaluator.evaluateAsString(TokenParser.parse("#{sampleBean^name}")));
            assertEquals("Another Bean", evaluator.evaluateAsString(TokenParser.parse("#{anotherBean^name}")));

            // The primary bean of SampleBean type is 'sampleBean'
            assertEquals("I am a bean", evaluator.evaluateAsString(TokenParser.parse(
                    "#{class:com.aspectran.core.context.asel.TokenEvaluationTest$SampleBean^name}")));

            // Static field/property access
            assertEquals("a static field", evaluator.evaluateAsString(TokenParser.parse(
                    "#{field:com.aspectran.core.context.asel.TokenEvaluationTest$SampleBean^staticField}")));
            assertEquals("a static field", evaluator.evaluateAsString(TokenParser.parse(
                    "#{class:com.aspectran.core.context.asel.TokenEvaluationTest$SampleBean^staticField}")));

            // Test enum access
            Object result = evaluator.evaluate(TokenParser.parse(
                    "#{class:com.aspectran.core.context.asel.TokenEvaluationTest$SampleEnum^TWO}"));
            assertEquals(SampleEnum.TWO, result);
            return null;
        });
    }

    public enum SampleEnum {
        ONE, TWO, THREE
    }

    public static class SampleBean {

        private String name = "I am a bean";

        public static String staticField = "a static field";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public static String getStaticField() {
            return staticField;
        }

    }

}
