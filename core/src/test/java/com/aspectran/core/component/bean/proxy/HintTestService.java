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

import com.aspectran.core.component.bean.annotation.Advisable;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Hint;

@Component
@Bean
public class HintTestService {

    @Advisable
    @Hint(type = "layout", value = "name: popup, width: 800")
    @Hint(type = "transactional", value = "readOnly: true")
    public void testHint() {
        // Basic functionality check
    }

    @Hint(type = "test", value = "val: outer", propagated = true)
    public void outerPropagated() {
        inner();
    }

    @Hint(type = "test", value = "val: outer", propagated = false)
    public void outerIsolated() {
        inner();
    }

    @Advisable
    public void inner() {
        // The aspect will verify the visibility of hints for this call
    }

}
