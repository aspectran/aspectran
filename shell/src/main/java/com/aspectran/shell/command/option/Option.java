/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Describes a single command-line option.  It maintains
 * information regarding the short-name of the option, the long-name,
 * if any exists, a flag indicating if an argument is required for
 * this option, and a self-documenting description of the option.
 *
 * <p>An Option is not created independently, but is created through
 * an instance of {@link Options}. An Option is required to have
 * at least a short or a long-name.</p>
 *
 * <p><strong>Note:</strong> once an {@link Option} has been added to an instance
 * of {@link Options}, it's required flag may not be changed anymore.</p>
 *
 * @see Options
 * @see ParsedOptions
 */
public class Option implements Cloneable, Serializable {

    private static final long serialVersionUID = -7707766888283034409L;

    /** Constant that specifies the number of argument values has not been specified */
    public static final int UNINITIALIZED = -1;

    /** Constant that specifies the number of argument values is infinite */
    public static final int UNLIMITED_VALUES = -2;

    /** The name of the option */
    private final String name;

    /** The long representation of the option */
    private String longName;

    /** The name of the argument value for this option */
    private String valueName;

    /** The Option will use '=' as a means to separate argument value */
    private boolean withEqualSign;

    /** Description of the option */
    private String description;

    /** Specifies whether this option is required to be present */
    private boolean required;

    /** Specifies whether the argument value of this Option is optional */
    private boolean optionalValue;

    /** The number of argument values this option can have */
    private int numberOfValues = UNINITIALIZED;

    /** The type of this Option */
    private OptionValueType valueType = OptionValueType.STRING;

    /** The list of argument values **/
    private List<String> values = new ArrayList<>();

    /**
     * Private constructor used by the nested Builder class.
     * 
     * @param builder builder used to create this option
     */
    private Option(Builder builder) {
        this.name = builder.name;
        this.longName = builder.longName;
        this.valueName = builder.valueName;
        this.valueType = builder.valueType;
        this.withEqualSign = builder.withEqualSign;
        this.numberOfValues = builder.numberOfValues;
        this.optionalValue = builder.optionalValue;
        this.required = builder.required;
        this.description = builder.description;
    }
    
    /**
     * Creates an Option using the specified parameters.
     * The option does not take an argument.
     * @param name short representation of the option
     * @param description describes the function of the option
     * @throws IllegalArgumentException if there are any non valid
     *      Option characters in {@code name}
     */
    public Option(String name, String description) throws IllegalArgumentException {
        this(name, null, false, description);
    }

    /**
     * Creates an Option using the specified parameters.
     * @param name short representation of the option
     * @param hasValue specifies whether the Option takes an argument value or not
     * @param description describes the function of the option
     * @throws IllegalArgumentException if there are any non valid
     *      Option characters in {@code name}
     */
    public Option(String name, boolean hasValue, String description) throws IllegalArgumentException {
        this(name, null, hasValue, description);
    }

    /**
     * Creates an Option using the specified parameters.
     * @param name short representation of the option
     * @param longName the long representation of the option
     * @param hasValue specifies whether the Option takes an argument value or not
     * @param description describes the function of the option
     * @throws IllegalArgumentException if there are any non valid
     *      Option characters in {@code name}
     */
    public Option(String name, String longName, boolean hasValue, String description)
           throws IllegalArgumentException {
        // ensure that the option is valid
        OptionUtils.validateOption(name);

        this.name = name;
        this.longName = longName;

        // if hasValue is set then the number of arguments is 1
        if (hasValue) {
            this.numberOfValues = 1;
        }

        this.description = description;
    }

    /**
     * Returns the id of this Option.  This is only set when the
     * Option shortOpt is a single character.  This is used for switch
     * statements.
     * @return the id of this Option
     */
    public int getId() {
        return getKey().charAt(0);
    }

    /**
     * Returns the 'unique' Option identifier.
     * 
     * @return the 'unique' Option identifier
     */
    public String getKey() {
        // if 'opt' is null, then it is a 'long' option
        return (name == null ? longName : name);
    }

    /** 
     * Retrieve the name of this Option.
     *
     * It is this String which can be used with
     * {@link ParsedOptions#hasOption(String name)} and
     * {@link ParsedOptions#getValue(String name)} to check
     * for existence and argument.
     * @return the name of this option
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieve the long name of this Option.
     * @return the long name of this Option, or null, if there is no long name
     */
    public String getLongName() {
        return longName;
    }

