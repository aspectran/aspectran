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
package com.aspectran.core.var.rule;

import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.var.type.AspectAdviceType;

/**
 * <p>Created: 2008. 04. 01 오후 11:19:28</p>
 */
public class AspectJobAdviceRule {

	private String aspectId;
	
	private final AspectAdviceType aspectAdviceType = AspectAdviceType.JOB;
	
	private String jobTransletName;
	
	private Boolean disabled;

	public String getAspectId() {
		return aspectId;
	}

	public void setAspectId(String aspectId) {
		this.aspectId = aspectId;
	}

	public AspectAdviceType getAspectAdviceType() {
		return aspectAdviceType;
	}

	public String getJobTransletName() {
		return jobTransletName;
	}

	public void setJobTransletName(String jobTransletName) {
		this.jobTransletName = jobTransletName;
	}

	public Boolean getDisabled() {
		return disabled;
	}
	
	public boolean isDisabled() {
		return BooleanUtils.toBoolean(disabled);
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{aspectId=").append(aspectId);
		sb.append(", aspectAdviceType=").append(aspectAdviceType);
		sb.append(", jobTransletName=").append(jobTransletName);
		sb.append(", disabled=").append(disabled);
		sb.append("}");
		
		return sb.toString();
	}
	
	public static AspectJobAdviceRule newInstance(AspectRule aspectRule, String transletName, Boolean disabled) {
		AspectJobAdviceRule ajar = new AspectJobAdviceRule();
		ajar.setAspectId(aspectRule.getId());
		ajar.setJobTransletName(transletName);
		ajar.setDisabled(disabled);
		
		return ajar;
	}
	
}
