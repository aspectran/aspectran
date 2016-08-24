/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.rule;

import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class AspectJobAdviceRule.
 * 
 * <p>Created: 2008. 04. 01 PM 11:19:28</p>
 */
public class JobRule {

	private String transletName;
	
	private Boolean disabled;

	public String getTransletName() {
		return transletName;
	}

	public void setTransletName(String transletName) {
		this.transletName = transletName;
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

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("transletName", transletName);
		tsb.append("disabled", disabled);
		return tsb.toString();
	}
	
	public static JobRule newInstance(String transletName, Boolean disabled) {
		JobRule jobRule = new JobRule();
		jobRule.setTransletName(transletName);
		jobRule.setDisabled(disabled);
		return jobRule;
	}
	
}
