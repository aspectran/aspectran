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
package com.aspectran.core.activity.aspect;

import com.aspectran.core.component.aspect.AspectException;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Exception thrown when an Advice Constraint Violation occurs.
 */
public class AdviceConstraintViolationException extends AspectException {

    /** @serial */
    private static final long serialVersionUID = -5175491727350661063L;

    private Set<AspectRule> relevantAspectRules = new LinkedHashSet<>();

    private List<String> violations = new ArrayList<>();

    public AdviceConstraintViolationException() {
        super("Advice constraint violation has occurred");
    }

    public String addViolation(AspectRule aspectRule, String msg) {
        if (!relevantAspectRules.contains(aspectRule)) {
            relevantAspectRules.add(aspectRule);
            msg = msg + "; Please check the Aspect Rule " + aspectRule;
            violations.add(msg);
            return msg;
        } else {
            return null;
        }
    }

    public Set<AspectRule> getRelevantAspectRules() {
        return relevantAspectRules;
    }

    @Override
    public String getMessage() {
        return StringUtils.toDelimitedString(violations, ActivityContext.LINE_SEPARATOR);
    }

}
