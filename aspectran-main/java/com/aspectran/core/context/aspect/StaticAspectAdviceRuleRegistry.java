package com.aspectran.core.context.aspect;

import java.util.HashMap;
import java.util.Map;


public class StaticAspectAdviceRuleRegistry extends AspectAdviceRuleRegistry {
	
	private Map<String, AspectAdviceRuleRegistry> aspectAdviceRuleRegistryCache = new HashMap<String, AspectAdviceRuleRegistry>();

	
}
