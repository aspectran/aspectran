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
package com.aspectran.core.activity.process;

import com.aspectran.core.activity.process.action.EchoAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.context.rule.EchoActionRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>Created: 2026. 10. 3.</p>
 */
class ContentListTest {

    @Test
    void testActionListAndContentList() {
        ContentList contentList = ContentList.newInstance("mainContent");
        assertTrue(contentList.isExplicit());
        assertEquals("mainContent", contentList.getName());

        ActionList actionList = ActionList.newInstance("action1");
        EchoActionRule echoRule = new EchoActionRule();
        echoRule.setActionId("echo1");
        Executable echoAction = actionList.putActionRule(echoRule);

        assertNotNull(echoAction);
        assertEquals(1, actionList.size());
        assertEquals("action1", actionList.getName());

        contentList.addActionList(actionList);
        assertEquals(1, contentList.size());
        assertEquals(actionList, contentList.getActionList("action1"));
    }

    @Test
    void testDeepCopyReplication() {
        ContentList originalContentList = ContentList.newInstance("original");
        ActionList actionList = ActionList.newInstance("actions");
        actionList.add(new EchoAction(new EchoActionRule()));
        originalContentList.addActionList(actionList);

        ContentList replicatedContentList = originalContentList.replicate();

        assertNotSame(originalContentList, replicatedContentList);
        assertEquals(originalContentList.size(), replicatedContentList.size());
        assertEquals(originalContentList.getName(), replicatedContentList.getName());

        // Verify deep copy of ActionList inside ContentList
        ActionList originalActionList = originalContentList.getFirst();
        ActionList replicatedActionList = replicatedContentList.getFirst();
        assertNotSame(originalActionList, replicatedActionList);
        assertEquals(originalActionList.getName(), replicatedActionList.getName());
        assertEquals(originalActionList.size(), replicatedActionList.size());

        // Modifying replicated ActionList should not affect original ActionList
        replicatedActionList.add(new EchoAction(new EchoActionRule()));
        assertEquals(1, originalActionList.size());
        assertEquals(2, replicatedActionList.size());
    }

}
