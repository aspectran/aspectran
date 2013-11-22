/**
 * 
 */
package com.aspectran.core.var.rule.ability;

import com.aspectran.core.var.rule.BeanActionRule;
import com.aspectran.core.var.rule.EchoActionRule;

/**
 *
 * @author Gulendol
 * @since 2011. 2. 21.
 *
 */
public interface ActionSettable {

	public void setEchoAction(EchoActionRule echoActionRule);
	
	public void setBeanAction(BeanActionRule beanActionRule);
	
}
