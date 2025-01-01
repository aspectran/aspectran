/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.utils.MultiValueMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ItemEvaluationTest {

    private ActivityContext context;

    @BeforeAll
    void setUp() throws ActivityContextBuilderException {
        ActivityContextBuilder builder = new HybridActivityContextBuilder();
        context = builder.build();
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

}
