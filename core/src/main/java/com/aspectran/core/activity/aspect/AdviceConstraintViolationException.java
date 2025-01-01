/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.core.activity.ActivityException;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.utils.StringUtils;

import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Checked exception thrown when Advice Constraint Violation occurs.
 */
public class AdviceConstraintViolationException extends ActivityException {

    @Serial
    private static final long serialVersionUID = -5175491727350661063L;

    private final Set<AspectRule> relevantAspectRules = new LinkedHashSet<>();

    private final List<String> violations = new ArrayList<>();

    public AdviceConstraintViolationException() {
        super("Advice constraint violation has occurred");
    }

    public String addViolation(AspectRule aspectRule, String msg) {
        if (!relevantAspectRules.contains(aspectRule)) {
            relevantAspectRules.add(aspectRule);
            msg = msg + "; Please check AspectRule " + aspectRule;
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
        return StringUtils.toLineDelimitedString(violations);
    }

}
