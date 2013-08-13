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
package com.aspectran.web.view;

import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.dispatch.DispatchResponseException;
import com.aspectran.core.activity.response.dispatch.ViewDispatcher;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.rule.DispatchResponseRule;
import com.aspectran.core.rule.ResponseRule;

/**
 * JSP or other web resource integration.
 * 
 * <p>
 * Created: 2008. 03. 22 오후 5:51:58
 * </p>
 */
public class JspViewDispatcher implements ViewDispatcher {

	private final Log log = LogFactory.getLog(JspViewDispatcher.class);

	private final boolean debugEnabled = log.isDebugEnabled();

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.dispatch.ViewDispatcher#dispatch(com.aspectran.core.activity.AspectranActivity, com.aspectran.base.rule.DispatchResponseRule)
	 */
	public void dispatch(AspectranActivity activity, DispatchResponseRule dispatchResponseRule) throws DispatchResponseException {
		try {
			RequestAdapter requestAdapter = activity.getRequestAdapter();
			ResponseAdapter responseAdapter = activity.getResponseAdapter();

			String contentType = dispatchResponseRule.getContentType();
			String outputEncoding = dispatchResponseRule.getCharacterEncoding();

			if(contentType != null)
				responseAdapter.setContentType(contentType);

			if(outputEncoding != null)
				responseAdapter.setCharacterEncoding(outputEncoding);
			else {
				String characterEncoding = (String)activity.getResponseSetting(ResponseRule.CHARACTER_ENCODING_SETTING);
				
				if(characterEncoding != null)
					responseAdapter.setCharacterEncoding(characterEncoding);
			}
			
			String templateFile = dispatchResponseRule.getTemplateFile();
			ProcessResult processResult = activity.getProcessResult();

			if(processResult != null)
				parse(requestAdapter, processResult, null);

			HttpServletRequest request = (HttpServletRequest)requestAdapter.getAdaptee();
			HttpServletResponse response = (HttpServletResponse)responseAdapter.getAdaptee();
			
			RequestDispatcher requestDispatcher = request.getRequestDispatcher(templateFile);
			requestDispatcher.forward(request, response);

			if(debugEnabled) {
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
					log.debug(sb2.toString());
				}

				log.debug("JSP Dispatch {templateFile: " + templateFile + "}");
			}
		} catch(Exception e) {
			throw new DispatchResponseException("Dispatch response error: " + dispatchResponseRule, e);
		}
	}

	/**
	 * Parse.
	 * 
	 * @param servletRequest the servlet request
	 * @param processResult the process result
	 * @param parentActionPath the parent action path
	 */
	private void parse(RequestAdapter requestAdapter, ProcessResult processResult, String parentActionPath) {
		for(ContentResult contentResult : processResult) {
			for(ActionResult actionResult : contentResult) {
				Object actionResultValue = actionResult.getResultValue();

				if(actionResultValue instanceof ProcessResult)
					parse(requestAdapter, (ProcessResult)actionResultValue, actionResult.getActionPath());
				else
					requestAdapter.setAttribute(actionResult.getActionPath(parentActionPath), actionResultValue);
			}
		}
	}

}
