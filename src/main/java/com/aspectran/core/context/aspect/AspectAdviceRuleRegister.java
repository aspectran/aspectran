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
package com.aspectran.core.context.aspect;

import java.util.List;

import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.AspectAdviceType;

/**
 * The Class AspectAdviceRuleRegister.
 */
public class AspectAdviceRuleRegister {
	
	public static void register(AspectAdviceRuleRegistry aspectAdviceRuleRegistry, AspectRule aspectRule) {
		register(aspectAdviceRuleRegistry, aspectRule, null);
	}
	
	public static void register(AspectAdviceRuleRegistry aspectAdviceRuleRegistry, AspectRule aspectRule, AspectAdviceType excludeAspectAdviceType) {
		SettingsAdviceRule settingsAdviceRule = aspectRule.getSettingsAdviceRule();
		List<AspectAdviceRule> aspectAdviceRuleList = aspectRule.getAspectAdviceRuleList();
		ExceptionRule exceptionRule = aspectRule.getExceptionRule();
		
		if(settingsAdviceRule != null) {
			aspectAdviceRuleRegistry.addAspectAdviceRule(settingsAdviceRule);
		}
		
		if(aspectAdviceRuleList != null) {
			for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
				if(excludeAspectAdviceType == null || aspectAdviceRule.getAspectAdviceType() != excludeAspectAdviceType) {
					aspectAdviceRuleRegistry.addAspectAdviceRule(aspectAdviceRule);
				}
			}
		}
		
		if(exceptionRule != null) {
			aspectAdviceRuleRegistry.addExceptionRule(exceptionRule);
		}
		
		aspectAdviceRuleRegistry.increaseAspectRuleCount();
	}

	public static void register(TransletRule transletRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = transletRule.getAspectAdviceRuleRegistry();

		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			transletRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}

		register(aspectAdviceRuleRegistry, aspectRule);
	}

	public static void register(RequestRule requestRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = requestRule.getAspectAdviceRuleRegistry();

		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			requestRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}

		register(aspectAdviceRuleRegistry, aspectRule);
	}

	public static void register(ContentList contentList, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = contentList.getAspectAdviceRuleRegistry();

		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			contentList.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}

		if(aspectRule != null) {
			register(aspectAdviceRuleRegistry, aspectRule);
		}
	}

	public static void register(ResponseRule responseRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = responseRule.getAspectAdviceRuleRegistry();

		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			responseRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}

		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
}
