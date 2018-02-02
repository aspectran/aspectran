/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Configuration;
import com.aspectran.core.context.rule.type.ScopeType;

@Configuration
public class TestConfiguration {

    @Autowired
    private FirstBean firstBean;

    @Bean(id = "thirdResult", scope = ScopeType.SINGLETON, lazyInit = true)
    public ThirdResult getThirdResult() {
        return new ThirdResult(firstBean);
    }

}
