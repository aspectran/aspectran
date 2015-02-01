/*
 *  Copyright (c) 2010 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.core.var.rule;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.CoreTransletImpl;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.action.BeanAction;
import com.aspectran.core.activity.process.action.EchoAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.action.IncludeAction;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.AbstractTransform;
import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.var.rule.ability.ActionAddable;
import com.aspectran.core.var.rule.ability.ResponseSettable;
import com.aspectran.core.var.type.ResponseType;
import com.aspectran.core.var.type.TransformType;

/**
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public class TransletRule implements ActionAddable, ResponseSettable, AspectAdviceSupport, Cloneable {

	private String name;

	private RequestRule requestRule;
	
	private ContentList contentList;
	
	private ResponseRule responseRule;
	
	/** The response rule list of child translet. */
	private List<ResponseRule> responseRuleList;
	
	private ResponseByContentTypeRuleMap exceptionHandlingRuleMap;
	
	private Class<? extends CoreTranslet> transletInterfaceClass;
	
	private Class<? extends CoreTransletImpl> transletImplementClass;

	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;
	
	//private boolean aspectAdviceRuleExists;

	/**
	 * Instantiates a new translet rule.
	 */
	public TransletRule() {
	}

	/**
	 * Gets the translet name.
	 * 
	 * @return the translet name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the tranlset name.
	 * 
	 * @param name the new translet name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the request rule.
	 * 
	 * @return the request rule
	 */
	public RequestRule getRequestRule() {
		return requestRule;
	}

	/**
	 * Sets the request rule.
	 * 
	 * @param requestRule the new request rule
	 */
	public void setRequestRule(RequestRule requestRule) {
		this.requestRule = requestRule;
	}

	/**
	 * Gets the content list.
	 * 
	 * @return the content list
	 */
	public ContentList getContentList() {
		return contentList;
	}

	/**
	 * Sets the content list.
	 * 
	 * @param contentList the new content list
	 */
	public void setContentList(ContentList contentList) {
		this.contentList = contentList;
	}

	/**
	 * Gets the response rule.
	 * 
	 * @return the response rule
	 */
	public ResponseRule getResponseRule() {
		return responseRule;
	}
	
	/**
	 * Sets the response rule.
	 * 
	 * @param responseRule the new response rule
	 */
	public void setResponseRule(ResponseRule responseRule) {
		Responsible response = responseRule.getResponse();
		
		if(response != null) {
			addActionList(response.getActionList());
		}
		
		if(this.responseRule != null)
			unadoptTransletName(this, this.responseRule);
		
		this.responseRule = responseRule;
		
		adoptTransletName(this, responseRule);
	}
	
	public List<ResponseRule> getResponseRuleList() {
		return responseRuleList;
	}

	public void setResponseRuleList(List<ResponseRule> responseRuleList) {
		this.responseRuleList = responseRuleList;
	}
	
	public void addResponseRule(ResponseRule responseRule) {
		if(responseRuleList == null)
			responseRuleList = new ArrayList<ResponseRule>();
		
		responseRuleList.add(responseRule);
	}

	public AbstractTransform setResponse(TransformRule tr) {
		addActionList(tr.getActionList());
		
		if(responseRule == null)
			responseRule = new ResponseRule();
		
		return responseRule.setResponse(tr);
	}
	
	/**
	 * Adds the response rule.
	 * 
	 * @param drr the drr
	 * 
	 * @return the dispatch response
	 */
	public DispatchResponse setResponse(DispatchResponseRule drr) {
		addActionList(drr.getActionList());

		if(responseRule == null)
			responseRule = new ResponseRule();
		
		return responseRule.setResponse(drr);
	}
	
	/**
	 * Adds the response rule.
	 * 
	 * @param rrr the rrr
	 * 
	 * @return the redirect response
	 */
	public RedirectResponse setResponse(RedirectResponseRule rrr) {
		addActionList(rrr.getActionList());

		if(responseRule == null)
			responseRule = new ResponseRule();
		
		return responseRule.setResponse(rrr);
	}
	
	/**
	 * Adds the response rule.
	 * 
	 * @param frr the frr
	 * 
	 * @return the forward response
	 */
	public ForwardResponse setResponse(ForwardResponseRule frr) {
		addActionList(frr.getActionList());

		if(responseRule == null)
			responseRule = new ResponseRule();
		
		return responseRule.setResponse(frr);
	}
	
	private void addActionList(ActionList actionList) {
		if(actionList == null)
			return;
		
		if(contentList == null)
			contentList = new ContentList();
		
		contentList.add(actionList);
	}

	public ResponseByContentTypeRuleMap getExceptionHandlingRuleMap() {
		return exceptionHandlingRuleMap;
	}

	public void setExceptionHandlingRuleMap(ResponseByContentTypeRuleMap responseByContentTypeRuleMap) {
		this.exceptionHandlingRuleMap = responseByContentTypeRuleMap;
	}

	public void addExceptionHandlingRule(ResponseByContentTypeRule responseByContentTypeRule) {
		if(exceptionHandlingRuleMap == null)
			exceptionHandlingRuleMap = new ResponseByContentTypeRuleMap();
		
		exceptionHandlingRuleMap.putResponseByContentTypeRule(responseByContentTypeRule);
	}
	
	public Class<? extends CoreTranslet> getTransletInterfaceClass() {
		return transletInterfaceClass;
	}

	public void setTransletInterfaceClass(Class<? extends CoreTranslet> transletInterfaceClass) {
		this.transletInterfaceClass = transletInterfaceClass;
	}

	public Class<? extends CoreTransletImpl> getTransletImplementClass() {
		return transletImplementClass;
	}

	public void setTransletInstanceClass(Class<? extends CoreTransletImpl> transletInstanceClass) {
		this.transletImplementClass = transletInstanceClass;
	}

	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry() {
		return aspectAdviceRuleRegistry;
	}

	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry(boolean clone) throws CloneNotSupportedException {
		if(clone && aspectAdviceRuleRegistry != null)
			return (AspectAdviceRuleRegistry)aspectAdviceRuleRegistry.clone();
		
		return aspectAdviceRuleRegistry;
	}

	public void setAspectAdviceRuleRegistry(AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
		this.aspectAdviceRuleRegistry = aspectAdviceRuleRegistry;
	}
	
