package com.aspectran.console.service;

import com.aspectran.core.util.ToStringBuilder;

/**
 * <p>Created: 2017. 3. 8.</p>
 */
public class RedirectionOperation {

	private final RedirectionOperator redirectionOperator;

	private String buffer;

	public RedirectionOperation(RedirectionOperator redirectionOperator) {
		this.redirectionOperator = redirectionOperator;
	}

	public String getBuffer() {
		return buffer;
	}

	public void setBuffer(String buffer) {
		this.buffer = buffer;
	}

	public RedirectionOperator getRedirectionOperator() {
		return redirectionOperator;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof RedirectionOperation) {
			RedirectionOperation r = (RedirectionOperation)o;
			if(r.getBuffer().equals(getBuffer()) &&
					r.getRedirectionOperator().equals(getRedirectionOperator())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return 7129415;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("redirectionOperator", redirectionOperator);
		tsb.append("buffer", buffer);
		return tsb.toString();
	}

}