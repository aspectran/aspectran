package com.aspectran.scheduler.adapter;

import java.util.Collections;
import java.util.Enumeration;

import org.quartz.JobDetail;

import com.aspectran.core.activity.variable.AttributeMap;
import com.aspectran.core.adapter.AbstractRequestAdapter;
import com.aspectran.core.adapter.RequestAdapter;

/**
 * The Class QuartzJobRequestAdapter.
 * 
 * @since 2013. 11. 20.
 */
public class QuartzJobRequestAdapter extends AbstractRequestAdapter implements RequestAdapter {
	
	private String characterEncoding;
	
	private AttributeMap attributeMap = new AttributeMap();
	
	/**
	 * Instantiates a new quartz job request adapter.
	 *
	 * @param request the request
	 */
	public QuartzJobRequestAdapter(JobDetail jobDetail) {
		super(jobDetail);
	}
	
	public String getCharacterEncoding() {
		return characterEncoding;
	}
	
	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}
	
	public String getParameter(String name) {
		return null;
	}
	
	public String[] getParameterValues(String name) {
		return null;
	}
	
	public Enumeration<String> getParameterNames() {
		return null;
	}
	
	public <T> T getAttribute(String name) {
		return attributeMap.getValue(name);
	}
	
	public void setAttribute(String name, Object o) {
		attributeMap.put(name, o);
	}
	
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attributeMap.keySet());

	}

	public void removeAttribute(String name) {
		attributeMap.remove(name);
	}
}
