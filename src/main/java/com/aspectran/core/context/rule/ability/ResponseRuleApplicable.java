/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
 *
 * @author Juho Jeong
 * @since 2011. 2. 21.
 *
 */
public interface ResponseRuleApplicable {

	public Response applyResponseRule(TransformRule transformRule);
	
	/**
	 * Sets the response rule.
	 * 
	 * @param dispatchResponseRule the drr
	 * 
	 * @return the dispatch response
	 */
	public Response applyResponseRule(DispatchResponseRule dispatchResponseRule);
	
	/**
	 * Sets the response rule.
	 * 
	 * @param redirectResponseRule the rrr
	 * 
	 * @return the redirect response
	 */
	public Response applyResponseRule(RedirectResponseRule redirectResponseRule);
	
	/**
	 * Sets the response rule.
	 * 
	 * @param forwardResponseRule the frr
	 * 
	 * @return the forward response
	 */
	public Response applyResponseRule(ForwardResponseRule forwardResponseRule);
	
}
