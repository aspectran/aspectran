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

import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Options represents a collection of {@link Option} objects, which
 * describe the possible options for a command-line.</p>
 *
 * <p>It may flexibly parse long and short options, with or without
 * values.  Additionally, it may parse only a portion of a commandline,
 * allowing for flexible multi-stage parsing.</p>
 *
 * @see ParsedOptions
 */
public class Options implements Serializable {

    @Serial
    private static final long serialVersionUID = -6416293453155205092L;

    /** A map of the options with the character key */
    private final Map<String, Option> shortOpts = new LinkedHashMap<>();

    /** A map of the options with the long key */
    private final Map<String, Option> longOpts = new LinkedHashMap<>();

    /** A map of the required options */
    private final List<Object> requiredOpts = new ArrayList<>();

    /** A map of the option groups */
    private final Map<String, OptionGroup> optionGroups = new LinkedHashMap<>();

    private String title = "Options:";

    private boolean skipParsingAtNonOption;

    public Options() {
        this(false);
    }

    public Options(boolean skipParsingAtNonOption) {
        this.skipParsingAtNonOption = skipParsingAtNonOption;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSkipParsingAtNonOption() {
        return skipParsingAtNonOption;
    }

    public void setSkipParsingAtNonOption(boolean skipParsingAtNonOption) {
        this.skipParsingAtNonOption = skipParsingAtNonOption;
    }

    /**
     * Add the specified option group.
     * @param group the OptionGroup that is to be added
     * @return the resulting Options instance
     */
    public Options addOptionGroup(@NonNull OptionGroup group) {
        if (group.isRequired()) {
            requiredOpts.add(group);
        }
        for (Option option : group.getOptions()) {
            // an Option cannot be required if it is in an
            // OptionGroup, either the group is required or
            // nothing is required
            option.setRequired(false);
            addOption(option);
            optionGroups.put(option.getKey(), group);
        }
        return this;
    }

    /**
     * Lists the OptionGroups that are members of this Options instance.
     * @return a Collection of OptionGroup instances
     */
    Collection<OptionGroup> getOptionGroups() {
        return new HashSet<>(optionGroups.values());
    }

    /**
     * Adds an option instance.
     * @param opt the option that is to be added
     * @return the resulting Options instance
     */
    public Options addOption(@NonNull Option opt) {
        String key = opt.getKey();

        // add it to the long option list
        if (opt.hasLongName()) {
            longOpts.put(opt.getLongName(), opt);
        }

        // if the option is required add it to the required list
        if (opt.isRequired()) {
            if (requiredOpts.contains(key)) {
                requiredOpts.remove(requiredOpts.indexOf(key));
            }
            requiredOpts.add(key);
        }

        shortOpts.put(key, opt);
        return this;
    }

    /**
     * Returns {@code true} if no options have been added.
     * @return {@code true} if no options have been added
     */
    public boolean isEmpty() {
        return shortOpts.isEmpty();
    }

    /**
     * Retrieve a read-only list of options in this set.
     * @return read-only Collection of {@link Option} objects in this descriptor
     */
    public Collection<Option> getAllOptions() {
        return Collections.unmodifiableCollection(shortOpts.values());
    }

    /**
     * Returns the Options for use by the HelpFormatter.
     * @return the List of Options
     */
    public List<Option> getHelpOptions() {
        return new ArrayList<>(shortOpts.values());
    }

    /**
     * Returns the required options.
     * @return read-only List of required options
     */
    public List<Object> getRequiredOptions() {
        return Collections.unmodifiableList(requiredOpts);
    }

    /**
     * Retrieve the {@link Option} matching the long or short name specified.
     * <p>The leading hyphens in the name are ignored (up to 2).</p>
     * @param name short or long name of the {@link Option}
     * @return the option represented by name
     */
    public Option getOption(String name) {
        if (shortOpts.containsKey(name)) {
            return shortOpts.get(name);
        }
        return longOpts.get(name);
    }

    /**
     * Returns the options with a long name starting with the name specified.
     * @param name the partial name of the option
     * @return the options matching the partial name specified, or an empty list if none matches
     */
    public List<String> getMatchingOptions(String name) {
        List<String> matchingOpts = new ArrayList<>();
        // for a perfect match return the single option only
        if (longOpts.containsKey(name)) {
            return Collections.singletonList(name);
        }
        for (String longOpt : longOpts.keySet()) {
            if (longOpt.startsWith(name)) {
                matchingOpts.add(longOpt);
            }
        }
        return matchingOpts;
    }

    /**
     * Returns whether the named {@link Option} is a member of this {@link Options}.
     * @param name short or long name of the {@link Option}
     * @return true if the named {@link Option} is a member of this {@link Options}
     */
    public boolean hasOption(String name) {
        return (shortOpts.containsKey(name) || longOpts.containsKey(name));
    }

    /**
     * Returns whether the named {@link Option} is a member of this {@link Options}.
     * @param name long name of the {@link Option}
     * @return true if the named {@link Option} is a member of this {@link Options}
     */
    public boolean hasLongOption(String name) {
        return longOpts.containsKey(name);
    }

    /**
     * Returns whether the named {@link Option} is a member of this {@link Options}.
     * @param name short name of the {@link Option}
     * @return true if the named {@link Option} is a member of this {@link Options}
     */
    public boolean hasShortOption(String name) {
        return shortOpts.containsKey(name);
    }

    /**
     * Returns the OptionGroup the <code>opt</code> belongs to.
     * @param opt the option whose OptionGroup is being queried.
     * @return the OptionGroup if <code>opt</code> is part of an OptionGroup, otherwise return null
     */
    public OptionGroup getOptionGroup(@NonNull Option opt) {
        return optionGroups.get(opt.getKey());
    }

    /**
     * Dump state, suitable for debugging.
     * @return the stringified form of this object
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ Options: [ short ");
        sb.append(shortOpts);
        sb.append(" ] [ long ");
        sb.append(longOpts);
        sb.append(" ]");
        return sb.toString();
    }

}
