package com.aspectran.core.context.aspect;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.action.BeanAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.aspect.pointcut.PointcutFactory;
import com.aspectran.core.context.aspect.pointcut.ReusePointcutFactory;
import com.aspectran.core.rule.AspectAdviceRule;
import com.aspectran.core.rule.AspectRule;
import com.aspectran.core.rule.AspectRuleMap;
import com.aspectran.core.rule.BeanActionRule;
import com.aspectran.core.rule.PointcutRule;
import com.aspectran.core.rule.RequestRule;
import com.aspectran.core.rule.ResponseByContentTypeRule;
import com.aspectran.core.rule.ResponseByContentTypeRuleMap;
import com.aspectran.core.rule.ResponseRule;
import com.aspectran.core.rule.SettingsAdviceRule;
import com.aspectran.core.rule.TransletRule;
import com.aspectran.core.rule.TransletRuleMap;
import com.aspectran.core.type.ActionType;
import com.aspectran.core.type.JoinpointScopeType;
import com.aspectran.core.type.JoinpointTargetType;

public class AspectAdviceRuleRegister {
	
	private final Log log = LogFactory.getLog(AspectAdviceRuleRegister.class);
	
	private AspectRuleMap aspectRuleMap;
	
	private PointcutFactory pointcutFactory = new ReusePointcutFactory();
	
	public AspectAdviceRuleRegister(AspectRuleMap aspectRuleMap) {
		this.aspectRuleMap = aspectRuleMap;
	}

	public void register(TransletRuleMap transletRuleMap) {
		for(TransletRule transletRule : transletRuleMap) {
			register(transletRule);
		}
	}
	
