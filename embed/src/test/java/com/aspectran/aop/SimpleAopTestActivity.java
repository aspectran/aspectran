/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.aop;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Request;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.io.IOException;

/**
 * <p>Created: 2016. 11. 5.</p>
 */
@Component
public class SimpleAopTestActivity {

    private static final Logger logger = LoggerFactory.getLogger(SimpleAopTestActivity.class);

    @Request("aop/test/action1")
    public void action1(Translet translet) {
        logger.debug("===> Action1: [SimpleAopTestActivity]=== Action Result (Action-1)");
        SampleAnnotatedAspect sampleAnnotatedAspect = translet.getAspectAdviceBean("aspect02");
        sampleAnnotatedAspect.foo();
    }

    @Request("aop/test/action2")
    public void action2() {
        logger.debug("===> Action2: [SimpleAopTestActivity]=== Action Result (Action-2)");
        logger.debug("===> Action2: [SimpleAopTestActivity]=== Force Exception ==============");
        throw new SimpleAopTestException();
    }

    @Request("aop/test/action3-${param1}")
    public String action3(Translet translet, String param1) throws IOException {
        logger.debug("===> Action3: [SimpleAopTestActivity]=== Action Result (Action-3)");
        logger.debug("===> Action3: (PathVariable)param1: " + param1);
        translet.getResponseAdapter().getWriter().write(param1);
        return param1;
    }

}
