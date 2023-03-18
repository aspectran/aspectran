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
package com.aspectran.core.context.rule.ability;

import com.aspectran.core.activity.response.Response;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.core.context.rule.ForwardRule;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.TransformRule;

/**
 * The Interface ResponseRuleApplicable.
 */
public interface ResponseRuleApplicable {

    /**
     * Apply the transform rule to the response rule.
     * @param transformRule the transform rule
     * @return the response
     */
    Response applyResponseRule(TransformRule transformRule);

    /**
     * Apply the dispatch rule to the response rule.
     * @param dispatchRule the dispatch rule
     * @return the response
     */
    Response applyResponseRule(DispatchRule dispatchRule);

    /**
     * Apply the forward rule to the response rule.
     * @param forwardRule the forward rule
     * @return the response
     */
    Response applyResponseRule(ForwardRule forwardRule);

    /**
     * Apply the redirect rule to the response rule.
     * @param redirectRule the redirect rule
     * @return the response
     */
    Response applyResponseRule(RedirectRule redirectRule);

}
