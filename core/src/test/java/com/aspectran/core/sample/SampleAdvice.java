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
package com.aspectran.core.sample;

import com.aspectran.core.activity.Translet;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

public class SampleAdvice {

    private final Logger logger = LoggerFactory.getLogger(SampleAdvice.class);

    public String welcome(Translet translet) {
        String msg = "Welcome to Aspectran!";

        logger.info(msg);

        return msg;
    }

    public String goodbye(Translet translet) {
        logger.info("activityData " + translet.getActivityData());

        String msg = "Goodbye!";

        logger.info(msg);

        return msg;
    }

}
