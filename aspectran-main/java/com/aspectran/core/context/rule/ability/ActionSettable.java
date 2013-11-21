/**
 * 
 */
package com.aspectran.core.context.rule.ability;

import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.EchoActionRule;

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
