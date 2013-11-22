package com.aspectran.scheduler.adapter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.quartz.JobDetail;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.adapter.AbstractResponseAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.var.rule.RedirectResponseRule;
import com.aspectran.core.var.type.ContentType;

/**
 * 
 * @since 2013. 11. 20.
 */
public class QuartzJobResponseAdapter extends AbstractResponseAdapter implements ResponseAdapter {

	private String characterEncoding;
	
	private JobDetail jobDetail;
	
	public QuartzJobResponseAdapter(JobDetail jobDetail) {
		super(null);
		
		this.jobDetail = jobDetail;
	}
	
	public String getCharacterEncoding() {
		return characterEncoding;
	}
	
	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}
	
	public String getContentType() {
		return ContentType.TEXT_PLAIN.toString();
	}

	public void setContentType(String contentType) {
		// only text/plain
	}
	
	public OutputStream getOutputStream() throws IOException {
		return null;
	}
	
	public Writer getWriter() throws IOException {
		return new QuartzJobResponseWriter(jobDetail);
	}
	
	public void redirect(String requestUri) throws IOException {
	}
	
	public String redirect(CoreActivity activity, RedirectResponseRule redirectResponseRule) throws IOException {
		return null;
	}

}
