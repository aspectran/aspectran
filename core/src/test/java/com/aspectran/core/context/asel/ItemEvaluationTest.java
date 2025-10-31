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
import com.aspectran.core.context.asel.item.ItemEvaluator;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.utils.MultiValueMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ItemEvaluationTest {

    private ActivityContext context;

    @BeforeAll
    void setUp() throws ActivityContextBuilderException {
        ActivityContextBuilder builder = new HybridActivityContextBuilder();
        context = builder.build("classpath:config/asel/asel-test-config.xml");
    }

    @Test
    void testEvaluateAsMultiValueMap() throws ActivityPerformException {
        InstantActivity activity = new InstantActivity(context);
        MultiValueMap<String, String> result = activity.perform(() -> {
            activity.getRequestAdapter().setParameter("param1", "Apple");
            activity.getRequestAdapter().setAttribute("attr1", "Strawberry");

            ItemRule itemRule1 = new ItemRule();
            itemRule1.setName("item1");
            itemRule1.setValue("${param1}, ${param2:Tomato}, @{attr1}, @{attr2:Melon}");

            ItemRule itemRule2 = new ItemRule();
            itemRule2.setType(ItemType.ARRAY);
            itemRule2.setName("item2");
            itemRule2.addValue("${param1}");
            itemRule2.addValue("${param2:Tomato}");
            itemRule2.addValue("@{attr1}");
            itemRule2.addValue("@{attr2:Melon}");

            ItemRuleMap itemRuleMap = new ItemRuleMap();
            itemRuleMap.putItemRule(itemRule1);
            itemRuleMap.putItemRule(itemRule2);

            ItemEvaluator itemEvaluator = activity.getItemEvaluator();
            return itemEvaluator.evaluateAsMultiValueMap(itemRuleMap);
        });
        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            if ("item1".equals(entry.getKey())) {
                assertEquals("[Apple, Tomato, Strawberry, Melon]", entry.getValue().toString());
            } else if ("item2".equals(entry.getKey())) {
                assertEquals("[Apple, Tomato, Strawberry, Melon]", entry.getValue().toString());
            }
        }
    }

    @Test
    void testPropertyAndBeanEvaluation() throws ActivityPerformException {
        InstantActivity activity = new InstantActivity(context);
        Map<String, Object> result = activity.perform(() -> {
            ItemRule itemRule1 = new ItemRule();
            itemRule1.setName("propItem");
            itemRule1.setValue("%{my-prop}");

            ItemRule itemRule2 = new ItemRule();
            itemRule2.setName("beanItem");
            itemRule2.setValue("#{sampleBean^name}");

            ItemRule itemRule3 = new ItemRule();
            itemRule3.setName("staticFieldItem");
            itemRule3.setValue("#{field:com.aspectran.core.context.asel.SampleBean^staticField}");

            ItemRule itemRule4 = new ItemRule();
            itemRule4.setName("enumItem");
            itemRule4.setValue("#{class:com.aspectran.core.context.asel.SampleEnum^TWO}");

            ItemRuleMap itemRuleMap = new ItemRuleMap();
            itemRuleMap.putItemRule(itemRule1);
            itemRuleMap.putItemRule(itemRule2);
            itemRuleMap.putItemRule(itemRule3);
            itemRuleMap.putItemRule(itemRule4);

            ItemEvaluator itemEvaluator = activity.getItemEvaluator();
            return itemEvaluator.evaluate(itemRuleMap);
        });

        assertEquals("My Property", result.get("propItem"));
        assertEquals("I am a bean", result.get("beanItem"));
        assertEquals("a static field", result.get("staticFieldItem"));
        assertEquals(SampleEnum.TWO, result.get("enumItem"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMapItemEvaluation() throws ActivityPerformException {
        InstantActivity activity = new InstantActivity(context);
        Map<String, Object> result = activity.perform(() -> {
            activity.getRequestAdapter().setParameter("param1", "Apple");
            activity.getRequestAdapter().setAttribute("attr1", "Strawberry");

            ItemRule itemRule = new ItemRule();
            itemRule.setType(ItemType.MAP);
            itemRule.setName("mapItem");
            itemRule.putValue("param", "${param1}");
            itemRule.putValue("attr", "@{attr1}");
            itemRule.putValue("prop", "%{my-prop}");
            itemRule.putValue("bean", "#{sampleBean^name}");

            ItemRuleMap itemRuleMap = new ItemRuleMap();
            itemRuleMap.putItemRule(itemRule);

            ItemEvaluator itemEvaluator = activity.getItemEvaluator();
            Map<String, Object> map = itemEvaluator.evaluate(itemRuleMap);
            return (Map<String, Object>)map.get("mapItem");
        });

        assertNotNull(result);
        assertEquals("Apple", result.get("param"));
        assertEquals("Strawberry", result.get("attr"));
        assertEquals("My Property", result.get("prop"));
        assertEquals("I am a bean", result.get("bean"));
    }

    @Test
    void testCollectionItemEvaluation() throws ActivityPerformException {
        InstantActivity activity = new InstantActivity(context);
        Map<String, Object> result = activity.perform(() -> {
            String[] stringArray = new String[] {"One", "Two", "Three"};
            List<String> stringList = Arrays.asList(stringArray);

            activity.getRequestAdapter().setAttribute("stringArray", stringArray);
            activity.getRequestAdapter().setAttribute("stringList", stringList);

            ItemRule itemRule1 = new ItemRule();
            itemRule1.setName("arrayItem");
            itemRule1.setValue("@{stringArray}");

            ItemRule itemRule2 = new ItemRule();
            itemRule2.setName("listItem");
            itemRule2.setValue("@{stringList}");

            ItemRuleMap itemRuleMap = new ItemRuleMap();
            itemRuleMap.putItemRule(itemRule1);
            itemRuleMap.putItemRule(itemRule2);

            ItemEvaluator itemEvaluator = activity.getItemEvaluator();
            return itemEvaluator.evaluate(itemRuleMap);
        });

        assertArrayEquals(new String[] {"One", "Two", "Three"}, (String[])result.get("arrayItem"));
        assertEquals(Arrays.asList("One", "Two", "Three"), result.get("listItem"));
    }

    @Test
    void testBeanItemEvaluation() throws ActivityPerformException {
        InstantActivity activity = new InstantActivity(context);
        Map<String, Object> result = activity.perform(() -> {
            activity.getRequestAdapter().setParameter("param1", "Injected Name");

            // 1. Item referencing an existing bean by ID
            ItemRule itemRule1 = new ItemRule();
            itemRule1.setName("beanById");
            BeanRule beanRule1 = new BeanRule();
            beanRule1.setId("sampleBean");
            beanRule1.setBeanClass(SampleBean.class);
            itemRule1.setBeanRule(beanRule1);

            // 2. Item defining a new bean inline with a property
            ItemRule itemRule2 = new ItemRule();
            itemRule2.setName("inlineBean");
            BeanRule beanRule2 = new BeanRule();
            beanRule2.setBeanClass(SampleBean.class);
            ItemRule propRule = new ItemRule();
            propRule.setName("name");
            propRule.setValue("${param1}");
            ItemRuleMap propMap = new ItemRuleMap();
            propMap.putItemRule(propRule);
            beanRule2.setPropertyItemRuleMap(propMap);
            itemRule2.setBeanRule(beanRule2);

            ItemRuleMap itemRuleMap = new ItemRuleMap();
            itemRuleMap.putItemRule(itemRule1);
            itemRuleMap.putItemRule(itemRule2);

            ItemEvaluator itemEvaluator = activity.getItemEvaluator();
            return itemEvaluator.evaluate(itemRuleMap);
        });

        // Assertions for bean by ID
        SampleBean bean1 = (SampleBean)result.get("beanById");
        assertNotNull(bean1);
        SampleBean originalBean = activity.getBean("sampleBean");
        assertNotSame(originalBean, bean1);
        assertEquals("I am a bean", bean1.getName());

        // Assertions for inline bean
        SampleBean bean2 = (SampleBean)result.get("inlineBean");
        assertNotNull(bean2);
        assertEquals("Injected Name", bean2.getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testBeanListEvaluation() throws ActivityPerformException {
        InstantActivity activity = new InstantActivity(context);
        List<Object> result = activity.perform(() -> {
            activity.getRequestAdapter().setParameter("param1", "Injected Name for List");

            ItemRule itemRule = new ItemRule();
            itemRule.setType(ItemType.LIST);
            itemRule.setName("beanList");

            // 1. Add an existing bean by ID
            BeanRule beanRule1 = new BeanRule();
            beanRule1.setId("sampleBean");
            beanRule1.setBeanClass(SampleBean.class);
            itemRule.addBeanRule(beanRule1);

            // 2. Add a new bean inline with a property
            BeanRule beanRule2 = new BeanRule();
            beanRule2.setBeanClass(SampleBean.class);
            ItemRule propRule = new ItemRule();
            propRule.setName("name");
            propRule.setValue("${param1}");
            ItemRuleMap propMap = new ItemRuleMap();
            propMap.putItemRule(propRule);
            beanRule2.setPropertyItemRuleMap(propMap);
            itemRule.addBeanRule(beanRule2);

            ItemRuleMap itemRuleMap = new ItemRuleMap();
            itemRuleMap.putItemRule(itemRule);

            ItemEvaluator itemEvaluator = activity.getItemEvaluator();
            Map<String, Object> map = itemEvaluator.evaluate(itemRuleMap);
            return (List<Object>)map.get("beanList");
        });

        assertNotNull(result);
        assertEquals(2, result.size());

        // Assertions for bean by ID
        SampleBean bean1 = (SampleBean)result.get(0);
        assertNotNull(bean1);
        SampleBean originalBean = activity.getBean("sampleBean");
        assertNotSame(originalBean, bean1);
        assertEquals("I am a bean", bean1.getName());

        // Assertions for inline bean
        SampleBean bean2 = (SampleBean)result.get(1);
        assertNotNull(bean2);
        assertEquals("Injected Name for List", bean2.getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testBeanMapEvaluation() throws ActivityPerformException {
        InstantActivity activity = new InstantActivity(context);
        Map<String, Object> result = activity.perform(() -> {
            activity.getRequestAdapter().setAttribute("attr1", "Injected Name for Map");

            ItemRule itemRule = new ItemRule();
            itemRule.setType(ItemType.MAP);
            itemRule.setName("beanMap");

            // 1. Put an existing bean by ID
            BeanRule beanRule1 = new BeanRule();
            beanRule1.setId("anotherBean");
            beanRule1.setBeanClass(SampleBean.class);
            itemRule.putBeanRule("existing", beanRule1);

            // 2. Put a new bean inline with a property
            BeanRule beanRule2 = new BeanRule();
            beanRule2.setBeanClass(SampleBean.class);
            ItemRule propRule = new ItemRule();
            propRule.setName("name");
            propRule.setValue("@{attr1}");
            ItemRuleMap propMap = new ItemRuleMap();
            propMap.putItemRule(propRule);
            beanRule2.setPropertyItemRuleMap(propMap);
            itemRule.putBeanRule("inline", beanRule2);

            ItemRuleMap itemRuleMap = new ItemRuleMap();
            itemRuleMap.putItemRule(itemRule);

            ItemEvaluator itemEvaluator = activity.getItemEvaluator();
            Map<String, Object> map = itemEvaluator.evaluate(itemRuleMap);
            return (Map<String, Object>)map.get("beanMap");
        });

        assertNotNull(result);
        assertEquals(2, result.size());

        // Assertions for bean by ID
        SampleBean bean1 = (SampleBean)result.get("existing");
        assertNotNull(bean1);
        SampleBean originalBean = activity.getBean("anotherBean");
        assertNotSame(originalBean, bean1);
        assertEquals("I am a bean", bean1.getName());

        // Assertions for inline bean
        SampleBean bean2 = (SampleBean)result.get("inline");
        assertNotNull(bean2);
        assertEquals("Injected Name for Map", bean2.getName());
    }

    @Test
    void testPropertiesItemEvaluation() throws ActivityPerformException {
        InstantActivity activity = new InstantActivity(context);
        Properties result = activity.perform(() -> {
            activity.getRequestAdapter().setAttribute("attr1", "strawberry");

            Properties props = new Properties();
            props.setProperty("key1", "value1");
            props.setProperty("key2", "Hello, @{attr1}");

            ItemRule itemRule = new ItemRule();
            itemRule.setName("propsItem");
            itemRule.setValue(props);

            ItemRuleMap itemRuleMap = new ItemRuleMap();
            itemRuleMap.putItemRule(itemRule);

            ItemEvaluator itemEvaluator = activity.getItemEvaluator();
            Map<String, Object> map = itemEvaluator.evaluate(itemRuleMap);
            return (Properties)map.get("propsItem");
        });

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("value1", result.getProperty("key1"));
        assertEquals("Hello, strawberry", result.getProperty("key2"));
    }

}
