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
package com.aspectran.core.context.rule.ability;

import com.aspectran.core.activity.response.Response;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.core.context.rule.ForwardRule;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.TransformRule;

/**
 * Defines a contract for rule classes that can contain and manage response rules.
 * This interface provides a polymorphic way to add different types of response rules
 * (e.g., transform, dispatch) to a parent rule, determining how the final output is generated.
 */
public interface HasResponseRules {

    /**
     * Applies a transform rule, creating a response that will transform the content
     * (e.g., to JSON or XML).
     * @param transformRule the transform rule to apply
     * @return the created {@link Response} instance
     */
    Response putResponseRule(TransformRule transformRule);

    /**
     * Applies a dispatch rule, creating a response that will dispatch the request
     * to a view technology (e.g., JSP).
     * @param dispatchRule the dispatch rule to apply
     * @return the created {@link Response} instance
     */
    Response putResponseRule(DispatchRule dispatchRule);

    /**
     * Applies a forward rule, creating a response that will perform a server-side
     * forward to another resource.
     * @param forwardRule the forward rule to apply
     * @return the created {@link Response} instance
     */
    Response putResponseRule(ForwardRule forwardRule);

    /**
     * Applies a redirect rule, creating a response that will send a client-side
     * redirect to a new URL.
     * @param redirectRule the redirect rule to apply
     * @return the created {@link Response} instance
     */
    Response putResponseRule(RedirectRule redirectRule);

}
