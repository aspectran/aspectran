/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.web.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.base.adapter.ApplicationAdapter;
import com.aspectran.base.context.AspectranContext;
import com.aspectran.base.type.ContextMergeMode;
import com.aspectran.base.util.StringUtils;
import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.translet.registry.TransletNotFoundException;
import com.aspectran.web.AccessPermitter;
import com.aspectran.web.activity.WebAspectranActivity;
import com.aspectran.web.adapter.WebApplicationAdapter;
import com.aspectran.web.context.ContextLoader;
import com.aspectran.web.context.ContextLoaderFactory;

/**
 * Servlet implementation class for Servlet: Translets.
 */
public class WebAspectranActivityServlet extends HttpServlet implements Servlet {

	/** @serial */
	static final long serialVersionUID = 6659683668233267847L;

	private static final Log log = LogFactory.getLog(WebAspectranActivityServlet.class);
	
	private static boolean debugEnabled = log.isDebugEnabled();

	private AspectranContext context;
	
	private AccessPermitter accessPermitter;

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	/**
	 * Instantiates a new action servlet.
	 */
	public WebAspectranActivityServlet() {
		super();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		try {
			String remoteAccessAllowed = getServletConfig().getInitParameter("access:allowRemoteAddress");
			String remoteAccessDenied = getServletConfig().getInitParameter("access:denyRemoteAddress");
			
			if(!StringUtils.isEmpty(remoteAccessAllowed) || !StringUtils.isEmpty(remoteAccessDenied)) {
				accessPermitter = new AccessPermitter();
				accessPermitter.setAllowedAddresses(remoteAccessAllowed);
				accessPermitter.setDeniedAddresses(remoteAccessDenied);
			}
			
			ContextLoader contextLoader = ContextLoaderFactory.getContextLoader(getServletConfig());
			
			List<AspectranContext> contextList = contextLoader.getContextList();
			ContextMergeMode mergeMode = contextLoader.getMergeMode();
			
			log.debug("contextList:" + contextList);

			if(contextList != null) {
				if(contextList.size() == 1) {
					context = contextList.get(0);
					log.debug(context);
				} else {
					ContextMerger contextMerger = new ContextMerger(mergeMode);
					context = contextMerger.merge(contextList);
				}
			}

			if(context == null) {
				throw new Exception("지정된 컨텍스트가 없습니다.");
			}
			
			ApplicationAdapter applicationAdapter = new WebApplicationAdapter(getServletContext());
			context.setApplicationAdapter(applicationAdapter);
		} catch(Exception e) {
			log.error("Unable to initialize TransletsActivityServlet.", e);
			throw new UnavailableException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
			if(accessPermitter != null) {
				String remoteAddr = req.getRemoteAddr();
			
				if(!accessPermitter.isValidAccess(remoteAddr)) {
					if(debugEnabled) {
						log.debug("Access denied '" + remoteAddr + "'.");
					}
					
					res.sendError(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
			}
			
			String requestUri = req.getRequestURI();

			AspectranActivity activity = new WebAspectranActivity(context, req, res);
			activity.request(requestUri);
			activity.process();
			activity.response();
		} catch(TransletNotFoundException e) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			log.error(e.getMessage(), e);
			//e.printStackTrace();
		} catch(Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			log.error(e.getMessage(), e);
			//e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {
		log.info("Closing Trasnlets activity servlet. context " + context);
		
		super.destroy();
		
		if(context != null)
			context.destroy();
	}
}