package com.aspectran.scheduler.adapter;

import java.util.Collections;
import java.util.Enumeration;

import org.quartz.JobDetail;

import com.aspectran.core.adapter.AbstractRequestAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.var.AttributeMap;

/**
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
		return ((JobDetail)adaptee).getJobDataMap().getString(name);
	}
	
	public String[] getParameterValues(String name) {
		String value = ((JobDetail)adaptee).getJobDataMap().getString(name);
		
		if(value == null)
			return null;
		
		return new String[] { value };
	}
	
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(((JobDetail)adaptee).getJobDataMap().keySet());
	}
	
	public Object getAttribute(String name) {
		return attributeMap.get(name);
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
