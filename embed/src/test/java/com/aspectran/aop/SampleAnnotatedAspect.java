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

import com.aspectran.core.component.bean.annotation.After;
import com.aspectran.core.component.bean.annotation.Around;
import com.aspectran.core.component.bean.annotation.Aspect;
import com.aspectran.core.component.bean.annotation.Before;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Description;
import com.aspectran.core.component.bean.annotation.ExceptionThrown;
import com.aspectran.core.component.bean.annotation.Finally;
import com.aspectran.core.component.bean.annotation.Joinpoint;
import com.aspectran.core.component.bean.annotation.Settings;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;

@Component
@Aspect(
        id = "aspect02",
        order = 2
)
@Joinpoint(
        target = JoinpointTargetType.METHOD,
        methods = {
                MethodType.GET
        },
        pointcut = {
                "+: aop/test/*",
                "-: aop/foo/*"
        }
)
@Settings({
       "setting1: value1",
       "setting2: value2",
       "setting3: value3"
})
@Description("The annotated aspect02")
public class SampleAnnotatedAspect {

    private static final Logger logger = LoggerFactory.getLogger(SampleAnnotatedAspect.class);

    @Before
    public String helloWorld() {
        String msg = "Before: Hello, World.";

        logger.info("===> aspect02: " + msg);

        return msg;
    }

    @After
    public String goodbye() {
        String msg = "After: Goodbye.";

        logger.info("===> aspect02: " + msg);

        return msg;
    }

    @Finally
    public String seeYouAgain() {
        String msg = "Finally: See you again.";

        logger.info("===> aspect02: " + msg);

        return msg;
    }

    @Around
    public String around() {
        String msg = "Around Method: Hi~~~~~~~~~~~~~~";

        logger.info("===> aspect02: " + msg);

        return msg;
    }

    @ExceptionThrown(SimpleAopTestException.class)
    public String oops() {
        String msg = "ExceptionThrown: Oops!!!!!!!!!!!!!!!";

        logger.info("===> aspect02: " + msg);

        return msg;
    }

    @ExceptionThrown
    public String oopsGlobal() {
        String msg = "ExceptionThrown: Global Oops!!!!!!!!!!!!!!!";

        logger.info("===> aspect02: " + msg);

        return msg;
    }

    public String foo() {
        return "foo (avoid)";
    }

}
