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
package com.aspectran.core.rule;

import java.util.List;

import com.aspectran.core.activity.AbstractSuperTranslet;
import com.aspectran.core.activity.SuperTranslet;
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
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.builder.AspectranContextConstant;
import com.aspectran.core.type.ResponseType;
import com.aspectran.core.type.TransformType;

/**
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public class TransletRule implements AspectAdviceSupport, Cloneable {

	private String name;

	private String parentTransletName;
	
	private RequestRule requestRule;
	
	private ContentList contentList;
	
	private ResponseRule responseRule;
	
	private ResponseByContentTypeRuleMap exceptionHandlingRuleMap;
	
	private String multipleTransletResponseId;
	
	private Class<? extends SuperTranslet> transletInterfaceClass;
	
	private Class<? extends AbstractSuperTranslet> transletInstanceClass;

	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;
	
	private boolean aspectAdviceRuleExists;

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
	
	public String getParentTransletName() {
		return parentTransletName;
	}

	public void setParentTransletName(String parentTransletName) {
		this.parentTransletName = parentTransletName;
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
		this.responseRule = responseRule;
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
	
	public String getMultipleTransletResponseId() {
		return multipleTransletResponseId;
	}

	public void setMultipleTransletResponseId(String multipleTransletResponseId) {
		this.multipleTransletResponseId = multipleTransletResponseId;
	}

	public Class<? extends SuperTranslet> getTransletInterfaceClass() {
		return transletInterfaceClass;
	}

	public void setTransletInterfaceClass(Class<? extends SuperTranslet> transletInterfaceClass) {
		this.transletInterfaceClass = transletInterfaceClass;
	}

	public Class<? extends AbstractSuperTranslet> getTransletInstanceClass() {
		return transletInstanceClass;
	}

	public void setTransletInstanceClass(Class<? extends AbstractSuperTranslet> transletInstanceClass) {
		this.transletInstanceClass = transletInstanceClass;
	}

	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry() {
		return aspectAdviceRuleRegistry;
	}

	public void setAspectAdviceRuleRegistry(AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
		this.aspectAdviceRuleRegistry = aspectAdviceRuleRegistry;
	}
	
	/**
	 * @return the aspectAdviceRuleExists
	 */
	public boolean isAspectAdviceRuleExists() {
		return aspectAdviceRuleExists;
	}

	/**
	 * @param aspectAdviceRuleExists the aspectAdviceRuleExists to set
	 */
	public void setAspectAdviceRuleExists(boolean aspectAdviceRuleExists) {
		this.aspectAdviceRuleExists = aspectAdviceRuleExists;
	}

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
		sb.append("}");
		
		return sb.toString();
	}
	
	/**
	 * Describing translet.
	 * 
	 * @return the string
	 */
	public String describe() {
		final String CRLF = AspectranContextConstant.LINE_SEPARATOR;
		
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

		if(responseRule != null) {
			sb.append("   Response ").append(responseRule).append(CRLF);

			if(responseRule.getResponseMap() != null) {
				for(Responsible responsible : responseRule.getResponseMap()) {
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

		return sb.toString();
	}
}
