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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.NonActivity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.asel.value.ValueExpression;
import com.aspectran.test.AspectranTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive test cases for AsEL custom operators, focusing on edge cases and common mistakes.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AspectranTest
class CustomOperatorTest {

    private Activity activity;

    @BeforeAll
    void setUp(@NonNull ActivityContext context) {
        NonActivity activity = new NonActivity(context);

        // User with deep structure
        Map<String, Object> user = new HashMap<>();
        user.put("username", "tester");
        user.put("nickname", null);

        Map<String, Object> address = new HashMap<>();
        address.put("city", "Seoul");
        user.put("address", address);

        activity.getRequestAdapter().setAttribute("user", user);

        // Collection data
        List<Map<String, Object>> members = new ArrayList<>();
        members.add(Map.of("name", "Alice", "age", 25));
        members.add(Map.of("name", "Bob", "age", 18));
        members.add(Map.of("name", "Charlie", "age", 30));
        activity.getRequestAdapter().setAttribute("members", members);

        // Parameters
        activity.getRequestAdapter().setParameter("email", "test@example.com");
        activity.getRequestAdapter().setParameter("nullParam", (String)null);
        activity.getRequestAdapter().setParameter("emptyParam", "");

        this.activity = activity;
    }

    @Test
    void testElvisEdgeCases() {
        // Nested Elvis: a ?: b ?: c
        assertEquals("Fallback", ValueExpression.evaluate("@{user^nickname} ?: ${nullParam} ?: 'Fallback'", activity));

        // Whitespace variations
        assertEquals("tester", ValueExpression.evaluate("@{user^nickname}?:@{user^username}", activity));
        assertEquals("tester", ValueExpression.evaluate("@{user^nickname}  ?:  @{user^username}", activity));

        // Literal string containing Elvis pattern (should NOT be preprocessed)
        assertEquals("URL ?: test", ValueExpression.evaluate("'URL ?: test'", activity));
    }

    @Test
    void testSafeNavigationEdgeCases() {
        // Deep navigation with null in the middle
        assertNull(ValueExpression.evaluate("@{user}?.profile?.settings?.theme", activity));

        // Method call with Safe Navigation when receiver IS null
        activity.getRequestAdapter().setAttribute("nullUser", null);
        assertNull(ValueExpression.evaluate("@{nullUser}?.getName()", activity));

        // Mixed Dot and Safe Navigation
        // @{user}?.address.city -> user is not null, so address is accessed, then .city is accessed.
        assertEquals("Seoul", ValueExpression.evaluate("@{user}?.address.city", activity));
    }

    @Test
    void testCollectionEdgeCases() {
        // Empty result selection
        List<?> selected = (List<?>) ValueExpression.evaluate("@{members}.?[age > 100]", activity);
        assertTrue(selected.isEmpty());

        // Chained Projection and Selection
        // Filter age > 20, then get names, then get first
        assertEquals("Alice", ValueExpression.evaluate("@{members}.?[age > 20].![name][0]", activity));

        // First and Last operators
        Object firstResult = ValueExpression.evaluate("@{members}.^[age > 20]", activity);
        if (firstResult instanceof List<?> list && !list.isEmpty()) {
            assertEquals("Alice", ((Map<?,?>)list.get(0)).get("name"));
        } else if (firstResult instanceof Map<?,?> map) {
            assertEquals("Alice", map.get("name"));
        }

        Object lastResult = ValueExpression.evaluate("@{members}.$[age > 20]", activity);
        if (lastResult instanceof List<?> list && !list.isEmpty()) {
            assertEquals("Charlie", ((Map<?,?>)list.get(0)).get("name"));
        } else if (lastResult instanceof Map<?,?> map) {
            assertEquals("Charlie", map.get("name"));
        }
    }

    @Test
    void testTypeOperatorEdgeCases() {
        // Static field access
        assertEquals(Math.PI, (Double) ValueExpression.evaluate("T(java.lang.Math).PI", activity), 0.0001);

        // Static method with multiple arguments
        assertEquals("AB", ValueExpression.evaluate("T(java.lang.String).format('%s%s', 'A', 'B')", activity));

        // T(class) as method argument (using Boolean.valueOf)
        assertTrue((Boolean) ValueExpression.evaluate("T(java.lang.Boolean).valueOf('true')", activity));
    }

    @Test
    void testOperatorPrecedence() {
        // ?. has higher precedence than ?:
        // @{user}?.nickname ?: 'Anonymous'
        // 1. @{user}?.nickname is evaluated (results in null)
        // 2. null ?: 'Anonymous' is evaluated
        assertEquals("Anonymous", ValueExpression.evaluate("@{user}?.nickname ?: 'Anonymous'", activity));

        // Arithmetic vs Elvis
        // AsEL preprocessor wraps expressions in parentheses to ensure correct OGNL evaluation
        // (1 + 2) ?: 5
        assertEquals(3, ValueExpression.evaluate("1 + 2 ?: 5", activity));
    }

    @Test
    void testExtremeEdgeCases() {
        // 1. Escaped quotes in strings containing operator patterns
        // Note: AsEL/OGNL uses backslash for escape
        assertEquals("It's a ?: test", ValueExpression.evaluate("'It\\'s a ?: test' ?: 'fallback'", activity));

        // 2. Multiple nested operators in complex arithmetic
        // (1 + (null ?: 2)) * (null ?: 4) => (1 + 2) * 4 = 12
        assertEquals(12, ValueExpression.evaluate("(1 + (@{nullParam} ?: 2)) * (@{nullParam} ?: 4)", activity));

        // 3. Complex method arguments with nested operators
        // String.format('%s-%s', null ?: 'A', null?.b ?: 'B') => 'A-B'
        assertEquals("A-B", ValueExpression.evaluate("T(java.lang.String).format('%s-%s', @{nullParam} ?: 'A', @{nullUser}?.b ?: 'B')", activity));

        // 4. Safe Navigation combined with property access and indexing
        // members[0]?.name ?: 'Unknown'
        assertEquals("Alice", ValueExpression.evaluate("@{members}[0]?.name ?: 'Unknown'", activity));

        // 5. Deeply nested Safe Navigation and Elvis
        // a?.b?.c ?: d?.e?.f ?: 'End'
        assertEquals("End", ValueExpression.evaluate("@{nullUser}?.a?.b ?: @{nullUser}?.d?.e ?: 'End'", activity));
    }

    @Test
    void testVariableCollision() {
        // Potential flaw: we use #_res as a temporary variable.
        // What if the user also uses it?
        // This test might fail if our preprocessor is not careful about unique naming.
        activity.getRequestAdapter().setAttribute("_res", "UserValue");
        // Expression: use #_res, then use ?: which also uses #_res internally
        // If they collide, the result will be wrong.
        String expression = "#_res = 'Collision', @{user^nickname} ?: #_res";
        assertEquals("Collision", ValueExpression.evaluate(expression, activity));
    }

    @Test
    void testAbnormalWhitespaces() {
        // Preprocessor should be resilient to weird spacing
        assertEquals("tester", ValueExpression.evaluate("@{user^nickname}  ?:  @{user^username}", activity));
        // Note: '?. ' with space might be tricky
        assertEquals("Seoul", ValueExpression.evaluate("@{user} ?. address ?. city", activity));
    }

}