//	/**
//	 * @return the aspectAdviceRuleExists
//	 */
//	public boolean isAspectAdviceRuleExists() {
//		return aspectAdviceRuleExists;
//	}
//
//	/**
//	 * @param aspectAdviceRuleExists the aspectAdviceRuleExists to set
//	 */
//	public void setAspectAdviceRuleExists(boolean aspectAdviceRuleExists) {
//		this.aspectAdviceRuleExists = aspectAdviceRuleExists;
//	}

	public List<AspectAdviceRule> getBeforeAdviceRuleList() {
		if(aspectAdviceRuleRegistry == null)
			return null;
		
		return aspectAdviceRuleRegistry.getBeforeAdviceRuleList();
	}
	
	public List<AspectAdviceRule> getAfterAdviceRuleList() {
		if(aspectAdviceRuleRegistry == null)
			return null;
		
		return aspectAdviceRuleRegistry.getAfterAdviceRuleList();
	}
	
	public List<AspectAdviceRule> getFinallyAdviceRuleList() {
		if(aspectAdviceRuleRegistry == null)
			return null;
		
		return aspectAdviceRuleRegistry.getFinallyAdviceRuleList();
	}
	
	public List<AspectAdviceRule> getExceptionRaizedAdviceRuleList() {
		if(aspectAdviceRuleRegistry == null)
			return null;
		
		return aspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
	}
	
	public TransletRule newSubTransletRule(ResponseRule responseRule) throws CloneNotSupportedException {
		TransletRule transletRule = (TransletRule)clone();
		transletRule.setResponseRule(responseRule);
		transletRule.setResponseRuleList(null);
		
		return transletRule;
	}
	
	private void adoptTransletName(TransletRule transletRule, ResponseRule responseRule) {
		String transletName = transletRule.getName();
		String responseName = responseRule.getName();
		
		if(responseName != null && responseName.length() > 0) {
			if(responseName.charAt(0) == AspectranConstant.TRANSLET_NAME_EXTENSION_DELIMITER) {
				transletName += responseName;
			} else {
				transletName += AspectranConstant.TRANSLET_NAME_SEPARATOR + responseName;
			}
			
			transletRule.setName(transletName);
		}
	}
	
	private void unadoptTransletName(TransletRule transletRule, ResponseRule responseRule) {
		String transletName = transletRule.getName();
		String responseName = responseRule.getName();
		
		if(responseName != null && responseName.length() > 0) {
			if(transletName.endsWith(responseName)) {
				transletName = transletName.substring(0, transletName.length() - responseName.length());
			}
			
			transletRule.setName(transletName);
		}
	}
	
	public Object clone() throws CloneNotSupportedException {                      
		return super.clone();              
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{name=").append(name);
		sb.append(", requestRule=").append(requestRule);
		sb.append(", responseRule=").append(responseRule);
		sb.append(", responseRuleList=").append(responseRuleList);
		sb.append(", exceptionHandlingRuleMap=").append(exceptionHandlingRuleMap);
		sb.append(", transletInterfaceClass=").append(transletInterfaceClass);
		sb.append(", transletInstanceClass=").append(transletImplementClass);
		sb.append(", aspectAdviceRuleRegistry=").append(aspectAdviceRuleRegistry);
		//sb.append(", aspectAdviceRuleExists=").append(aspectAdviceRuleExists);
		sb.append("}");
		
		return sb.toString();
	}
	
	/**
	 * Describing translet.
	 * 
	 * @return the string
	 */
	public String describe() {
		final String CRLF = AspectranConstant.LINE_SEPARATOR;
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("Translet ").append(toString()).append(CRLF);
		
		if(requestRule != null) {
			sb.append("   Request ").append(requestRule).append(CRLF);
			
			if(requestRule.getAttributeItemRuleMap() != null) {
				for(ItemRule ir : requestRule.getAttributeItemRuleMap())
					sb.append("      Attribute ").append(ir).append(CRLF);
			}
			if(requestRule.getFileItemRuleMap() != null) {
				for(FileItemRule fir : requestRule.getFileItemRuleMap())
					sb.append("      FileItem ").append(fir).append(CRLF);
			}
		}
		
		if(contentList != null) {
			sb.append("   Process {contentCount=").append(contentList.size()).append("}").append(CRLF);
	
			for(ActionList actionList : contentList) {
				sb.append("      Content ").append(actionList).append(CRLF);
	
				for(Executable executable : actionList) {
					if(executable instanceof EchoAction) {
						EchoAction action = (EchoAction)executable;
						sb.append("         EchoAction ").append(action).append(CRLF);
						if(action.getEchoActionRule().getAttributeItemRuleMap() != null) {
							for(ItemRule pr : action.getEchoActionRule().getAttributeItemRuleMap())
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

		if(responseRule != null) {
			sb.append("   Response ").append(responseRule).append(CRLF);

			if(responseRule.getResponse() != null) {
				Responsible responsible = responseRule.getResponse();

				if(responsible.getResponseType() == ResponseType.TRANSFORM) {
					AbstractTransform tr = (AbstractTransform)responsible;
					if(tr.getTransformType() == TransformType.XSL_TRANSFORM) {
						sb.append("      XSLTransformer ");
					} else if(tr.getTransformType() == TransformType.XML_TRANSFORM) {
						sb.append("      XMLTransformer ");
					} else if(tr.getTransformType() == TransformType.TEXT_TRANSFORM) {
						sb.append("      TextTransformer ");
					} else if(tr.getTransformType() == TransformType.JSON_TRANSFORM) {
						sb.append("      JSONTransformer ");
					} else if(tr.getTransformType() == TransformType.CUSTOM_TRANSFORM) {
						sb.append("      CustomTransformer ");
					}
					sb.append(tr.getTransformRule()).append(CRLF);
				} else if(responsible.getResponseType() == ResponseType.DISPATCH) {
					DispatchResponse dr = (DispatchResponse)responsible;
					sb.append("      DispatchResponse " + dr.getDispatchResponseRule()).append(CRLF);
				} else if(responsible.getResponseType() == ResponseType.FORWARD) {
					ForwardResponse fr = (ForwardResponse)responsible;
					sb.append("      ForwardResponse " + fr.getForwardResponseRule()).append(CRLF);
					if(fr.getForwardResponseRule().getParameterItemRuleMap() != null) {
						for(ItemRule pr : fr.getForwardResponseRule().getParameterItemRuleMap())
							sb.append("            Parameter ").append(pr).append(CRLF);
					}
				} else if(responsible.getResponseType() == ResponseType.REDIRECT) {
					RedirectResponse rr = (RedirectResponse)responsible;
					sb.append("      RedirectResponse " + rr.getRedirectResponseRule()).append(CRLF);
					if(rr.getRedirectResponseRule().getParameterItemRuleMap() != null) {
						for(ItemRule pr : rr.getRedirectResponseRule().getParameterItemRuleMap())
							sb.append("            Parameter ").append(pr).append(CRLF);
					}
				}

				ActionList actionList = responsible.getActionList();
				
				if(actionList != null) {
					for(Executable executable : actionList) {
						if(executable instanceof EchoAction) {
							EchoAction action = (EchoAction)executable;
							sb.append("         EchoAction ").append(action).append(CRLF);
							if(action.getEchoActionRule().getAttributeItemRuleMap() != null) {
								for(ItemRule pr : action.getEchoActionRule().getAttributeItemRuleMap())
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

		return sb.toString();
	}

	public void addEchoAction(EchoActionRule echoActionRule) {
		getActionList().addEchoAction(echoActionRule);
	}

	public void addBeanAction(BeanActionRule beanActionRule) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, NoSuchMethodException {
		getActionList().addBeanAction(beanActionRule);
	}

	public void addIncludeAction(IncludeActionRule includeActionRule) {
		getActionList().addIncludeAction(includeActionRule);
	}
	
	private ActionList getActionList() {
		ActionList actionList;		
		
		if(contentList == null) {
			actionList = new ActionList();
			contentList = new ContentList();
			contentList.add(actionList);
		} else {
			if(contentList.size() > 0)
				actionList = contentList.get(0);
			else {
				actionList = new ActionList();
				contentList.add(actionList);
			}
		}
		
		return actionList;
	}
	
	public static TransletRule newInstance(String name) {
		if(name == null)
			throw new IllegalArgumentException("The <translet> element requires a name attribute.");

		TransletRule transletRule = new TransletRule();
		transletRule.setName(name);

		return transletRule;
	}
	
}