    /**
     * Sets the long name of this Option.
     * @param longName the long name of this Option
     */
    public void setLongName(String longName) {
        this.longName = longName;
    }

    /**
     * Query to see if this Option has a long name.
     * @return boolean flag indicating existence of a long name
     */
    public boolean hasLongName() {
        return longName != null;
    }

    /**
     * Gets the display name for the argument value.
     * @return the display name for the argument value
     */
    public String getValueName() {
        return valueName;
    }

    /**
     * Sets the display name for the argument value.
     * @param valueName the display name for the argument value
     */
    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    /**
     * Returns whether the display name for the argument value has been set.
     * @return if the display name for the argument value has been set
     */
    public boolean hasValueName() {
        return (valueName != null && valueName.length() > 0);
    }

    /**
     * Retrieve the type of this Option.
     * @return the type of this option
     */
    public OptionValueType getValueType() {
        return valueType;
    }

    /**
     * Sets the type of this Option.
     * @param valueType the type of this Option
     */
    public void setValueType(OptionValueType valueType) {
        this.valueType = valueType;
    }

    /**
     * Sets whether this Option can have an optional argument.
     * @param optionalValue specifies whether the Option can have
     *      an optional argument.
     */
    public void setOptionalValue(boolean optionalValue) {
        this.optionalValue = optionalValue;
    }

    /**
     * Returns whether this Option can have an optional argument.
     * @return whether this Option can have an optional argument
     */
    public boolean hasOptionalValue() {
        return optionalValue;
    }

    /**
     * Returns the number of argument values this Option can take.
     *
     * <p>
     * A value equal to the constant {@link #UNINITIALIZED} (= -1) indicates
     * the number of arguments has not been specified.
     * A value equal to the constant {@link #UNLIMITED_VALUES} (= -2) indicates
     * that this options takes an unlimited amount of values.
     * </p>
     * @return num the number of argument values
     * @see #UNINITIALIZED
     * @see #UNLIMITED_VALUES
     */
    public int getNumberOfValues() {
        return numberOfValues;
    }

    /**
     * Sets the number of argument values this Option can take.
     * @param num the number of argument values
     */
    public void setNumberOfValues(int num) {
        this.numberOfValues = num;
    }

    /**
     * Query to see if this Option requires an argument.
     * @return boolean flag indicating if an argument is required
     */
    public boolean hasValue() {
        return (numberOfValues > 0 || numberOfValues == UNLIMITED_VALUES);
    }

    /**
     * Query to see if this Option can take many values.
     * @return boolean flag indicating if multiple values are allowed
     */
    public boolean hasValues() {
        return (numberOfValues > 1 || numberOfValues == UNLIMITED_VALUES);
    }

    public void withEqualSign() {
        setWithEqualSign(true);
    }

    public boolean isWithEqualSign() {
        return withEqualSign;
    }

    public void setWithEqualSign(boolean withEqualSign) {
        this.withEqualSign = withEqualSign;
    }

    /**
     * Adds the specified value to this Option.
     * @param value is a/the value of this Option
     */
    public void addValue(String value) {
        if (numberOfValues == UNINITIALIZED) {
            throw new RuntimeException("NO_ARGS_ALLOWED");
        }
        add(value);
    }

    /**
     * Add the value to this Option.  If the number of arguments
     * is greater than zero and there is enough space in the list then
     * add the value.  Otherwise, throw a runtime exception.
     * @param value the value to be added to this Option
     */
    private void add(String value) {
        if (!acceptsValue()) {
            throw new RuntimeException("Cannot add value, list full");
        }

        // store value
        values.add(value);
    }

    /**
     * Returns the specified value of this Option or
     * {@code null} if there is no value.
     * @return the value/first value of this Option or
     *      {@code null} if there is no value
     */
    public String getValue() {
        return (hasNoValues() ? null : values.get(0));
    }

    /**
     * Returns the specified value of this Option or
     * {@code null} if there is no value.
     * @param index the index of the value to be returned.
     * @return the specified value of this Option or
     *      {@code null} if there is no value.
     * @throws IndexOutOfBoundsException if index is less than 1
     *      or greater than the number of the values for this Option
     */
    public String getValue(int index) throws IndexOutOfBoundsException {
        return (hasNoValues() ? null : values.get(index));
    }

