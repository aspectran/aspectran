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
package com.aspectran.utils;

import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class MethodUtilsTestBean {

    private static final Logger logger = LoggerFactory.getLogger(MethodUtilsTestBean.class);

    @SuppressWarnings("unused")
    public void primitiveArray(int num) {
        logger.debug("specified args: " + num);
    }

    @SuppressWarnings("unused")
    public void primitiveArray(int[] intArray) {
        logger.debug("specified args: " + Arrays.toString(intArray));
    }

    @SuppressWarnings("unused")
    public String countTo10() {
        StringBuilder sb = new StringBuilder();

        for (int i = 1; i <= 10; i++) {
            sb.append(i).append("\n");
        }

        return sb.toString();
    }

    @SuppressWarnings("unused")
    public void setSampleBean(MethodUtilsTestBean sampleBean) {
        logger.debug("specified args: " + sampleBean);
    }

    @SuppressWarnings("unused")
    public void setSampleBean(MethodUtilsTestBean[] sampleBean) {
        logger.debug("specified args: " + Arrays.toString(sampleBean));
    }

    @SuppressWarnings("unused")
    public void setSampleBean(List<MethodUtilsTestBean> list) {
        logger.debug("specified args: " + list);
    }

    @SuppressWarnings("unused")
    public static class ExtendedMethodUtilsTestBean extends MethodUtilsTestBean {
    }

}
