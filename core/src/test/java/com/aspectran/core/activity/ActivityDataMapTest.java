package com.aspectran.core.activity;

import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Testcase for ActivityDataMap.
 *
 * <p>Created: 2017. 12. 4.</p>
 */
public class ActivityDataMapTest {

    @Test
    public void testEvaluateAsString() throws ActivityContextBuilderException {
        ActivityContextBuilder builder = new HybridActivityContextBuilder();
        ActivityContext context = builder.build();

        ParameterMap parameterMap = new ParameterMap();
        parameterMap.setParameter("param1", "Apple");
        parameterMap.setParameter("param2", "Tomato");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("attr1", "Strawberry");
        attributes.put("attr2", "Melon");

        Activity activity = new InstantActivity(context, parameterMap, attributes);

        ActivityDataMap activityDataMap = new ActivityDataMap(activity);
        activityDataMap.put("result1", 1);

        assertEquals("Apple", activityDataMap.getParameterWithoutCache("param1"));
        assertEquals("Apple", activityDataMap.get("param1"));
        assertEquals("Apple", activityDataMap.get("param1"));
        assertEquals("Strawberry", activityDataMap.getAttributeWithoutCache("attr1"));
        assertEquals("Strawberry", activityDataMap.get("attr1"));
        assertEquals("Strawberry", activityDataMap.get("attr1"));
        assertEquals(1, activityDataMap.get("result1"));
    }

}