    /**
     * Returns the value/first value of this Option or the
     * <code>defaultValue</code> if there is no value.
     * @param defaultValue the value to be returned if there
     *      is no value.
     * @return the value/first value of this Option or the
     *      <code>defaultValue</code> if there are no values
     */
    public String getValue(String defaultValue) {
        String value = getValue();
        return (value != null ? value : defaultValue);
    }

    /**
     * Return the values of this Option as a String array
     * or null if there are no values.
     * @return the values of this Option as a String array
     *      or null if there are no values
     */
    public String[] getValues() {
        return (hasNoValues() ? null : values.toArray(new String[0]));
    }

    /**
     * Returns the values of this Option as a List
     * or null if there are no values.
     * @return the values of this Option as a List
     *      or null if there are no values
     */
    public List<String> getValuesList() {
        return values;
    }

    /**
     * Returns whether this Option has any values.
     * @return true if this Option has no value; false otherwise
     */
    private boolean hasNoValues() {
        return values.isEmpty();
    }

    /**
     * Query to see if this Option is mandatory
     * @return boolean flag indicating whether this Option is mandatory
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Sets whether this Option is mandatory.
     * @param required specifies whether this Option is mandatory
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Retrieve the self-documenting description of this Option
     * @return the string description of this option
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the self-documenting description of this Option
     * @param description the description of this option
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Option option = (Option)o;
        if (!Objects.equals(name, option.name)) {
            return false;
        }
        if (!Objects.equals(longName, option.longName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 1);
        result = 31 * result + (longName != null ? longName.hashCode() : 0);
        return result;
    }

    /**
     * A rather odd clone method - due to incorrect code in 1.0 it is public
     * and in 1.1 rather than throwing a CloneNotSupportedException it throws
     * a RuntimeException so as to maintain backwards compat at the API level.
     *
     * After calling this method, it is very likely you will want to call
     * clearValues().
     * @return a clone of this Option instance
     * @throws RuntimeException if a {@link CloneNotSupportedException} has been thrown
     *      by {@code super.clone()}
     */
    @Override
    public Option clone() {
        try {
            Option option = (Option)super.clone();
            option.values = new ArrayList<>(values);
            return option;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("A CloneNotSupportedException was thrown: " + e.getMessage());
        }
    }

    /**
     * Dump state, suitable for debugging.
     * @return the stringified form of this object
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder().append("[ option: ");
        buf.append(name);
        if (longName != null) {
            buf.append(" ").append(longName);
        }
        buf.append(" ");
        if (hasValues()) {
            buf.append("[ARG...]");
        } else if (hasValue()) {
            buf.append(" [ARG]");
        }
        buf.append(" :: ").append(description);
        if (valueType != null) {
            buf.append(" :: ").append(valueType);
        }
        buf.append(" ]");
        return buf.toString();
    }

    /**
     * Clear the Option values. After a parse is complete, these are left with
     * data in them and they need clearing if another parse is done.
     *
     * See: <a href="https://issues.apache.org/jira/browse/CLI-71">CLI-71</a>
     */
    void clearValues() {
        values.clear();
    }

    /**
     * Tells if the option can accept more arguments.
     * 
     * @return false if the maximum number of arguments is reached
     */
    boolean acceptsValue() {
        return (hasValue() && (numberOfValues <= 0 || values.size() < numberOfValues));
    }

    /**
     * Tells if the option requires more arguments to be valid.
     * 
     * @return false if the option doesn't require more arguments
     */
    boolean requiresValue() {
        if (optionalValue) {
            return false;
        }
        if (numberOfValues == UNLIMITED_VALUES) {
            return values.isEmpty();
        }
        return acceptsValue();
    }
    
    /**
     * Returns a {@link Builder} to create an {@link Option} using descriptive
     * methods.  
     * 
     * @return a new {@link Builder} instance
     */
    public static Builder builder() {
        return builder(null);
    }
    
    /**
     * Returns a {@link Builder} to create an {@link Option} using descriptive
     * methods.  
     * @param name short representation of the option
     * @return a new {@link Builder} instance
     * @throws IllegalArgumentException if there are any non valid Option characters in {@code name}
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }
    
    /**
     * A nested builder class to create <code>Option</code> instances
     * using descriptive methods.
     * <p>
     * Example usage:
     * <pre>
     * Option option = Option.builder("a")
     *     .required(true)
     *     .longName("arg-name")
     *     .build();
     * </pre>
     */
    public static final class Builder {

