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
package com.aspectran.aop;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.annotation.Advisable;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Created: 2016. 11. 5.</p>
 */
public class SimpleAopTestAdvice {

    private static final Logger logger = LoggerFactory.getLogger(SimpleAopTestAdvice.class);

    @Advisable
    public void begin(Translet translet) {
        logger.debug("===> aspect01: [SimpleAopTestAdvice]=== Begin");
    }

    public void end(Translet translet) {
        logger.debug("===> aspect01: [SimpleAopTestAdvice]=== End");
    }

    public void thrown(@NonNull Translet translet) {
        logger.debug("===> aspect01: [SimpleAopTestAdvice]=== Thrown - {}",
                String.valueOf(translet.getRootCauseOfRaisedException()));
    }

    public void close(Translet translet) {
        logger.debug("===> aspect01: [SimpleAopTestAdvice]=== Close");
    }

    public void globalExceptionHandling(Translet translet) {
        logger.debug("===> aspect01: [SimpleAopTestAdvice]=== globalExceptionHandling");
    }

}
