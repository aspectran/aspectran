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

	public void applyEchoActionRule(EchoActionRule echoActionRule);
	
	public void applyBeanActionRule(BeanActionRule beanActionRule);
	
	public void applyIncludeActionRule(IncludeActionRule includeActionRule);

}
