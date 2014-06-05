package com.aspectran.core.context.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.var.rule.AspectRule;
import com.aspectran.core.var.rule.AspectRuleMap;
import com.aspectran.core.var.rule.BeanRuleMap;
import com.aspectran.core.var.rule.RequestRule;
import com.aspectran.core.var.rule.ResponseRule;
import com.aspectran.core.var.rule.TransletRule;
import com.aspectran.core.var.rule.TransletRuleMap;
import com.aspectran.core.var.type.AspectTargetType;
import com.aspectran.core.var.type.JoinpointScopeType;

public class AspectAdviceRulePreRegister extends AspectAdviceRuleRegister {
	
	private final Logger logger = LoggerFactory.getLogger(AspectAdviceRulePreRegister.class);
	
	private AspectRuleMap aspectRuleMap;
	
	public AspectAdviceRulePreRegister(AspectRuleMap aspectRuleMap) {
		this.aspectRuleMap = aspectRuleMap;
	}
	
	public void register(BeanRuleMap beanRuleMap, TransletRuleMap transletRuleMap) {
//		for(BeanRule beanRule : beanRuleMap) {
//			register(beanRule);
//		}
		
		for(AspectRule aspectRule : aspectRuleMap) {
			AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();
			JoinpointScopeType joinpointScope = aspectRule.getJoinpointScope();
			Pointcut pointcut = aspectRule.getPointcut();

			if(aspectTargetType == AspectTargetType.TRANSLET) {
				if(joinpointScope == JoinpointScopeType.TRANSLET ||
						joinpointScope == JoinpointScopeType.REQUEST ||
						joinpointScope == JoinpointScopeType.CONTENT ||
						joinpointScope == JoinpointScopeType.RESPONSE) {
					if(pointcut == null) {
						
					} else {
						if(!pointcut.matches(null, "") && !pointcut.matches(null, null, "")) {

						}
					}
				}
			}
		}

		for(TransletRule transletRule : transletRuleMap) {
			register(transletRule);
		}
		
		//TODO preregistered
		
	}
	
