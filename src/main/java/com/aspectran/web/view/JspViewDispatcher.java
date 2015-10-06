/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.web.view;

import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.dispatch.ViewDispatchException;
import com.aspectran.core.activity.response.dispatch.ViewDispatcher;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * JSP or other web resource integration.
 * 
 * @since 2008. 03. 22 오후 5:51:58
 */
public class JspViewDispatcher implements ViewDispatcher {

	private static final Log log = LogFactory.getLog(JspViewDispatcher.class);

	private static final boolean debugEnabled = log.isDebugEnabled();
	
	private static final boolean traceEnabled = log.isTraceEnabled();
	
	private String templateFilePrefix;

	private String templateFileSuffix;
	
	/**
	 * Sets the template file prefix.
	 *
	 * @param templateFilePrefix the new template file prefix
	 */
	public void setTemplateFilePrefix(String templateFilePrefix) {
		this.templateFilePrefix = templateFilePrefix;
	}

	/**
	 * Sets the template file suffix.
	 *
	 * @param templateFileSuffix the new template file suffix
	 */
	public void setTemplateFileSuffix(String templateFileSuffix) {
		this.templateFileSuffix = templateFileSuffix;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.dispatch.ViewDispatcher#dispatch(com.aspectran.core.activity.AspectranActivity, com.aspectran.base.rule.DispatchResponseRule)
	 */
	public void dispatch(Activity activity, DispatchResponseRule dispatchResponseRule) {
		try {
			TemplateRule templateRule = dispatchResponseRule.getTemplateRule();
			if(templateRule == null) {
				log.warn("No specified template. " + dispatchResponseRule);
				return;
			}

			String templateFile = templateRule.getFile();
			if(templateFile == null) {
				log.warn("No specified template file. " + dispatchResponseRule);
				return;
			}
			
			if(templateFilePrefix != null && templateFileSuffix != null) {
				templateFile = templateFilePrefix + templateFile + templateFileSuffix;
			} else if(templateFilePrefix != null) {
				templateFile = templateFilePrefix + templateFile;
			} else if(templateFileSuffix != null) {
				templateFile = templateFile + templateFileSuffix;
			}
			
			RequestAdapter requestAdapter = activity.getRequestAdapter();
			ResponseAdapter responseAdapter = activity.getResponseAdapter();

			String contentType = dispatchResponseRule.getContentType();
			String outputEncoding = dispatchResponseRule.getCharacterEncoding();

			if(contentType != null)
				responseAdapter.setContentType(contentType);

			if(outputEncoding != null)
				responseAdapter.setCharacterEncoding(outputEncoding);
			else {
				String characterEncoding = activity.getResponseSetting(ResponseRule.CHARACTER_ENCODING_SETTING_NAME);
				
				if(characterEncoding != null)
					responseAdapter.setCharacterEncoding(characterEncoding);
			}
			
			ProcessResult processResult = activity.getProcessResult();

			if(processResult != null)
				setAttribute(requestAdapter, processResult, null);

			HttpServletRequest request = requestAdapter.getAdaptee();
			HttpServletResponse response = responseAdapter.getAdaptee();
			
			RequestDispatcher requestDispatcher = request.getRequestDispatcher(templateFile);
			requestDispatcher.forward(request, response);

			if(traceEnabled) {
				Enumeration<String> attrNames = requestAdapter.getAttributeNames();

				if(attrNames.hasMoreElements()) {
					StringBuilder sb2 = new StringBuilder(256);
					sb2.append("request atttibute names [");
					String name = null;

					while(attrNames.hasMoreElements()) {
						if(name != null)
							sb2.append(", ");

						name = attrNames.nextElement();
						sb2.append(name);
					}

					sb2.append("]");
					log.trace(sb2.toString());
				}

				if(debugEnabled)
					log.debug("dispatch to a JSP {templateFile: " + templateFile + "}");
			}
		} catch(Exception e) {
			throw new ViewDispatchException("JSP View Dispatch Error: " + dispatchResponseRule, e);
		}
	}

	/**
	 * Stores an attribute in request.
	 *
	 * @param requestAdapter the request adapter
	 * @param processResult the process result
	 * @param parentQualifiedActionId the parent qualified action id
	 */
	private void setAttribute(RequestAdapter requestAdapter, ProcessResult processResult, String parentQualifiedActionId) {
		for(ContentResult contentResult : processResult) {
			for(ActionResult actionResult : contentResult) {
				Object actionResultValue = actionResult.getResultValue();

				if(actionResultValue instanceof ProcessResult) {
					setAttribute(requestAdapter, (ProcessResult)actionResultValue, actionResult.getQuialifiedActionId());
				} else {
					String actionId = actionResult.getQuialifiedActionId(parentQualifiedActionId);
					if(actionId != null)
						requestAdapter.setAttribute(actionResult.getQuialifiedActionId(parentQualifiedActionId), actionResultValue);
				}
			}
		}
	}

}
