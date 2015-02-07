/**
 * 
 */
package com.aspectran.core.var.rule.ability;

import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.AbstractTransform;
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

	public AbstractTransform applyResponseRule(TransformRule transformRule);
	
	/**
	 * Sets the response rule.
	 * 
	 * @param dispatchResponseRule the drr
	 * 
	 * @return the dispatch response
	 */
	public DispatchResponse applyResponseRule(DispatchResponseRule dispatchResponseRule);
	
	/**
	 * Sets the response rule.
	 * 
	 * @param redirectResponseRule the rrr
	 * 
	 * @return the redirect response
	 */
	public RedirectResponse applyResponseRule(RedirectResponseRule redirectResponseRule);
	
	/**
	 * Sets the response rule.
	 * 
	 * @param forwardResponseRule the frr
	 * 
	 * @return the forward response
	 */
	public ForwardResponse applyResponseRule(ForwardResponseRule forwardResponseRule);
	
}
