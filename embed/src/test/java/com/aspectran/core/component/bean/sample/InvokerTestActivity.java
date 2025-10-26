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
package com.aspectran.core.component.bean.sample;

import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.ParamItem;
import com.aspectran.core.component.bean.annotation.Request;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Component
public class InvokerTestActivity {

    @Request("/parameter-binding-failure")
    @ParamItem(name = "num", value = "invalid-number")
    public void parameterBindingFailure(int num) {
        // The invoker will fail to bind 'invalid-number' to int.
        // Because the parameter is not required, it will not throw an exception.
        // The value for the primitive 'int' will default to 0.
        // This triggers the improved debug log for parameter binding failure.
        assertEquals(0, num);
    }

    @Request("/model-binding-failure")
    public void modelBindingFailure(TestModel model) {
        // The invoker will attempt to bind request parameters to the TestModel.
        // The 'number' parameter is 'invalid-number', which will cause a binding failure
        // for the 'number' property of TestModel.
        // This triggers the improved debug log for model property binding failure.
        assertEquals(0, model.getNumber());
    }

    @Request("/required-setter-missing")
    public void requiredSetterMissing(RequiredSetterModel model) {
        // The 'name' property of RequiredSetterModel has a @Required setter.
        // Before the fix, not providing the 'name' parameter would throw an
        // IllegalArgumentException from within bindModel.
        // After the fix, it should not throw, and the property will remain as its default value.
        assertEquals("default", model.getName());
    }

    @Request("/bind-model-features")
    public void bindModelFeatures(ComplexModel model) {
        // This action method is used to verify the comprehensive features of bindModel.
        assertEquals("tester", model.getName());
        assertEquals(30, model.getAge());
        assertArrayEquals(new String[]{"coding", "reading"}, model.getHobbies());

        assertNotNull(model.getJoinDate());
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        assertEquals("2025-10-26", sdf.format(model.getJoinDate()));
    }

}
