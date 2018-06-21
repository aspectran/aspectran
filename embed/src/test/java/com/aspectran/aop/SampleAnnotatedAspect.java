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
package com.aspectran.aop;

import com.aspectran.core.component.bean.annotation.After;
import com.aspectran.core.component.bean.annotation.Around;
import com.aspectran.core.component.bean.annotation.Aspect;
import com.aspectran.core.component.bean.annotation.Before;
import com.aspectran.core.component.bean.annotation.Configuration;
import com.aspectran.core.component.bean.annotation.Description;
import com.aspectran.core.component.bean.annotation.ExceptionThrown;
import com.aspectran.core.component.bean.annotation.Finally;
import com.aspectran.core.component.bean.annotation.Joinpoint;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

@Configuration
@Aspect(
        id = "aspect02",
        order = 2
)
@Joinpoint(
        target = JoinpointTargetType.METHOD,
        pointcut = "+aop/test/*"
)
@Description("The annotated aspect02")
public class SampleAnnotatedAspect {

    private final Log log = LogFactory.getLog(SampleAnnotatedAspect.class);

    @Before
    public String helloWorld() {
        String msg = "Hello, World.";

        log.info("===> aspect02: " + msg);

        return msg;
    }

    @After
    public String goodbye() {
        String msg = "Goodbye.";

        log.info("===> aspect02: " + msg);

        return msg;
    }

    @Finally
    public String seeYouAgain() {
        String msg = "See you again.";

        log.info("===> aspect02: " + msg);

        return msg;
    }

    @Around
    public String around() {
        String msg = "Hi~~~~~~~~~~~~~~";

        log.info("===> aspect02: " + msg);

        return msg;
    }

    @ExceptionThrown (type = SimpleAopTestException.class)
    public String oops() {
        String msg = "Oops!!!!!!!!!!!!!!!";

        log.info("===> aspect02: " + msg);

        return msg;
    }

    @ExceptionThrown
    public String oopsGloba() {
        String msg = "Global Oops!!!!!!!!!!!!!!!";

        log.info("===> aspect02: " + msg);

        return msg;
    }

    public String foo() {
        return "foo";
    }

}
