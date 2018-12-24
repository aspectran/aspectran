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
package com.aspectran.core.util;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.Arrays;
import java.util.List;

public class MethodUtilsTestBean {

    private static final Log log = LogFactory.getLog(MethodUtilsTestBean.class);

    @SuppressWarnings("unused")
    public void primitiveArray(int num) {
        log.debug("specified args: " + num);
    }

    @SuppressWarnings("unused")
    public void primitiveArray(int[] intArray) {
        log.debug("specified args: " + Arrays.toString(intArray));
    }

    @SuppressWarnings("unused")
    public String countTo10(Translet translet) {
        StringBuilder sb = new StringBuilder();

        for (int i = 1; i <= 10; i++) {
            sb.append(i).append("\n");
        }

        return sb.toString();
    }

    @SuppressWarnings("unused")
    public void setSampleBean(MethodUtilsTestBean sampleBean) {
        log.debug("specified args: " + sampleBean);
    }

    @SuppressWarnings("unused")
    public void setSampleBean(MethodUtilsTestBean[] sampleBean) {
        log.debug("specified args: " + Arrays.toString(sampleBean));
    }

    @SuppressWarnings("unused")
    public void setSampleBean(List<MethodUtilsTestBean> list) {
        log.debug("specified args: " + list);
    }

    @SuppressWarnings("unused")
    public class ExtendedMethodUtilsTestBean extends MethodUtilsTestBean {
    }

}