	private void register(TransletRule transletRule) {
		for(AspectRule aspectRule : aspectRuleMap) {
			JoinpointTargetType joinpointTarget = aspectRule.getJoinpointTarget();
			JoinpointScopeType joinpointScope = aspectRule.getJoinpointScope();
			PointcutRule pointcutRule = aspectRule.getPointcutRule();

			Pointcut pointcut = null;
			if(pointcutRule != null)
				pointcut = pointcutFactory.createPointcut(pointcutRule);
			
			if(joinpointTarget == JoinpointTargetType.TRANSLET) {
				if(joinpointScope == JoinpointScopeType.REQUEST) {
					if(pointcut == null || pointcut.matches(transletRule.getName())) {
						RequestRule requestRule = transletRule.getRequestRule();
						log.debug("aspectRule " + aspectRule + " transletRule " + transletRule + " requestRule " + requestRule);
						register(transletRule.getRequestRule(), aspectRule);
						transletRule.setAspectAdviceRuleExists(true);
					}
				} else if(joinpointScope == JoinpointScopeType.CONTENT) {
					if(pointcut == null || pointcut.matches(transletRule.getName())) {
						ContentList contentList = transletRule.getContentList();
						
						if(contentList == null) {
							contentList = new ContentList();
							transletRule.setContentList(contentList);
						}

						log.debug("aspectRule " + aspectRule + " transletRule " + transletRule + " contentList " + contentList);
						register(contentList, aspectRule);
						transletRule.setAspectAdviceRuleExists(true);
					}
				} else if(joinpointScope == JoinpointScopeType.ACTION_CONTENT) {
					if(pointcut != null && pointcut.isActionInfluenced()) {
						ContentList contentList = transletRule.getContentList();
						
						if(contentList != null) {
							boolean actionExists = false;
							
							for(ActionList actionList : contentList) {
								for(Executable action : actionList) {
									if(pointcut.matches(transletRule.getName(), action.getFullActionId())) {
										actionExists = true;
										break;
									}
								}
							}
							
							if(actionExists) {
								log.debug("aspectRule " + aspectRule + " transletRule " + transletRule + " contentList " + contentList);
								register(contentList, aspectRule);
								transletRule.setAspectAdviceRuleExists(true);
							}
						}
					}
				} else if(joinpointScope == JoinpointScopeType.BEAN_CONTENT) {
					if(pointcut != null && pointcut.isActionInfluenced()) {
						ContentList contentList = transletRule.getContentList();
						
						if(contentList != null) {
							boolean actionExists = false;
							
							for(ActionList actionList : contentList) {
								for(Executable action : actionList) {
									if(action.getActionType() == ActionType.BEAN) {
										BeanActionRule beanActionRule = ((BeanAction)action).getBeanActionRule();
										
										if(pointcut.matches(transletRule.getName(), beanActionRule.getBeanId(), beanActionRule.getMethodName())) {
											actionExists = true;
											break;
										}
									}
								}
							}
							
							if(actionExists) {
								log.debug("aspectRule " + aspectRule + " transletRule " + transletRule + " contentList " + contentList);
								register(contentList, aspectRule);
								transletRule.setAspectAdviceRuleExists(true);
							}
						}
					}
				} else if(joinpointScope == JoinpointScopeType.RESPONSE) {
					if(pointcut == null || pointcut.matches(transletRule.getName())) {
						ResponseRule responseRule = transletRule.getResponseRule();
						log.debug("aspectRule " + aspectRule + " transletRule " + transletRule + " responseRule " + responseRule);
						register(transletRule.getResponseRule(), aspectRule);
						transletRule.setAspectAdviceRuleExists(true);
					}
				} else if(joinpointScope == JoinpointScopeType.ACTION) {
					ContentList contentList = transletRule.getContentList();
					
					if(contentList != null) {
						for(ActionList actionList : contentList) {
							for(Executable action : actionList) {
								if(pointcut == null || pointcut.matches(transletRule.getName(), action.getFullActionId())) {
									log.debug("aspectRule " + aspectRule + " transletRule " + transletRule + " action " + action);
									register(action, aspectRule);
									transletRule.setAspectAdviceRuleExists(true);
								}
							}
						}
					}
					
					ResponseByContentTypeRuleMap exceptionHandlingRuleMap = transletRule.getExceptionHandlingRuleMap();
					
					if(exceptionHandlingRuleMap != null) {
						for(ResponseByContentTypeRule responseByContentTypeRule : exceptionHandlingRuleMap) {
							ResponseMap responseMap = responseByContentTypeRule.getResponseMap();
							Responsible defaultResponse = responseByContentTypeRule.getDefaultResponse();
							
							if(responseMap != null) {
								for(Responsible res : responseMap) {
									ActionList actionList = res.getActionList();
									
									if(actionList != null) {
										for(Executable action : actionList) {
											if(pointcut == null || pointcut.matches(transletRule.getName(), action.getFullActionId())) {
												log.debug("aspectRule " + aspectRule + " transletRule " + transletRule + " responseByContentTypeRule " + responseByContentTypeRule + " action " + action);
												register(action, aspectRule);
												transletRule.setAspectAdviceRuleExists(true);
											}
										}
									}
								}
							}

							if(defaultResponse != null) {
								ActionList actionList = defaultResponse.getActionList();
								
								if(actionList != null) {
									for(Executable action : actionList) {
										if(pointcut == null || pointcut.matches(transletRule.getName(), action.getFullActionId())) {
											log.debug("aspectRule " + aspectRule + " transletRule " + transletRule + " responseByContentTypeRule " + responseByContentTypeRule + " defaultResponse " + defaultResponse + " action " + action);
											register(action, aspectRule);
											transletRule.setAspectAdviceRuleExists(true);
										}
									}
								}
							}
						}
					}
				} else if(joinpointScope == JoinpointScopeType.BEAN) {
					ContentList contentList = transletRule.getContentList();
					
					if(contentList != null) {
						for(ActionList actionList : contentList) {
							for(Executable action : actionList) {
								if(action.getActionType() == ActionType.BEAN) {
									BeanActionRule beanActionRule = ((BeanAction)action).getBeanActionRule();
									
									if(pointcut == null || pointcut.matches(transletRule.getName(), beanActionRule.getBeanId(), beanActionRule.getMethodName())) {
										log.debug("aspectRule " + aspectRule + " transletRule " + transletRule + " beanActionRule " + beanActionRule);
										register(action, aspectRule);
										transletRule.setAspectAdviceRuleExists(true);
									}
								}
							}
						}
					}
					
					ResponseByContentTypeRuleMap exceptionHandlingRuleMap = transletRule.getExceptionHandlingRuleMap();
					
					if(exceptionHandlingRuleMap != null) {
						for(ResponseByContentTypeRule responseByContentTypeRule : exceptionHandlingRuleMap) {
							ResponseMap responseMap = responseByContentTypeRule.getResponseMap();
							Responsible defaultResponse = responseByContentTypeRule.getDefaultResponse();
							
							if(responseMap != null) {
								for(Responsible res : responseMap) {
									ActionList actionList = res.getActionList();
									
									if(actionList != null) {
										for(Executable action : actionList) {
											if(action.getActionType() == ActionType.BEAN) {
												BeanActionRule beanActionRule = ((BeanAction)action).getBeanActionRule();
												
												if(pointcut == null || pointcut.matches(transletRule.getName(), beanActionRule.getBeanId(), beanActionRule.getMethodName())) {
													log.debug("aspectRule " + aspectRule + " transletRule " + transletRule + " beanActionRule " + beanActionRule);
													register(action, aspectRule);
													transletRule.setAspectAdviceRuleExists(true);
												}
											}
										}
									}
								}
							}

							if(defaultResponse != null) {
								ActionList actionList = defaultResponse.getActionList();
								
								if(actionList != null) {
									for(Executable action : actionList) {
										if(action.getActionType() == ActionType.BEAN) {
											BeanActionRule beanActionRule = ((BeanAction)action).getBeanActionRule();

											if(pointcut == null || pointcut.matches(transletRule.getName(), beanActionRule.getBeanId(), beanActionRule.getMethodName())) {
												register(action, aspectRule);
												transletRule.setAspectAdviceRuleExists(true);
											}
										}
									}
								}
							}
						}
					}
				} else { //translet scope
					if(pointcut == null || pointcut.matches(transletRule.getName())) {
						register(transletRule, aspectRule);
						transletRule.setAspectAdviceRuleExists(true);
					}
				}
			}			
		}
	}
	
	private void register(TransletRule transletRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = transletRule.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			transletRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	private void register(RequestRule requestRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = requestRule.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			requestRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	private void register(ContentList contentList, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = contentList.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			contentList.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	private void register(ResponseRule responseRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = responseRule.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			responseRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	private void register(Executable action, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = action.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			action.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	private void register(AspectAdviceRuleRegistry aspectAdviceRuleRegistry, AspectRule aspectRule) {
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
