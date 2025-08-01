/*
 * Copyright (c) 2008-present The Aspectran Project
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

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A group of mutually exclusive options.
 */
public class OptionGroup implements Serializable {

    @Serial
    private static final long serialVersionUID = 5281255012541670780L;

    /** Hold the options */
    private final Map<String, Option> optionMap = new LinkedHashMap<>();

    /** The name of the selected option */
    private String selected;

    /** Specified whether this group is required */
    private boolean required;

    /**
     * Add the specified {@code Option} to this group.
     * @param option the option to add to this group
     * @return this option group with the option added
     */
    public OptionGroup addOption(Option option) {
        // key   - option name
        // value - the option
        optionMap.put(option.getKey(), option);
        return this;
    }

    /**
     * @return the names of the options in this group as a {@code Collection}
     */
    public Collection<String> getNames() {
        // the key set is the collection of names
        return optionMap.keySet();
    }

    /**
     * @return the options in this group as a {@code Collection}
     */
    public Collection<Option> getOptions() {
        // the values are the collection of options
        return optionMap.values();
    }

    /**
     * Set the selected option of this group to {@code name}.
     * @param option the option that is selected
     * @throws AlreadySelectedException if an option from this group has
     *      already been selected.
     */
    public void setSelected(Option option) throws AlreadySelectedException {
        if (option == null) {
            // reset the option previously selected
            selected = null;
            return;
        }

        // if no option has already been selected or the
        // same option is being reselected then set the
        // selected member variable
        if (selected == null || selected.equals(option.getKey())) {
            selected = option.getKey();
        } else {
            throw new AlreadySelectedException(this, option);
        }
    }

    /**
     * Returns the selected option name.
     * @return the selected option name
     */
    public String getSelected() {
        return selected;
    }

    /**
     * @param required specifies if this group is required
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Returns whether this option group is required.
     * @return whether this option group is required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Returns the stringified version of this OptionGroup.
     * @return the stringified representation of this group
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("[");

        Iterator<Option> it = getOptions().iterator();
        while (it.hasNext()) {
            Option option = it.next();
            if (option.getName() != null) {
                buff.append("-");
                buff.append(option.getName());
            } else {
                buff.append("--");
                buff.append(option.getLongName());
            }
            if (option.getDescription() != null) {
                buff.append(" ");
                buff.append(option.getDescription());
            }
            if (it.hasNext()) {
                buff.append(", ");
            }
        }

        buff.append("]");
        return buff.toString();
    }

}
