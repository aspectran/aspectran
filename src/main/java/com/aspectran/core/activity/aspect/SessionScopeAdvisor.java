/**
 * Copyright 2008-2016 Juho Jeong
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

import java.util.List;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.SessionScopeActivity;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.aspect.AspectRuleRegistry;
import com.aspectran.core.context.rule.AspectAdviceRule;

/**
 * The Class SessionScopeAdvisor.
 */
public class SessionScopeAdvisor {

	private final CoreActivity activity;
	
	private final List<AspectAdviceRule> beforeAdviceRuleList;
	
	private final List<AspectAdviceRule> afterAdviceRuleList;
	
	private SessionScopeAdvisor(SessionScopeActivity activity, AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
		this.activity = activity;
		this.beforeAdviceRuleList = aspectAdviceRuleRegistry.getBeforeAdviceRuleList();
		this.afterAdviceRuleList = aspectAdviceRuleRegistry.getAfterAdviceRuleList();
	}
	
	public void executeBeforeAdvice() {
		if(beforeAdviceRuleList != null) {
			activity.execute(beforeAdviceRuleList);
		}
	}
	
	public void executeAfterAdvice() {
		if(afterAdviceRuleList != null) {
			activity.executeWithoutThrow(afterAdviceRuleList);
		}
	}
	
	public static SessionScopeAdvisor newInstance(ActivityContext context, SessionAdapter sessionAdapter) {
		AspectRuleRegistry aspectRuleRegistry = context.getAspectRuleRegistry();
		AspectAdviceRuleRegistry aarr = aspectRuleRegistry.getSessionAspectAdviceRuleRegistry();
		if(aarr == null)
			return null;

		SessionScopeActivity activity = new SessionScopeActivity(context, sessionAdapter);
		return new SessionScopeAdvisor(activity, aarr);
	}
	
}
