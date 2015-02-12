/**
 * 
 */
package com.aspectran.core.activity.response;

import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.JsonTransform;
import com.aspectran.core.activity.response.transform.TextTransform;
import com.aspectran.core.activity.response.transform.XmlTransform;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.type.TransformType;

/**
 *
 * @author Gulendol
 * @since 2011. 3. 12.
 *
 */
public class TransformResponseFactory {

	public static Responsible getResponse(TransformRule transformRule) {
		TransformType type = transformRule.getTransformType();
		Responsible res = null;
		
		if(type == TransformType.XML_TRANSFORM) {
			res = new XmlTransform(transformRule);
		} else if(type == TransformType.XSL_TRANSFORM) {
			res = new XmlTransform(transformRule);
		} else if(type == TransformType.JSON_TRANSFORM) {
			res = new JsonTransform(transformRule);
		} else if(type == TransformType.TEXT_TRANSFORM) {
			res = new TextTransform(transformRule);
		}
		
		return res;
	}
	
	public static Responsible getResponse(ForwardResponseRule forwardResponseRule) {
		return new ForwardResponse(forwardResponseRule);
	}
	
	public static Responsible getResponse(RedirectResponseRule redirectResponseRule) {
		return new RedirectResponse(redirectResponseRule);
	}
	
	public static Responsible getResponse(DispatchResponseRule dispatchResponseRule) {
		return new DispatchResponse(dispatchResponseRule);
	}
}
