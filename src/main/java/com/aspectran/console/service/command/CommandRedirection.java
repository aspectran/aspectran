/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.console.service.command;

import com.aspectran.core.util.ToStringBuilder;

/**
 * <p>Created: 2017. 3. 8.</p>
 */
public class CommandRedirection {

	private final Operator operator;

	private String operand;

	public CommandRedirection(Operator operator) {
		this.operator = operator;
	}

	public String getOperand() {
		return operand;
	}

	public void setOperand(String operand) {
		this.operand = operand;
	}

	public Operator getOperator() {
		return operator;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof CommandRedirection) {
			CommandRedirection r = (CommandRedirection)o;
			if(r.getOperand().equals(getOperand()) &&
					r.getOperator().equals(getOperator())) {
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
		tsb.append("operator", operator);
		tsb.append("operand", operand);
		return tsb.toString();
	}
	
	/**
	 * Command redirection operators
	 */
	public enum Operator {
		
		/**
		 * Writes the command output to a text file.
		 */
		OVERWRITE_OUT(">"), // >
		
		/**
		 * Appends command output to the end of a text file.
		 */
		APPEND_OUT(">>"); // >>
		
		private final String alias;

		Operator(String alias) {
			this.alias = alias;
		}

		@Override
		public String toString() {
			return this.alias;
		}
	
	}
	
}