        /** The name of the option */
        private final String name;

        /** The long representation of the option */
        private String longName;

        /** Description of the option */
        private String description;

        /** The name of the argument value for this option */
        private String valueName;

        private boolean withEqualSign;

        /** Specifies whether this option is required to be present */
        private boolean required;

        /** Specifies whether the argument value of this Option is optional */
        private boolean optionalValue;

        /** The number of argument values this option can have */
        private int numberOfValues = UNINITIALIZED;

        /** The type of this Option */
        private OptionValueType valueType = OptionValueType.STRING;

        /**
         * Constructs a new {@code Builder} with the minimum
         * required parameters for an {@code Option} instance.
         * 
         * @param name short representation of the option
         * @throws IllegalArgumentException if there are any non valid Option characters in {@code name}
         */
        private Builder(String name) throws IllegalArgumentException {
            OptionUtils.validateOption(name);
            this.name = name;
        }

        /**
         * Sets the long name of the Option.
         *
         * @param longName the long name of the Option
         * @return this builder, to allow method chaining
         */
        public Builder longName(String longName) {
            this.longName = longName;
            return this;
        }

        /**
         * Sets the display name for the argument value.
         *
         * @param valueName the display name for the argument value
         * @return this builder, to allow method chaining
         */
        public Builder valueName(String valueName) {
            this.valueName = valueName;
            return this;
        }

        /**
         * Sets the type of the Option.
         *
         * @param valueType the type of the Option
         * @return this builder, to allow method chaining
         */
        public Builder valueType(OptionValueType valueType) {
            this.valueType = valueType;
            return this;
        }

        /**
         * The Option will use '=' as a means to separate argument value.
         *
         * @return this builder, to allow method chaining
         */
        public Builder withEqualSign() {
            this.withEqualSign = true;
            return hasValue();
        }

        /**
         * Indicates that the Option will require an argument.
         *
         * @return this builder, to allow method chaining
         */
        public Builder hasValue() {
            return hasValue(true);
        }

        /**
         * Indicates if the Option has an argument value or not.
         *
         * @param hasValue specifies whether the Option takes an argument value or not
         * @return this builder, to allow method chaining
         */
        public Builder hasValue(boolean hasValue) {
            // set to UNINITIALIZED when no arg is specified to be compatible with OptionBuilder
            numberOfValues = (hasValue ? 1 : UNINITIALIZED);
            return this;
        }

        /**
         * Indicates that the Option can have unlimited argument values.
         *
         * @return this builder, to allow method chaining
         */
        public Builder hasValues() {
            numberOfValues = UNLIMITED_VALUES;
            return this;
        }

        /**
         * Sets the number of argument values the Option can take.
         *
         * @param numberOfValues the number of argument values
         * @return this builder, to allow method chaining
         */
        public Builder numberOfValues(int numberOfValues) {
            this.numberOfValues = numberOfValues;
            return this;
        }

        /**
         * Sets whether the Option can have an optional argument value.
         *
         * @return this builder, to allow method chaining
         */
        public Builder optionalValue() {
            this.optionalValue = true;
            return this;
        }

        /**
         * Marks this Option as required.
         *
         * @return this builder, to allow method chaining
         */
        public Builder required() {
            return required(true);
        }

        /**
         * Sets whether the Option is mandatory.
         *
         * @param required specifies whether the Option is mandatory
         * @return this builder, to allow method chaining
         */
        public Builder required(boolean required) {
            this.required = required;
            return this;
        }

        /**
         * Sets the description for this option.
         *
         * @param description the description of the option
         * @return this builder, to allow method chaining
         */
        public Builder desc(String description) {
            this.description = description;
            return this;
        }

        /**
         * Constructs an Option with the values declared by this {@link Builder}.
         * 
         * @return the new {@link Option}
         * @throws IllegalArgumentException if neither {@code name} or {@code longName} has been set
         */
        public Option build() {
            if (name == null && longName == null) {
                throw new IllegalArgumentException("Either name or longName must be specified");
            }
            return new Option(this);
        }
    }

}
