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
package com.aspectran.core.activity.aspect.result;

import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.type.AspectAdviceType;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the result data for the Aspect Advices.
 * Also, It contains the bean objects associated with the Aspect Advice.
 */
public class AspectAdviceResult {

    private Map<String, Object> aspectAdviceBeanMap;

    private Map<String, Object> beforeAdviceResultMap;

    private Map<String, Object> afterAdviceResultMap;

    private Map<String, Object> aroundAdviceResultMap;

    private Map<String, Object> finallyAdviceResultMap;

    public Object getAspectAdviceBean(String aspectId) {
        return (aspectAdviceBeanMap != null ? aspectAdviceBeanMap.get(aspectId) : null);
    }

    public void putAspectAdviceBean(String aspectId, Object adviceBean) {
        if (aspectAdviceBeanMap == null) {
            aspectAdviceBeanMap = new HashMap<>();
        }
        aspectAdviceBeanMap.put(aspectId, adviceBean);
    }

    public Object getBeforeAdviceResult(String aspectId) {
        return (beforeAdviceResultMap != null ? beforeAdviceResultMap.get(aspectId) : null);
    }

    private void putBeforeAdviceResult(String aspectId, Object actionResult) {
        if (beforeAdviceResultMap == null) {
            beforeAdviceResultMap = new HashMap<>();
        }
        beforeAdviceResultMap.put(aspectId, actionResult);
    }

    public Object getAfterAdviceResult(String aspectId) {
        return (afterAdviceResultMap != null ? afterAdviceResultMap.get(aspectId) : null);
    }

    private void putAfterAdviceResult(String aspectId, Object actionResult) {
        if (afterAdviceResultMap == null) {
            afterAdviceResultMap = new HashMap<>();
        }
        afterAdviceResultMap.put(aspectId, actionResult);
    }

    public Object getAroundAdviceResult(String aspectId) {
        return (aroundAdviceResultMap != null ? aroundAdviceResultMap.get(aspectId) : null);
    }

    private void putAroundAdviceResult(String aspectId, Object actionResult) {
        if (aroundAdviceResultMap == null) {
            aroundAdviceResultMap = new HashMap<>();
        }
        aroundAdviceResultMap.put(aspectId, actionResult);
    }

    public Object getFinallyAdviceResult(String aspectId) {
        return (finallyAdviceResultMap != null ? finallyAdviceResultMap.get(aspectId) : null);
    }

    private void putFinallyAdviceResult(String aspectId, Object actionResult) {
        if (finallyAdviceResultMap == null) {
            finallyAdviceResultMap = new HashMap<>();
        }
        finallyAdviceResultMap.put(aspectId, actionResult);
    }

    public void putAdviceResult(AspectAdviceRule aspectAdviceRule, Object adviceActionResult) {
        if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.BEFORE) {
            putBeforeAdviceResult(aspectAdviceRule.getAspectId(), adviceActionResult);
        } else if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AFTER) {
            putAfterAdviceResult(aspectAdviceRule.getAspectId(), adviceActionResult);
        } else if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AROUND) {
            putAroundAdviceResult(aspectAdviceRule.getAspectId(), adviceActionResult);
        } else if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.FINALLY) {
            putFinallyAdviceResult(aspectAdviceRule.getAspectId(), adviceActionResult);
        } else {
            throw new UnsupportedOperationException("Unrecognized aspect advice type: " + aspectAdviceRule.getAspectAdviceType());
        }
    }

}
