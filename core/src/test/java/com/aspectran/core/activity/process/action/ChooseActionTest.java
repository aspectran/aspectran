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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.ChooseRule;
import com.aspectran.core.context.rule.ChooseWhenRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.test.AspectranTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Test cases for {@link ChooseAction}.
 *
 * <p>Created: 2026. 10. 05</p>
 */
@AspectranTest
class ChooseActionTest {

    @Test
    void testWhenConditionMatchesFirst(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);
        activity.setAttributeMap(Map.of("score", 95));

        ChooseRule chooseRule = new ChooseRule();
        ChooseWhenRule whenGradeA = chooseRule.newChooseWhenRule();
        whenGradeA.setExpression("@{score} >= 90");

        ChooseWhenRule whenGradeB = chooseRule.newChooseWhenRule();
        whenGradeB.setExpression("@{score} >= 80");

        ChooseAction chooseAction = new ChooseAction(chooseRule);
        Object result = activity.perform(() -> chooseAction.execute(activity));

        assertSame(whenGradeA, result);
    }

    @Test
    void testWhenConditionMatchesSecond(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);
        activity.setAttributeMap(Map.of("score", 85));

        ChooseRule chooseRule = new ChooseRule();
        ChooseWhenRule whenGradeA = chooseRule.newChooseWhenRule();
        whenGradeA.setExpression("@{score} >= 90");

        ChooseWhenRule whenGradeB = chooseRule.newChooseWhenRule();
        whenGradeB.setExpression("@{score} >= 80");

        ChooseAction chooseAction = new ChooseAction(chooseRule);
        Object result = activity.perform(() -> chooseAction.execute(activity));

        assertSame(whenGradeB, result);
    }

    @Test
    void testOtherwiseBranch(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);
        activity.setAttributeMap(Map.of("score", 50));

        ChooseRule chooseRule = new ChooseRule();
        ChooseWhenRule whenGradeA = chooseRule.newChooseWhenRule();
        whenGradeA.setExpression("@{score} >= 90");

        ChooseWhenRule whenGradeB = chooseRule.newChooseWhenRule();
        whenGradeB.setExpression("@{score} >= 80");

        ChooseWhenRule otherwise = chooseRule.newChooseWhenRule();

        ChooseAction chooseAction = new ChooseAction(chooseRule);
        Object result = activity.perform(() -> chooseAction.execute(activity));

        assertSame(otherwise, result);
    }

    @Test
    void testNoConditionMatches(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);
        activity.setAttributeMap(Map.of("score", 50));

        ChooseRule chooseRule = new ChooseRule();
        ChooseWhenRule whenGradeA = chooseRule.newChooseWhenRule();
        whenGradeA.setExpression("@{score} >= 90");

        ChooseWhenRule whenGradeB = chooseRule.newChooseWhenRule();
        whenGradeB.setExpression("@{score} >= 80");

        ChooseAction chooseAction = new ChooseAction(chooseRule);
        Object result = activity.perform(() -> chooseAction.execute(activity));

        assertSame(Void.TYPE, result);
    }

    @Test
    void testEmptyChooseRule(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);

        ChooseRule chooseRule = new ChooseRule();
        ChooseAction chooseAction = new ChooseAction(chooseRule);

        assertEquals(ActionType.CHOOSE, chooseAction.getActionType());
        assertSame(chooseRule, chooseAction.getChooseRule());
        assertNotNull(chooseAction.toString());

        Object result = activity.perform(() -> chooseAction.execute(activity));
        assertSame(Void.TYPE, result);
    }

}
