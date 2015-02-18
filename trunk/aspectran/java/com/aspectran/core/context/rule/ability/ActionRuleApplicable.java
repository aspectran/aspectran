/**
 * 
 */
package com.aspectran.core.context.rule.ability;

import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.IncludeActionRule;

/**
 *
 * @author Gulendol
 * @since 2011. 2. 21.
 *
 */
public interface ActionRuleApplicable {

	public void applyActionRule(EchoActionRule echoActionRule);
	
	public void applyActionRule(BeanActionRule beanActionRule);
	
	public void applyActionRule(IncludeActionRule includeActionRule);

}
