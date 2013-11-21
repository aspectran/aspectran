/**
 * 
 */
package com.aspectran.core.context.rule.ability;

import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.AbstractTransform;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.TransformRule;

/**
 *
 * @author Gulendol
 * @since 2011. 2. 21.
 *
 */
public interface ResponseAddable {

	public AbstractTransform addResponse(TransformRule tr);
	
	/**
	 * Adds the response rule.
	 * 
	 * @param drr the drr
	 * 
	 * @return the dispatch response
	 */
	public DispatchResponse addResponse(DispatchResponseRule drr);
	
	/**
	 * Adds the response rule.
	 * 
	 * @param rrr the rrr
	 * 
	 * @return the redirect response
	 */
	public RedirectResponse addResponse(RedirectResponseRule rrr);
	
	/**
	 * Adds the response rule.
	 * 
	 * @param frr the frr
	 * 
	 * @return the forward response
	 */
	public ForwardResponse addResponse(ForwardResponseRule frr);
	
}
