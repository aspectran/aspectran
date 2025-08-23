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

import com.aspectran.core.activity.ActivityException;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.utils.StringUtils;

import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Checked exception thrown when an Advice Constraint Violation occurs.
 * <p>
 * This exception is raised by the {@code Activity} whenever a
 * configured {@link AspectRule} fails to meet its
 * constraints during execution. The exception keeps track of all relevant
 * aspect rules and human-readable violation messages so that callers can
 * diagnose the problem.</p>
 */
public class AdviceConstraintViolationException extends ActivityException {

    @Serial
    private static final long serialVersionUID = -5175491727350661063L;

    private final Set<AspectRule> relevantAspectRules = new LinkedHashSet<>();

    private final List<String> violations = new ArrayList<>();

    /**
     * Creates a new {@code AdviceConstraintViolationException} with the default message.
     * <p>The default message is {@code "Advice constraint violation has occurred"}.</p>
     */
    public AdviceConstraintViolationException() {
        super("Advice constraint violation has occurred");
    }

    /**
     * Records a new violation message for the specified {@link AspectRule}.
     * If this rule has not already been recorded, it is added to the set of
     * relevant rules and the supplied message is appended with a reference to
     * the rule. The formatted message is stored in the internal list and returned.
     * If the rule was already present, {@code null} is returned and no changes are made.
     * @param aspectRule the aspect rule that caused the violation
     * @param msg the initial violation message
     * @return the formatted message including a reference to the rule,
     * or {@code null} if the rule was already recorded
     */
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

    /**
     * Returns an unmodifiable view of the aspect rules that contributed to this violation.
     * @return a {@link java.util.Set} containing all {@link AspectRule}
     * instances that were recorded during construction of this exception
     */
    public Set<AspectRule> getRelevantAspectRules() {
        return relevantAspectRules;
    }

    /**
     * The message is a concatenation of all recorded violation messages,
     * each separated by a newline. This provides a human-readable summary
     * of every rule that caused the exception.
     */
    @Override
    public String getMessage() {
        return StringUtils.joinWithLines(violations);
    }

}
