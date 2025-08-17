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
package com.aspectran.core.activity.aspect;

import com.aspectran.core.context.rule.AdviceRule;
import com.aspectran.core.context.rule.type.AdviceType;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the result data for advice execution in Aspectran.
 * This class holds the results from different types of advice (before, after, around, finally)
 * and also maintains references to the advice bean objects.
 *
 * @since 2.0.0
 */
public class AdviceResult {

    private Map<String, Object> adviceBeanMap;

    private Map<String, Object> beforeAdviceResultMap;

    private Map<String, Object> afterAdviceResultMap;

    private Map<String, Object> aroundAdviceResultMap;

    private Map<String, Object> finallyAdviceResultMap;

    public Object getAdviceBean(String aspectId) {
        return (adviceBeanMap != null ? adviceBeanMap.get(aspectId) : null);
    }

    public void putAdviceBean(String aspectId, Object adviceBean) {
        if (adviceBeanMap == null) {
            adviceBeanMap = new HashMap<>();
        }
        adviceBeanMap.put(aspectId, adviceBean);
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

    public void putAdviceResult(@NonNull AdviceRule adviceRule, Object adviceResult) {
        if (adviceRule.getAdviceType() == AdviceType.BEFORE) {
            putBeforeAdviceResult(adviceRule.getAspectId(), adviceResult);
        } else if (adviceRule.getAdviceType() == AdviceType.AFTER) {
            putAfterAdviceResult(adviceRule.getAspectId(), adviceResult);
        } else if (adviceRule.getAdviceType() == AdviceType.AROUND) {
            putAroundAdviceResult(adviceRule.getAspectId(), adviceResult);
        } else if (adviceRule.getAdviceType() == AdviceType.FINALLY) {
            putFinallyAdviceResult(adviceRule.getAspectId(), adviceResult);
        } else {
            throw new UnsupportedOperationException("Unrecognized advice type: " +
                    adviceRule.getAdviceType());
        }
    }

}
