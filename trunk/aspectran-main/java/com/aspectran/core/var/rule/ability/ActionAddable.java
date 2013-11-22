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
public interface ActionAddable {

	public void addEchoAction(EchoActionRule echoActionRule);

	public void addBeanAction(BeanActionRule beanActionRule) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, NoSuchMethodException;
	
	public void addIncludeAction(IncludeActionRule includeActionRule);

}
