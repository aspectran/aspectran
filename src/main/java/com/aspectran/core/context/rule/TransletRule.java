/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.context.rule;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.variable.token.Token;
import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;
import com.aspectran.core.context.rule.type.RequestMethodType;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.wildcard.WildcardPattern;

/**
 * The Class TransletRule.
 *
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public class TransletRule implements ActionRuleApplicable, ResponseRuleApplicable {

	private String name;

	private RequestMethodType restVerb;
	
	private WildcardPattern namePattern;
	
	private Token[] nameTokens;
	
	private String maskPattern;
	
	private String path;
	
	private RequestRule requestRule;
	
	private ContentList contentList;

	private boolean explicitContent;

	private ResponseRule responseRule;
	
	/** The response rule list of child translet. */
	private List<ResponseRule> responseRuleList;
	
	private boolean implicitResponse;

	private ExceptionHandlingRule exceptionHandlingRule;
	
	private Class<? extends Translet> transletInterfaceClass;
	
	private Class<? extends CoreTranslet> transletImplementClass;

	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;
	
	private Parameters filterParameters;

	private String description;
	
	/**
	 * Instantiates a new TransletRule.
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
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the restful verb.
	 *
	 * @return the restful verb
	 */
	public RequestMethodType getRestVerb() {
		return restVerb;
	}

	/**
	 * Sets the rest verb.
	 *
	 * @param restVerb the new rest verb
	 */
	public void setRestVerb(RequestMethodType restVerb) {
		this.restVerb = restVerb;
	}

	/**
	 * Gets the name pattern.
	 *
	 * @return the name pattern
	 */
	public WildcardPattern getNamePattern() {
		return namePattern;
	}

	/**
	 * Sets the name pattern.
	 *
	 * @param namePattern the new name pattern
	 */
	public void setNamePattern(WildcardPattern namePattern) {
		this.namePattern = namePattern;
	}

	/**
	 * Gets the name tokens.
	 *
	 * @return the name tokens
	 */
	public Token[] getNameTokens() {
		return nameTokens;
	}

	/**
	 * Sets the name tokens.
	 *
	 * @param nameTokens the new name tokens
	 */
	public void setNameTokens(Token[] nameTokens) {
		this.nameTokens = nameTokens;
	}

	public String getMaskPattern() {
		return maskPattern;
	}

	public void setMaskPattern(String maskPattern) {
		this.maskPattern = maskPattern;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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
		
		if(contentList != null)
			explicitContent = true;
	}

	public ContentList touchContentList(boolean explicitContent) {
		this.explicitContent = explicitContent;
		return touchContentList();
	}
	
	public synchronized ContentList touchContentList() {
		if(contentList == null) {
			contentList = new ContentList();
			contentList.setOmittable(Boolean.TRUE);
		}
		
		return contentList;
	}

	public boolean isExplicitContent() {
		return explicitContent;
	}

	public void applyActionRule(EchoActionRule echoActionRule) {
		touchActionList().applyActionRule(echoActionRule);
	}

	public void applyActionRule(BeanActionRule beanActionRule) {
		touchActionList().applyActionRule(beanActionRule);
	}

	public void applyActionRule(IncludeActionRule includeActionRule) {
		touchActionList().applyActionRule(includeActionRule);
	}
	
	private ActionList touchActionList() {
		touchContentList();
		
		if(contentList.size() == 1) {
			return contentList.get(0);
		} else {
			return contentList.newActionList(true);
		}
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
		implicitResponse = false;
	}
	
	public List<ResponseRule> getResponseRuleList() {
		return responseRuleList;
	}

	public void setResponseRuleList(List<ResponseRule> responseRuleList) {
		this.responseRuleList = responseRuleList;
		implicitResponse = false;
	}
	
	public void addResponseRule(ResponseRule responseRule) {
		if(responseRuleList == null)
			responseRuleList = new ArrayList<ResponseRule>();
		
		responseRuleList.add(responseRule);
		implicitResponse = false;
	}

	public Response applyResponseRule(TransformRule tr) {
		if(responseRule == null)
			responseRule = new ResponseRule();
		
		implicitResponse = true;

		return responseRule.applyResponseRule(tr);
	}
	
	/**
	 * Adds the response rule.
	 * 
	 * @param drr the drr
	 * 
	 * @return the dispatch response
	 */
	public Response applyResponseRule(DispatchResponseRule drr) {
		if(responseRule == null)
			responseRule = new ResponseRule();

		implicitResponse = true;

		return responseRule.applyResponseRule(drr);
	}
	
	/**
	 * Adds the response rule.
	 * 
	 * @param rrr the rrr
	 * 
	 * @return the redirect response
	 */
	public Response applyResponseRule(RedirectResponseRule rrr) {
		if(responseRule == null)
			responseRule = new ResponseRule();
		
		implicitResponse = true;
		
		return responseRule.applyResponseRule(rrr);
	}
	
	/**
	 * Adds the response rule.
	 * 
	 * @param frr the frr
	 * 
	 * @return the forward response
	 */
	public Response applyResponseRule(ForwardResponseRule frr) {
		if(responseRule == null)
			responseRule = new ResponseRule();
		
		implicitResponse = true;

		return responseRule.applyResponseRule(frr);
	}
	
	private void addActionList(ActionList actionList) {
		if(actionList == null)
			return;
		
		touchContentList();		
		contentList.add(actionList);
	}
	
	public void determineResponseRule() {
		if(responseRule == null) {
			responseRule = new ResponseRule();
		} else {
			if(responseRule.getResponse() != null) {
				addActionList(responseRule.getResponse().getActionList());
			}
			assembleTransletName(this, responseRule);
		}

		setResponseRuleList(null);
	}

	public boolean isImplicitResponse() {
		return implicitResponse;
	}

	public ExceptionHandlingRule getExceptionHandlingRule() {
		return exceptionHandlingRule;
	}

	public void setExceptionHandlingRule(ExceptionHandlingRule exceptionHandlingRule) {
		this.exceptionHandlingRule = exceptionHandlingRule;
	}

	public ExceptionHandlingRule touchExceptionHandlingRule() {
		if(exceptionHandlingRule == null)
			exceptionHandlingRule = new ExceptionHandlingRule();
		
		return exceptionHandlingRule;
	}
	
	public Class<? extends Translet> getTransletInterfaceClass() {
		return transletInterfaceClass;
	}

	public void setTransletInterfaceClass(Class<? extends Translet> transletInterfaceClass) {
		this.transletInterfaceClass = transletInterfaceClass;
	}

	public Class<? extends CoreTranslet> getTransletImplementClass() {
		return transletImplementClass;
	}

	public void setTransletImplementClass(Class<? extends CoreTranslet> transletImplementClass) {
		this.transletImplementClass = transletImplementClass;
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
	
	public Parameters getFilterParameters() {
		return filterParameters;
	}

	public void setFilterParameters(Parameters filterParameters) {
		this.filterParameters = filterParameters;
	}
	
	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{name=").append(name);
		if(restVerb != null) {
			sb.append(", restVerb=").append(restVerb);
			sb.append(", namePattern=").append(namePattern);
		}
		sb.append(", requestRule=").append(requestRule);
		sb.append(", responseRule=").append(responseRule);
		if(exceptionHandlingRule != null)
			sb.append(", exceptionHandlingRule=").append(exceptionHandlingRule);
		if(transletInterfaceClass != null)
			sb.append(", transletInterfaceClass=").append(transletInterfaceClass);
		if(transletImplementClass != null)
			sb.append(", transletImplementClass=").append(transletImplementClass);
		if(aspectAdviceRuleRegistry != null)
			sb.append(", aspectAdviceRuleRegistry=").append(aspectAdviceRuleRegistry);
		if(explicitContent)
		sb.append(", explicitContent=").append(explicitContent);
		if(implicitResponse)
			sb.append(", implicitResponse=").append(implicitResponse);
		sb.append("}");
		
		return sb.toString();
	}
	
	public static TransletRule newInstance(String name, String mask, String path, String restVerb) {
		if(name == null)
			throw new IllegalArgumentException("The <translet> element requires a name attribute.");

		RequestMethodType requestMethod = null;
		
		if(restVerb != null) {
			requestMethod = RequestMethodType.valueOf(restVerb);
			
			if(requestMethod == null)
				throw new IllegalArgumentException("Unknown REST Verb '" + restVerb + "' for translet '" + name + "'");
		}

		TransletRule transletRule = new TransletRule();
		transletRule.setName(name);
		if(restVerb != null) {
			transletRule.setRestVerb(requestMethod);
		} else {
			transletRule.setMaskPattern(mask);
			transletRule.setPath(path);
		}
		
		return transletRule;
	}
	
	public static TransletRule newSubTransletRule(TransletRule transletRule, ResponseRule responseRule) {
		TransletRule newTransletRule = new TransletRule();
		newTransletRule.setName(transletRule.getName());
		newTransletRule.setRestVerb(transletRule.getRestVerb());
		newTransletRule.setRequestRule(transletRule.getRequestRule());
		newTransletRule.setResponseRule(responseRule);
		newTransletRule.setExceptionHandlingRule(transletRule.getExceptionHandlingRule());
		newTransletRule.setTransletInterfaceClass(transletRule.getTransletInterfaceClass());
		newTransletRule.setTransletImplementClass(transletRule.getTransletImplementClass());
		
		if(responseRule.getResponse() != null) {
			if(responseRule.getResponse().getActionList() != null) {
				ContentList contentList = transletRule.getContentList();
				if(contentList != null) {
					contentList = (ContentList)contentList.clone();
					newTransletRule.setContentList(contentList);
				}
			}
		}
		
		return newTransletRule;
	}
	
	public static TransletRule newDerivedTransletRule(TransletRule transletRule, String templateFile) {
		TransletRule newTransletRule = new TransletRule();
		newTransletRule.setName(transletRule.getName());
		newTransletRule.setRestVerb(transletRule.getRestVerb());
		newTransletRule.setRequestRule(transletRule.getRequestRule());
		newTransletRule.setExceptionHandlingRule(transletRule.getExceptionHandlingRule());
		newTransletRule.setTransletInterfaceClass(transletRule.getTransletInterfaceClass());
		newTransletRule.setTransletImplementClass(transletRule.getTransletImplementClass());

		ResponseRule responseRule = transletRule.getResponseRule();
		if(responseRule != null && responseRule.getTemplateRule() != null) {
			responseRule = ResponseRule.newDerivedResponseRule(responseRule);
			responseRule.getTemplateRule().setFile(templateFile);
			newTransletRule.setResponseRule(responseRule);
		}
		
		List<ResponseRule> responseRuleList = transletRule.getResponseRuleList();
		if(responseRuleList != null) {
			for(int i = 0; i < responseRuleList.size(); i++) {
				ResponseRule rr = responseRuleList.get(i);
				if(rr.getTemplateRule() != null) {
					rr = ResponseRule.newDerivedResponseRule(rr);
					rr.getTemplateRule().setFile(templateFile);
					responseRuleList.set(i, rr);
				}
			}
		}
		
		return newTransletRule;
	}
	
	protected static void assembleTransletName(TransletRule transletRule, ResponseRule responseRule) {
		String responseName = responseRule.getName();
		
		if(responseName != null && responseName.length() > 0) {
			String transletName = transletRule.getName();

			if(responseName.charAt(0) == AspectranConstant.TRANSLET_NAME_EXTENSION_SEPARATOR) {
				transletName += responseName;
			} else {
				transletName += AspectranConstant.TRANSLET_NAME_SEPARATOR + responseName;
			}
			
			transletRule.setName(transletName);
		}
	}
	
	protected static void disassembleTransletName(TransletRule transletRule, ResponseRule responseRule) {
		String responseName = responseRule.getName();

		if(responseName != null && responseName.length() > 0) {
			String transletName = transletRule.getName();
		
			if(transletName.endsWith(responseName)) {
				transletName = transletName.substring(0, transletName.length() - responseName.length());
			}
			
			transletRule.setName(transletName);
		}
	}
	
	public static String makeRestfulTransletName(String transletName, RequestMethodType restVerb) {
		return restVerb + " " + transletName;
	}

}
