package com.aspectran.support.http;

import java.io.IOException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;

public class HttpAccessControlAllowOriginFilter {
	
	private static final String HEADER_ORIGIN = "origin";
	
	private boolean withCredentials;
	
	private Set<String> origins;

	public void setWithCredentials(boolean withCredentials) {
		this.withCredentials = withCredentials;
	}

	public void setOrigins(Set<String> origins) {
		this.origins = origins;
	}

	public void checkAccessControlAllowCredentials(Translet translet) throws IOException {
		if(withCredentials) {
			RequestAdapter requestAdapter = translet.getRequestAdapter();
			HttpServletRequest req = requestAdapter.getAdaptee();
	
			boolean allowed = false;
			
			String origin = req.getHeader(HEADER_ORIGIN);
			allowed = origins.contains(origin);
			
			if(allowed) {
				ResponseAdapter responseAdapter = translet.getResponseAdapter();
				HttpServletResponse res = responseAdapter.getAdaptee();
				res.setHeader("Access-Control-Allow-Credentials", "true");
			    res.addHeader("Access-Control-Allow-Origin", origin);
			}
		}		
	}
}
