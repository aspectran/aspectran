/**
 * 
 */
package com.aspectran.core.var.rule.ability;

import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.var.rule.DispatchResponseRule;
import com.aspectran.core.var.rule.ForwardResponseRule;
import com.aspectran.core.var.rule.RedirectResponseRule;
import com.aspectran.core.var.rule.TransformRule;

/**
 *
 * @author Gulendol
 * @since 2011. 2. 21.
 *
 */
public interface ResponseRuleApplicable {

	public Responsible applyResponseRule(TransformRule transformRule);
	
	/**
	 * Sets the response rule.
	 * 
	 * @param dispatchResponseRule the drr
	 * 
	 * @return the dispatch response
	 */
	public Responsible applyResponseRule(DispatchResponseRule dispatchResponseRule);
	
	/**
	 * Sets the response rule.
	 * 
	 * @param redirectResponseRule the rrr
	 * 
	 * @return the redirect response
	 */
	public Responsible applyResponseRule(RedirectResponseRule redirectResponseRule);
	
	/**
	 * Sets the response rule.
	 * 
	 * @param forwardResponseRule the frr
	 * 
	 * @return the forward response
	 */
	public Responsible applyResponseRule(ForwardResponseRule forwardResponseRule);
	
}
