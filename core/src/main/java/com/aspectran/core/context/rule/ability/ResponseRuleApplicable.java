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
package com.aspectran.core.context.rule.ability;

import com.aspectran.core.activity.response.Response;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.TransformRule;

/**
 * The Interface ResponseRuleApplicable.
 */
public interface ResponseRuleApplicable {

    /**
     * Apply the dispatch response rule to the response rule.
     *
     * @param dispatchResponseRule the dispatch response rule
     * @return the response
     */
    Response applyResponseRule(DispatchResponseRule dispatchResponseRule);

    /**
     * Apply the transform response rule to the response rule.
     *
     * @param transformRule the transform rule
     * @return the response
     */
    Response applyResponseRule(TransformRule transformRule);

    /**
     * Apply the forward response rule to the response rule.
     *
     * @param forwardResponseRule the forward response rule
     * @return the response
     */
    Response applyResponseRule(ForwardResponseRule forwardResponseRule);

    /**
     * Apply the redirect response rule to the response rule.
     *
     * @param redirectResponseRule the redirect response rule
     * @return the response
     */
    Response applyResponseRule(RedirectResponseRule redirectResponseRule);

}
