/**
 * 
 */
package com.aspectran.core.rule.ability;

import com.aspectran.core.rule.BeanActionRule;
import com.aspectran.core.rule.EchoActionRule;
import com.aspectran.core.rule.IncludeActionRule;

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
