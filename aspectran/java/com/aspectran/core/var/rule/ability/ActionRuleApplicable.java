/**
 * 
 */
package com.aspectran.core.var.rule.ability;

import com.aspectran.core.var.rule.BeanActionRule;
import com.aspectran.core.var.rule.EchoActionRule;
import com.aspectran.core.var.rule.IncludeActionRule;

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
