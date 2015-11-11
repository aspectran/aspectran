/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.activity.aspect;

import com.aspectran.core.activity.SessionScopeActivity;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.aspect.AspectRuleRegistry;

/**
 * The Class SessionScope.
 */
public class SessionScopeAdvisor {

	private SessionScopeActivity activity;
	
	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;
	
	public SessionScopeAdvisor(SessionScopeActivity activity, AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
		this.activity = activity;
		this.aspectAdviceRuleRegistry = aspectAdviceRuleRegistry;
	}
	
	public void executeBeforeAdvice() {
		if(activity != null) {
			if(aspectAdviceRuleRegistry.getBeforeAdviceRuleList() != null)
				activity.execute(aspectAdviceRuleRegistry.getBeforeAdviceRuleList());
		}
	}
	
	public void executeAfterAdvice() {
		if(activity != null) {
			if(aspectAdviceRuleRegistry.getAfterAdviceRuleList() != null)
				activity.forceExecute(aspectAdviceRuleRegistry.getAfterAdviceRuleList());
		}
		
	}
	
	public static SessionScopeAdvisor newInstance(ActivityContext context, SessionAdapter sessionAdapter) {
		AspectRuleRegistry aspectRuleRegistry = context.getAspectRuleRegistry();
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = aspectRuleRegistry.getSessionAspectAdviceRuleRegistry();
		
		SessionScopeAdvisor advisor = null;
		
		if(aspectAdviceRuleRegistry != null) {
			SessionScopeActivity activity = new SessionScopeActivity(context, sessionAdapter);
			advisor = new SessionScopeAdvisor(activity, aspectAdviceRuleRegistry);
		}
		
		return advisor;
	}
	
}
