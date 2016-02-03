package com.aspectran.core.context.bean;

import java.lang.reflect.Method;

import com.aspectran.core.context.bean.annotation.Dispatch;
import com.aspectran.core.context.bean.annotation.Forward;
import com.aspectran.core.context.bean.annotation.Redirect;
import com.aspectran.core.context.bean.annotation.Transform;
import com.aspectran.core.context.bean.annotation.Translet;
import com.aspectran.core.context.bean.annotation.Translets;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.TransletRuleMap;
import com.aspectran.core.util.StringUtils;

/**
 * The Class BeanAnnotationParser.
 */
public abstract class BeanAnnotationParser {

	public static void parse(BeanRule beanRule) {
		Class<?> targetClass = beanRule.getBeanClass();
		Translets transletsAnno = targetClass.getAnnotation(Translets.class);
		String transletNamePrefix = StringUtils.emptyToNull(transletsAnno.name());
		
		TransletRuleMap transletRuleMap = new TransletRuleMap();
		
		for(Method method : targetClass.getMethods()) {
			if(method.isAnnotationPresent(Translet.class)) {
				Translet transletAnno = method.getAnnotation(Translet.class);
				String transletName = StringUtils.emptyToNull(transletAnno.name());
				String restVerb = StringUtils.emptyToNull(transletAnno.restVerb());
				
				if(transletNamePrefix != null && transletName != null)
					transletName = transletNamePrefix + transletName;
				
				TransletRule transletRule = TransletRule.newInstance(transletName, restVerb);
				
				if(method.isAnnotationPresent(Dispatch.class)) {
					Dispatch dispatchAnno = method.getAnnotation(Dispatch.class);
					String dispatchName = StringUtils.emptyToNull(dispatchAnno.name());
					String characterEncoding = StringUtils.emptyToNull(dispatchAnno.characterEncoding());
					DispatchResponseRule drr = DispatchResponseRule.newInstance(dispatchName, characterEncoding);
					transletRule.setResponseRule(ResponseRule.newInstance(drr));
				} else if(method.isAnnotationPresent(Transform.class)) {
					Transform transformAnno = method.getAnnotation(Transform.class);
					String transformType = StringUtils.emptyToNull(transformAnno.transformType());
					String contentType = StringUtils.emptyToNull(transformAnno.contentType());
					String templateId = StringUtils.emptyToNull(transformAnno.templateId());
					String characterEncoding = StringUtils.emptyToNull(transformAnno.characterEncoding());
					boolean pretty = transformAnno.pretty();
					TransformRule tr = TransformRule.newInstance(transformType, contentType, templateId, characterEncoding, null, pretty);
					transletRule.setResponseRule(ResponseRule.newInstance(tr));
				} else if(method.isAnnotationPresent(Forward.class)) {
					Forward forwardAnno = method.getAnnotation(Forward.class);
					String translet = StringUtils.emptyToNull(forwardAnno.translet());
					ForwardResponseRule frr = ForwardResponseRule.newInstance(translet);
					transletRule.setResponseRule(ResponseRule.newInstance(frr));
				} else if(method.isAnnotationPresent(Redirect.class)) {
					Redirect redirectAnno = method.getAnnotation(Redirect.class);
					String target = StringUtils.emptyToNull(redirectAnno.target());
					RedirectResponseRule rrr = RedirectResponseRule.newInstance(target);
					transletRule.setResponseRule(ResponseRule.newInstance(rrr));
				}
				
				BeanActionRule beanActionRule = new BeanActionRule();
				beanActionRule.setBeanId(beanRule.getId());
				beanActionRule.setMethodName(method.getName());
				transletRule.applyActionRule(beanActionRule);
				
				transletRuleMap.putTransletRule(transletRule);
			}
		}
	}
	
}
