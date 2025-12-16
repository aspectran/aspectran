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
import org.jspecify.annotations.NonNull;

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

    /**
     * Maps an aspect ID to its corresponding advice bean instance.
     */
    private Map<String, Object> adviceBeanMap;

    /**
     * Stores the result of BEFORE advice executions keyed by aspect ID.
     */
    private Map<String, Object> beforeAdviceResultMap;

    /**
     * Stores the result of AFTER advice executions keyed by aspect ID.
     */
    private Map<String, Object> afterAdviceResultMap;

    /**
     * Stores the result of AROUND advice executions keyed by aspect ID.
     */
    private Map<String, Object> aroundAdviceResultMap;

    /**
     * Stores the result of FINALLY advice executions keyed by aspect ID.
     */
    private Map<String, Object> finallyAdviceResultMap;

    /**
     * Retrieves the advice bean instance associated with the given aspect ID.
     * @param aspectId the unique identifier of the aspect
     * @return the advice bean object, or {@code null} if none is registered
     */
    public Object getAdviceBean(String aspectId) {
        return (adviceBeanMap != null ? adviceBeanMap.get(aspectId) : null);
    }

    /**
     * Stores the advice bean instance associated with the given aspect ID.
     * @param aspectId the unique identifier of the aspect
     * @param adviceBean the advice bean object
     */
    public void putAdviceBean(String aspectId, Object adviceBean) {
        if (adviceBeanMap == null) {
            adviceBeanMap = new HashMap<>();
        }
        adviceBeanMap.put(aspectId, adviceBean);
    }

    /**
     * Retrieves the result of a BEFORE advice execution for the specified aspect.
     * @param aspectId the unique identifier of the aspect
     * @return the result produced by the BEFORE advice, or {@code null} if not found
     */
    public Object getBeforeAdviceResult(String aspectId) {
        return (beforeAdviceResultMap != null ? beforeAdviceResultMap.get(aspectId) : null);
    }

    /**
     * Stores the result of a BEFORE advice execution for the specified aspect.
     * @param aspectId the unique identifier of the aspect
     * @param actionResult the result produced by the BEFORE advice
     */
    private void putBeforeAdviceResult(String aspectId, Object actionResult) {
        if (beforeAdviceResultMap == null) {
            beforeAdviceResultMap = new HashMap<>();
        }
        beforeAdviceResultMap.put(aspectId, actionResult);
    }

    public Object getAfterAdviceResult(String aspectId) {
        return (afterAdviceResultMap != null ? afterAdviceResultMap.get(aspectId) : null);
    }

    /**
     * Stores the result of an AFTER advice execution for the specified aspect.
     * @param aspectId the unique identifier of the aspect
     * @param actionResult the result produced by the AFTER advice
     */
    private void putAfterAdviceResult(String aspectId, Object actionResult) {
        if (afterAdviceResultMap == null) {
            afterAdviceResultMap = new HashMap<>();
        }
        afterAdviceResultMap.put(aspectId, actionResult);
    }

    public Object getAroundAdviceResult(String aspectId) {
        return (aroundAdviceResultMap != null ? aroundAdviceResultMap.get(aspectId) : null);
    }

    /**
     * Stores the result of an AROUND advice execution for the specified aspect.
     * @param aspectId the unique identifier of the aspect
     * @param actionResult the result produced by the AROUND advice
     */
    private void putAroundAdviceResult(String aspectId, Object actionResult) {
        if (aroundAdviceResultMap == null) {
            aroundAdviceResultMap = new HashMap<>();
        }
        aroundAdviceResultMap.put(aspectId, actionResult);
    }

    public Object getFinallyAdviceResult(String aspectId) {
        return (finallyAdviceResultMap != null ? finallyAdviceResultMap.get(aspectId) : null);
    }

    /**
     * Stores the result of a FINALLY advice execution for the specified aspect.
     * @param aspectId the unique identifier of the aspect
     * @param actionResult the result produced by the FINALLY advice
     */
    private void putFinallyAdviceResult(String aspectId, Object actionResult) {
        if (finallyAdviceResultMap == null) {
            finallyAdviceResultMap = new HashMap<>();
        }
        finallyAdviceResultMap.put(aspectId, actionResult);
    }

    /**
     * Stores the result of an advice execution based on its {@link AdviceType}.
     * @param adviceRule the rule describing the advice, including its type and aspect ID
     * @param adviceResult the result produced by the advice
     * @throws UnsupportedOperationException if the advice type is not supported
     */
    public void putAdviceResult(@NonNull AdviceRule adviceRule, Object adviceResult) {
        switch (adviceRule.getAdviceType()) {
            case BEFORE -> putBeforeAdviceResult(adviceRule.getAspectId(), adviceResult);
            case AFTER -> putAfterAdviceResult(adviceRule.getAspectId(), adviceResult);
            case AROUND -> putAroundAdviceResult(adviceRule.getAspectId(), adviceResult);
            case FINALLY -> putFinallyAdviceResult(adviceRule.getAspectId(), adviceResult);
            case null, default -> throw new UnsupportedOperationException("Unrecognized advice type: " +
                    adviceRule.getAdviceType());
        }
    }

}
