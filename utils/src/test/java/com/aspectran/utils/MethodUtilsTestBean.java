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
package com.aspectran.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class MethodUtilsTestBean {

    private static final Logger logger = LoggerFactory.getLogger(MethodUtilsTestBean.class);

    public void primitiveArray(int num) {
        logger.debug("specified args: {}", num);
    }

    public void primitiveArray(int[] intArray) {
        logger.debug("specified args: {}", Arrays.toString(intArray));
    }

    public String countTo10() {
        StringBuilder sb = new StringBuilder();

        for (int i = 1; i <= 10; i++) {
            sb.append(i).append("\n");
        }

        return sb.toString();
    }

    public void setSampleBean(MethodUtilsTestBean sampleBean) {
        logger.debug("specified args: {}", sampleBean);
    }

    public void setSampleBean(MethodUtilsTestBean[] sampleBean) {
        logger.debug("specified args: {}", Arrays.toString(sampleBean));
    }

    public void setSampleBean(List<MethodUtilsTestBean> list) {
        logger.debug("specified args: {}", list);
    }

    public static class ExtendedMethodUtilsTestBean extends MethodUtilsTestBean {
    }

}
