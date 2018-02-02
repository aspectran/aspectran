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
package com.aspectran.shell.command.option;

/**
 * Thrown when more than one option in an option group
 * has been provided.
 */
public class AlreadySelectedException extends OptionParserException {

    /** @serial */
    private static final long serialVersionUID = -1818262614499753083L;

    /** The option group selected. */
    private OptionGroup group;

    /** The option that triggered the exception. */
    private Option option;

    /**
     * Construct a new {@code AlreadySelectedException}
     * with the specified detail message.
     *
     * @param message the detail message
     */
    public AlreadySelectedException(final String message) {
        super(message);
    }

    /**
     * Construct a new {@code AlreadySelectedException}
     * for the specified option group.
     *
     * @param group  the option group already selected
     * @param option the option that triggered the exception
     */
    public AlreadySelectedException(final OptionGroup group, final Option option) {
        this("The option '" + option.getKey() + "' was specified but an option from this group "
                + "has already been selected: '" + group.getSelected() + "'");
        this.group = group;
        this.option = option;
    }

    /**
     * Returns the option group where another option has been selected.
     *
     * @return the related option group
     */
    public OptionGroup getOptionGroup() {
        return group;
    }

    /**
     * Returns the option that was added to the group and triggered the exception.
     *
     * @return the related option
     */
    public Option getOption() {
        return option;
    }

}
