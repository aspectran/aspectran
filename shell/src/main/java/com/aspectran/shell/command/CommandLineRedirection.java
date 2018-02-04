/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.shell.command;

import com.aspectran.core.util.ToStringBuilder;

/**
 * <p>Created: 2017. 3. 8.</p>
 */
public class CommandLineRedirection {

    private final Operator operator;

    private String operand;

    public CommandLineRedirection(Operator operator) {
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
        if (this == o) {
            return true;
        }
        if (o instanceof CommandLineRedirection) {
            CommandLineRedirection cr = (CommandLineRedirection)o;
            if (cr.getOperand().equals(operand) &&
                    cr.getOperator().equals(operator)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 17;
        result = prime * result + operator.hashCode();
        result = prime * result + (operand != null ? operand.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("operator", operator);
        tsb.append("operand", operand);
        return tsb.toString();
    }

    /**
     * Command redirection operators.
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