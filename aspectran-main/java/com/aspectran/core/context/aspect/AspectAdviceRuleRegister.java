package com.aspectran.core.context.aspect;

import java.util.List;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.action.BeanAction;
import com.aspectran.core.activity.process.action.EchoAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.action.IncludeAction;
import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.aspect.pointcut.PointcutFactory;
import com.aspectran.core.context.aspect.pointcut.ReusePointcutFactory;
import com.aspectran.core.rule.AspectAdviceRule;
import com.aspectran.core.rule.AspectRule;
import com.aspectran.core.rule.AspectRuleMap;
import com.aspectran.core.rule.ItemRule;
import com.aspectran.core.rule.PointcutRule;
import com.aspectran.core.rule.RequestRule;
import com.aspectran.core.rule.ResponseRule;
import com.aspectran.core.rule.SettingsAdviceRule;
import com.aspectran.core.rule.TransletRule;
import com.aspectran.core.rule.TransletRuleMap;
import com.aspectran.core.type.JoinpointScopeType;
import com.aspectran.core.type.JoinpointTargetType;
import com.aspectran.core.type.PointcutType;

public class AspectAdviceRuleRegister {
	
	private AspectRuleMap aspectRuleMap;
	
	private PointcutFactory pointcutFactory = new ReusePointcutFactory();
	
	public AspectAdviceRuleRegister(AspectRuleMap aspectRuleMap) {
		this.aspectRuleMap = aspectRuleMap;
	}

	public void register(TransletRuleMap transletRuleMap) {
		for(AspectRule aspectRule : aspectRuleMap) {
			JoinpointTargetType  joinpointTarget = aspectRule.getJoinpointTarget();
			PointcutRule pointcutRule = aspectRule.getPointcutRule();
			PointcutType pointcutType = pointcutRule.getPointcutType();
			
			Pointcut pointcut = pointcutFactory.createPointcut(pointcutRule);
			
			
		}
	}
	
	public void register(TransletRule transletRule) {
		for(AspectRule aspectRule : aspectRuleMap) {
			JoinpointTargetType joinpointTarget = aspectRule.getJoinpointTarget();
			JoinpointScopeType joinpointScope = aspectRule.getJoinpointScope();
			PointcutRule pointcutRule = aspectRule.getPointcutRule();

			Pointcut pointcut = pointcutFactory.createPointcut(pointcutRule);
			
			if(joinpointTarget == JoinpointTargetType.TRANSLET) {
				if(joinpointScope == JoinpointScopeType.REQUEST) {
					if(pointcut.matches(transletRule.getName())) {
						register(transletRule.getRequestRule(), aspectRule);
					}
				} else if(joinpointScope == JoinpointScopeType.CONTENT) {
					ContentList contentList = transletRule.getContentList();
					
					if(contentList == null)
						contentList = new ContentList();
					
					if(pointcut.matches(transletRule.getName())) {
						register(contentList, aspectRule);
					}
				} else if(joinpointScope == JoinpointScopeType.RESPONSE) {
					if(pointcut.matches(transletRule.getName())) {
						register(transletRule.getResponseRule(), aspectRule);
					}
				} else if(joinpointScope == JoinpointScopeType.ACTION) {
					ContentList contentList = transletRule.getContentList();
					
					if(contentList != null) {
						for(ActionList actionList : contentList) {
							for(Executable action : actionList) {
								if(pointcut.matches(transletRule.getName(), action.getFullActionId())) {
									register(transletRule.getResponseRule(), aspectRule);
								}
							}
						}
					}
				} else {
					if(pointcut.matches(transletRule.getName())) {
						register(transletRule, aspectRule);
					}
				}
			} else if(joinpointTarget == JoinpointTargetType.ACTION) {
				ContentList contentList = transletRule.getContentList();

				if(contentList != null) {
					for(ActionList actionList : contentList) {
					}
						sb.append("      Content ").append(actionList).append(CRLF);
			
						for(Executable executable : actionList) {
							if(executable instanceof EchoAction) {
								EchoAction action = (EchoAction)executable;
								sb.append("         EchoAction ").append(action).append(CRLF);
								if(action.getEchoActionRule().getItemRuleMap() != null) {
									for(ItemRule pr : action.getEchoActionRule().getItemRuleMap())
										sb.append("            Echo ").append(pr).append(CRLF);
								}
							} else if(executable instanceof BeanAction) {
								BeanAction action = (BeanAction)executable;
								sb.append("         BeanAction ").append(action).append(CRLF);
								if(action.getBeanActionRule().getArgumentItemRuleMap() != null) {
									for(ItemRule ar : action.getBeanActionRule().getArgumentItemRuleMap())
										sb.append("           Argument ").append(ar).append(CRLF);
								}
								if(action.getBeanActionRule().getPropertyItemRuleMap() != null) {
									for(ItemRule pr : action.getBeanActionRule().getPropertyItemRuleMap())
										sb.append("            Property ").append(pr).append(CRLF);
								}
							} else if(executable instanceof IncludeAction) {
								IncludeAction action = (IncludeAction)executable;
								sb.append("         IncludeAction ").append(action).append(CRLF);
								if(action.getIncludeActionRule().getAttributeItemRuleMap() != null) {
									for(ItemRule at : action.getIncludeActionRule().getAttributeItemRuleMap())
										sb.append("            Attribute ").append(at).append(CRLF);
								}
							}
						}
					}
				}

				
			}
			
			
		}
		
	}
	
	public void register(TransletRule transletRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = transletRule.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			transletRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	public void register(RequestRule requestRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = requestRule.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			requestRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	public void register(ContentList contentList, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = contentList.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			contentList.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	public void register(ResponseRule responseRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = responseRule.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			responseRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	public void register(AspectAdviceRuleRegistry aspectAdviceRuleRegistry, AspectRule aspectRule) {
		SettingsAdviceRule settingsAdviceRule = aspectRule.getSettingsAdviceRule();
		List<AspectAdviceRule> aspectAdviceRuleList = aspectRule.getAspectAdviceRuleList();
		
		if(settingsAdviceRule != null)
			aspectAdviceRuleRegistry.addAspectAdviceRule(settingsAdviceRule);
		
		if(aspectAdviceRuleList != null) {
			for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
				aspectAdviceRuleRegistry.addAspectAdviceRule(aspectAdviceRule);
			}
		}
	}
	
}
