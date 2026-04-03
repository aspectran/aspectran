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
package com.aspectran.core.component.bean.proxy;

import com.aspectran.core.activity.HintParameters;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.annotation.Aspect;
import com.aspectran.core.component.bean.annotation.Before;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Joinpoint;
import org.jspecify.annotations.NonNull;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Component
@Aspect(
        id = "hintTestAspect",
        order = 1
)
@Joinpoint(
        pointcut = "+: @class:com.aspectran.core.component.bean.proxy.HintTestService"
)
public class HintTestAspect {

    @Before
    public void testHint(@NonNull Translet translet) {
        HintParameters layoutHint = translet.peekHint("layout");
        assertNotNull(layoutHint, "layoutHint should not be null");
        assertEquals("popup", layoutHint.getString("name"));
        assertEquals(800, (int)layoutHint.getInt("width"));

        HintParameters txHint = translet.peekHint("transactional");
        assertNotNull(txHint, "txHint should not be null");
        assertTrue(txHint.getBoolean("readOnly"));
    }

}