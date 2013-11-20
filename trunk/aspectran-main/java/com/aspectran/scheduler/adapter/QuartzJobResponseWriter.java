package com.aspectran.scheduler.adapter;

import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuartzJobResponseWriter extends StringWriter {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(QuartzJobResponseWriter.class);

	@Override
	public void flush() {
		logger.info(toString());
	}

}