	private void register(TransletRule transletRule) {
		for(AspectRule aspectRule : aspectRuleMap) {
			AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();
			JoinpointScopeType joinpointScope = aspectRule.getJoinpointScope();
			Pointcut pointcut = aspectRule.getPointcut();
			
			if(aspectTargetType == AspectTargetType.TRANSLET) {
				if(joinpointScope == JoinpointScopeType.REQUEST) {
					if(pointcut == null || pointcut.strictMatches(transletRule.getName())) {
						RequestRule requestRule = transletRule.getRequestRule();
						
						if(logger.isTraceEnabled())
							logger.trace("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule + "\n\t> requestRule " + requestRule);
						
						register(requestRule, aspectRule);
					}
				} else if(joinpointScope == JoinpointScopeType.CONTENT) {
					if(pointcut == null || pointcut.matches(transletRule.getName())) {
						ContentList contentList = transletRule.getContentList();
						
						if(contentList == null) {
							contentList = new ContentList();
							transletRule.setContentList(contentList);
						}

						if(logger.isTraceEnabled())
							logger.trace("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule + "\n\t> contentList " + contentList);
						
						register(contentList, aspectRule);
					}
				} else if(joinpointScope == JoinpointScopeType.RESPONSE) {
					if(pointcut == null || pointcut.matches(transletRule.getName())) {
						ResponseRule responseRule = transletRule.getResponseRule();
						
						if(logger.isTraceEnabled())
							logger.trace("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule + "\n\t> responseRule " + responseRule);
						
						register(responseRule, aspectRule);
					}
				} else { //translet scope
					if(pointcut == null || pointcut.matches(transletRule.getName())) {
						if(logger.isTraceEnabled())
							logger.trace("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule);
						
						register(transletRule, aspectRule);
					}
				}
			}			
		}
	}
	
	
//	private void register(BeanRule beanRule) {
//		for(AspectRule aspectRule : aspectRuleMap) {
//			AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();
//			//JoinpointScopeType joinpointScope = aspectRule.getJoinpointScope();
//			
//			if(aspectTargetType == AspectTargetType.TRANSLET) {
//				Pointcut pointcut = aspectRule.getPointcut();
//			
//				//if(joinpointScope == JoinpointScopeType.BEAN) {
//					if(pointcut == null || pointcut.matches(null, beanRule.getId())) {
//						logger.debug("aspectRule " + aspectRule + "\n\t> beanRule " + beanRule);
//						register(beanRule, aspectRule);
//						beanRule.setProxyMode(true);
//					}
//				//}
//			}
//		}
//	}
//
//	private void register(TransletRule transletRule) {
//		for(AspectRule aspectRule : aspectRuleMap) {
//			AspectTargetType aspectTargetType = aspectRule.getAspectTargetType();
//			JoinpointScopeType joinpointScope = aspectRule.getJoinpointScope();
//			Pointcut pointcut = aspectRule.getPointcut();
//			
//			if(aspectTargetType == AspectTargetType.TRANSLET) {
//				if(joinpointScope == JoinpointScopeType.REQUEST) {
//					if(pointcut == null || pointcut.matches(transletRule.getName())) {
//						RequestRule requestRule = transletRule.getRequestRule();
//						logger.debug("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule + "\n\t> requestRule " + requestRule);
//						register(transletRule.getRequestRule(), aspectRule);
//						//transletRule.setAspectAdviceRuleExists(true);
//					}
//				} else if(joinpointScope == JoinpointScopeType.CONTENT) {
//					if(pointcut.isHoldsAction()) {
//						ContentList contentList = transletRule.getContentList();
//						register(contentList, null);
//					} else if(pointcut == null || pointcut.matches(transletRule.getName())) {
//						ContentList contentList = transletRule.getContentList();
//						
//						if(contentList == null) {
//							contentList = new ContentList();
//							transletRule.setContentList(contentList);
//						}
//
//						logger.debug("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule + "\n\t> contentList " + contentList);
//						register(contentList, aspectRule);
//						//transletRule.setAspectAdviceRuleExists(true);
//					}/*
//				} else if(joinpointScope == JoinpointScopeType.ACTION_CONTENT) {
//					if(pointcut != null && pointcut.isActionInfluenced()) {
//						ContentList contentList = transletRule.getContentList();
//						
//						if(contentList != null) {
//							boolean actionExists = false;
//							
//							for(ActionList actionList : contentList) {
//								for(Executable action : actionList) {
//									if(pointcut.matches(transletRule.getName(), action.getFullActionId())) {
//										actionExists = true;
//										break;
//									}
//								}
//							}
//							
//							if(actionExists) {
//								logger.debug("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule + "\n\t> contentList " + contentList);
//								register(contentList, aspectRule);
//								transletRule.setAspectAdviceRuleExists(true);
//							}
//						}
//					}*//*
//				} else if(joinpointScope == JoinpointScopeType.BEAN_CONTENT) {
//					if(pointcut != null && pointcut.isActionInfluenced()) {
//						ContentList contentList = transletRule.getContentList();
//						
//						if(contentList != null) {
//							boolean actionExists = false;
//							
//							for(ActionList actionList : contentList) {
//								for(Executable action : actionList) {
//									if(action.getActionType() == ActionType.BEAN) {
//										BeanActionRule beanActionRule = ((BeanAction)action).getBeanActionRule();
//										
//										if(pointcut.matches(transletRule.getName(), beanActionRule.getBeanId(), beanActionRule.getMethodName())) {
//											actionExists = true;
//											break;
//										}
//									}
//								}
//							}
//							
//							if(actionExists) {
//								logger.debug("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule + "\n\t> contentList " + contentList);
//								register(contentList, aspectRule);
//								transletRule.setAspectAdviceRuleExists(true);
//							}
//						}
//					}*/
//				} else if(joinpointScope == JoinpointScopeType.RESPONSE) {
//					if(pointcut == null || pointcut.matches(transletRule.getName())) {
//						ResponseRule responseRule = transletRule.getResponseRule();
//						logger.debug("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule + "\n\t> responseRule " + responseRule);
//						register(transletRule.getResponseRule(), aspectRule);
//						//transletRule.setAspectAdviceRuleExists(true);
//					}
//				/*} else if(joinpointScope == JoinpointScopeType.ACTION) {
//					ContentList contentList = transletRule.getContentList();
//					
//					if(contentList != null) {
//						for(ActionList actionList : contentList) {
//							for(Executable action : actionList) {
//								if(pointcut == null || pointcut.matches(transletRule.getName(), action.getFullActionId())) {
//									logger.debug("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule + "\n\t> action " + action);
//									register(action, aspectRule);
//									transletRule.setAspectAdviceRuleExists(true);
//								}
//							}
//						}
//					}
//					
//					ResponseByContentTypeRuleMap exceptionHandlingRuleMap = transletRule.getExceptionHandlingRuleMap();
//					
//					if(exceptionHandlingRuleMap != null) {
//						for(ResponseByContentTypeRule responseByContentTypeRule : exceptionHandlingRuleMap) {
//							ResponseMap responseMap = responseByContentTypeRule.getResponseMap();
//							Responsible defaultResponse = responseByContentTypeRule.getDefaultResponse();
//							
//							if(responseMap != null) {
//								for(Responsible res : responseMap) {
//									ActionList actionList = res.getActionList();
//									
//									if(actionList != null) {
//										for(Executable action : actionList) {
//											if(pointcut == null || pointcut.matches(transletRule.getName(), action.getFullActionId())) {
//												logger.debug("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule + "\n\t> responseByContentTypeRule " + responseByContentTypeRule + " action " + action);
//												register(action, aspectRule);
//												transletRule.setAspectAdviceRuleExists(true);
//											}
//										}
//									}
//								}
//							}
//
//							if(defaultResponse != null) {
//								ActionList actionList = defaultResponse.getActionList();
//								
//								if(actionList != null) {
//									for(Executable action : actionList) {
//										if(pointcut == null || pointcut.matches(transletRule.getName(), action.getFullActionId())) {
//											logger.debug("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule + "\n\t> responseByContentTypeRule " + responseByContentTypeRule + " defaultResponse " + defaultResponse + " action " + action);
//											register(action, aspectRule);
//											transletRule.setAspectAdviceRuleExists(true);
//										}
//									}
//								}
//							}
//						}
//					}*//*
//				} else if(joinpointScope == JoinpointScopeType.BEAN) {
//					ContentList contentList = transletRule.getContentList();
//					
//					if(contentList != null) {
//						for(ActionList actionList : contentList) {
//							for(Executable action : actionList) {
//								if(action.getActionType() == ActionType.BEAN) {
//									BeanActionRule beanActionRule = ((BeanAction)action).getBeanActionRule();
//									
//									if(pointcut == null || pointcut.matches(transletRule.getName(), beanActionRule.getBeanId(), beanActionRule.getMethodName())) {
//										logger.debug("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule + "\n\t> beanActionRule " + beanActionRule);
//										register(action, aspectRule);
//										transletRule.setAspectAdviceRuleExists(true);
//									}
//								}
//							}
//						}
//					}
//					
//					ResponseByContentTypeRuleMap exceptionHandlingRuleMap = transletRule.getExceptionHandlingRuleMap();
//					
//					if(exceptionHandlingRuleMap != null) {
//						for(ResponseByContentTypeRule responseByContentTypeRule : exceptionHandlingRuleMap) {
//							ResponseMap responseMap = responseByContentTypeRule.getResponseMap();
//							Responsible defaultResponse = responseByContentTypeRule.getDefaultResponse();
//							
//							if(responseMap != null) {
//								for(Responsible res : responseMap) {
//									ActionList actionList = res.getActionList();
//									
//									if(actionList != null) {
//										for(Executable action : actionList) {
//											if(action.getActionType() == ActionType.BEAN) {
//												BeanActionRule beanActionRule = ((BeanAction)action).getBeanActionRule();
//												
//												if(pointcut == null || pointcut.matches(transletRule.getName(), beanActionRule.getBeanId(), beanActionRule.getMethodName())) {
//													logger.debug("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule + "\n\t> beanActionRule " + beanActionRule);
//													register(action, aspectRule);
//													transletRule.setAspectAdviceRuleExists(true);
//												}
//											}
//										}
//									}
//								}
//							}
//
//							if(defaultResponse != null) {
//								ActionList actionList = defaultResponse.getActionList();
//								
//								if(actionList != null) {
//									for(Executable action : actionList) {
//										if(action.getActionType() == ActionType.BEAN) {
//											BeanActionRule beanActionRule = ((BeanAction)action).getBeanActionRule();
//
//											if(pointcut == null || pointcut.matches(transletRule.getName(), beanActionRule.getBeanId(), beanActionRule.getMethodName())) {
//												logger.debug("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule + "\n\t> responseByContentTypeRule " + responseByContentTypeRule + "\n\t> defaultResponse " + defaultResponse + "\n\t> action " + action);
//												register(action, aspectRule);
//												transletRule.setAspectAdviceRuleExists(true);
//											}
//										}
//									}
//								}
//							}
//						}
//					}*/
//				} else { //translet scope
//					if(pointcut == null || pointcut.matches(transletRule.getName())) {
//						logger.debug("aspectRule " + aspectRule + "\n\t> transletRule " + transletRule);
//						register(transletRule, aspectRule);
//						//transletRule.setAspectAdviceRuleExists(true);
//					}
//				}
//			}			
//		}
//	}

	protected void register(TransletRule transletRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = transletRule.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			transletRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	protected void register(RequestRule requestRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = requestRule.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			requestRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	protected void register(ContentList contentList, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = contentList.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			contentList.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		if(aspectRule != null)
			register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	protected void register(ResponseRule responseRule, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = responseRule.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			responseRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
	protected void register(Executable action, AspectRule aspectRule) {
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = action.getAspectAdviceRuleRegistry();
		
		if(aspectAdviceRuleRegistry == null) {
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
			action.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
		}
		
		register(aspectAdviceRuleRegistry, aspectRule);
	}
	
}
