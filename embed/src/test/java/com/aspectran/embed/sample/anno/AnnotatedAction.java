/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.embed.sample.anno;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Parameter;
import com.aspectran.core.component.bean.annotation.Request;

@Component
@Bean
public class AnnotatedAction {

    @Request(translet = "/action1",
            parameters = {
                    @Parameter(
                            name = "param1",
                            value = "Apple"
                    ),
                    @Parameter(
                            name = "param2",
                            value = "Tomato"
                    )
            }
    )
    public void action1(Translet translet, String param1, String param2) {
        System.out.println(translet);
        System.out.println("param1: " + param1);
        System.out.println("param2: " + param2);
    }

}
