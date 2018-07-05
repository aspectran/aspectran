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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Represents list of arguments parsed against a {@link Options} descriptor.
 *
 * <p>It allows querying of a boolean {@link #hasOption(String opt)},
 * in addition to retrieving the {@link #getValue(String opt)}
 * for options requiring arguments.</p>
 *
 * <p>Additionally, any left-over or unrecognized arguments,
 * are available for further processing.</p>
 */
public class ParsedOptions implements Serializable {

    /** @serial */
    private static final long serialVersionUID = -875791688751934582L;

    /** The processed options */
    private final List<Option> options = new ArrayList<>();

    /** The unrecognized options/arguments */
    private final List<String> args = new LinkedList<>();

    /**
     * Creates a command line.
     */
    protected ParsedOptions() {
        // Nothing to do
    }

    /**
     * Query to see if an option has been set.
     *
     * @param opt the option to check
     * @return true if set, false if not
     */
    public boolean hasOption(Option opt) {
        return options.contains(opt);
    }

    /**
     * Query to see if an option has been set.
     *
     * @param opt short name of the option
     * @return true if set, false if not
     */
    public boolean hasOption(String opt) {
        return hasOption(resolveOption(opt));
    }

    /**
     * Query to see if an option has been set.
     *
     * @param opt character name of the option
     * @return true if set, false if not
     */
    public boolean hasOption(char opt) {
        return hasOption(String.valueOf(opt));
    }

    /**
     * Checks if options exists.
     *
     * @return true if options exists; false otherwise
     */
    public boolean hasOptions() {
        return !options.isEmpty();
    }

    /**
     * Return a version of this {@code Option} converted to a particular type.
     *
     * @param <T> the generic type
     * @param option the option
     * @return the value parsed into a particular object
     * @throws OptionParserException if there are problems turning the option value into the desired type
     */
    @SuppressWarnings("unchecked")
    public <T> T getParsedValue(Option option) throws OptionParserException {
        if (option == null) {
            return null;
        }
        String value = getValue(option);
        if (value == null) {
            return null;
        }
        OptionValueType valueType = option.getValueType();
        if (valueType == OptionValueType.STRING) {
            return (T)value;
        } else if (valueType == OptionValueType.INT) {
            try {
                return (T)Integer.valueOf(value);
            } catch(NumberFormatException e) {
                throw new OptionParserException(e.getMessage());
            }
        } else if (valueType == OptionValueType.LONG) {
            try {
                return (T)Long.valueOf(value);
            } catch(NumberFormatException e) {
                throw new OptionParserException(e.getMessage());
            }
        } else if (valueType == OptionValueType.FLOAT) {
            try {
                return (T)Float.valueOf(value);
            } catch(NumberFormatException e) {
                throw new OptionParserException(e.getMessage());
            }
        } else if (valueType == OptionValueType.DOUBLE) {
            try {
                return (T)Double.valueOf(value);
            } catch(NumberFormatException e) {
                throw new OptionParserException(e.getMessage());
            }
        } else if (valueType == OptionValueType.BOOLEAN) {
            try {
                return (T)Boolean.valueOf(value);
            } catch(NumberFormatException e) {
                throw new OptionParserException(e.getMessage());
            }
        } else if (valueType == OptionValueType.FILE) {
            return (T)new File(value);
        } else {
            return null;
        }
    }

    /**
     * Return a version of this {@code Option} converted to a particular type.
     *
     * @param <T> the generic type
     * @param opt the name of the option
     * @return the value parsed into a particular object
     * @throws OptionParserException if there are problems turning the option value into the desired type
     */
    public <T> T getParsedValue(String opt) throws OptionParserException {
        return getParsedValue(resolveOption(opt));
    }

    /**
     * Return a version of this {@code Option} converted to a particular type.
     *
     * @param <T> the generic type
     * @param opt the name of the option
     * @return the value parsed into a particular object
     * @throws OptionParserException if there are problems turning the option value into the desired type
     */
    public <T> T getParsedValue(char opt) throws OptionParserException {
        return getParsedValue(String.valueOf(opt));
    }

    /**
     * Retrieve the first argument, if any, of this option.
     *
     * @param option the name of the option
     * @return the value of the argument if option is set, and has an argument,
     *      otherwise {@code null}
     */
    public String getValue(Option option) {
        if (option == null) {
            return null;
        }
        String[] values = getValues(option);
        return (values == null ? null : values[0]);
    }

    /**
     * Retrieve the first argument, if any, of this option.
     *
     * @param opt the name of the option
     * @return the value of the argument if option is set, and has an argument,
     *      otherwise {@code null}
     */
    public String getValue(String opt) {
        return getValue(resolveOption(opt));
    }

    /**
     * Retrieve the first argument, if any, of this option.
     *
     * @param opt the character name of the option
     * @return the value of the argument if option is set, and has an argument,
     *      otherwise {@code null}
     */
    public String getValue(char opt) {
        return getValue(String.valueOf(opt));
    }

    /**
     * Retrieves the array of values, if any, of an option.
     *
     * @param option string name of the option
     * @return the values of the argument if option is set, and has an argument,
     *      otherwise {@code null}
     */
    public String[] getValues(Option option) {
        List<String> values = new ArrayList<>();
        for (Option processedOption : options) {
            if (processedOption.equals(option)) {
                values.addAll(processedOption.getValuesList());
            }
        }
        return (values.isEmpty() ? null : values.toArray(new String[0]));
    }

    /**
     * Retrieves the array of values, if any, of an option.
     *
     * @param opt string name of the option
     * @return the values of the argument if option is set, and has an argument,
     *      otherwise {@code null}
     */
    public String[] getValues(String opt) {
        return getValues(resolveOption(opt));
    }

    /**
     * Retrieves the option object given the long or short option as a String.
     *
     * @param opt the short or long name of the option
     * @return the canonicalized option
     */
    private Option resolveOption(String opt) {
        opt = OptionUtils.stripLeadingHyphens(opt);
        for (Option option : options) {
            if (opt.equals(option.getOpt())) {
                return option;
            }
            if (opt.equals(option.getLongOpt())) {
                return option;
            }
        }
        return null;
    }

    /**
     * Retrieves the array of values, if any, of an option.
     *
     * @param opt character name of the option
     * @return Values of the argument if option is set, and has an argument,
     *      otherwise {@code null}
     */
    public String[] getValues(char opt) {
        return getValues(String.valueOf(opt));
    }

    /**
     * Retrieve the first argument, if any, of an option.
     *
     * @param option the name of the option
     * @param defaultValue the default value to be returned if the option
     *      is not specified
     * @return the value of the argument if option is set, and has an argument,
     *      otherwise {@code defaultValue}
     */
    public String getValue(Option option, String defaultValue) {
        String answer = getValue(option);
        return (answer != null ? answer : defaultValue);
    }

    /**
     * Retrieve the first argument, if any, of an option.
     *
     * @param opt the name of the option
     * @param defaultValue the default value to be returned if the option
     *      is not specified
     * @return the value of the argument if option is set, and has an argument,
     *      otherwise {@code defaultValue}
     */
    public String getValue(String opt, String defaultValue) {
        return getValue(resolveOption(opt), defaultValue);
    }

    /**
     * Retrieve the argument, if any, of an option.
     *
     * @param opt character name of the option
     * @param defaultValue the default value to be returned if the option
     *      is not specified
     * @return the value of the argument if option is set, and has an argument,
     *      otherwise {@code defaultValue}
     */
    public String getValue(char opt, String defaultValue) {
        return getValue(String.valueOf(opt), defaultValue);
    }

    /**
     * Retrieve the map of values associated to the option. This is convenient
     * for options specifying Java properties like <tt>-Dparam1=value1
     * -Dparam2=value2</tt>. The first argument of the option is the key, and
     * the 2nd argument is the value. If the option has only one argument
     * (<tt>-Dfoo</tt>) it is considered as a boolean flag and the value is
     * <tt>"true"</tt>.
     *
     * @param option the option to be processed
     * @return the Properties mapped by the option, never {@code null}
     *         even if the option doesn't exists
     */
    public Properties getProperties(Option option) {
        Properties props = new Properties();
        for (Option processedOption : options) {
            if (processedOption.equals(option)) {
                List<String> values = processedOption.getValuesList();
                if (values.size() >= 2) {
                    // use the first 2 arguments as the key/value pair
                    props.put(values.get(0), values.get(1));
                } else if (values.size() == 1) {
                    // no explicit value, handle it as a boolean
                    props.put(values.get(0), "true");
                }
            }
        }
        return props;
    }

    /**
     * Retrieve the map of values associated to the option. This is convenient
     * for options specifying Java properties like <tt>-Dparam1=value1
     * -Dparam2=value2</tt>. The first argument of the option is the key, and
     * the 2nd argument is the value. If the option has only one argument
     * (<tt>-Dfoo</tt>) it is considered as a boolean flag and the value is
     * <tt>"true"</tt>.
     *
     * @param opt the name of the option
     * @return the Properties mapped by the option, never {@code null}
     *         even if the option doesn't exists
     */
    public Properties getProperties(String opt) {
        Properties props = new Properties();
        for (Option option : options) {
            if (opt.equals(option.getOpt()) || opt.equals(option.getLongOpt())) {
                List<String> values = option.getValuesList();
                if (values.size() >= 2) {
                    // use the first 2 arguments as the key/value pair
                    props.put(values.get(0), values.get(1));
                } else if (values.size() == 1) {
                    // no explicit value, handle it as a boolean
                    props.put(values.get(0), "true");
                }
            }
        }
        return props;
    }

    /**
     * Add an option.
     * The values of the option are stored.
     *
     * @param opt the option to be processed
     */
    protected void addOption(Option opt) {
        options.add(opt);
    }

    /**
     * Returns an iterator over the Option members of ParsedOptions.
     *
     * @return an {@code Iterator} over the processed {@link Option}
     *      members of this {@link ParsedOptions}
     */
    public Iterator<Option> iterator() {
        return options.iterator();
    }

    /**
     * Returns an array of the processed {@link Option}s.
     *
     * @return an array of the processed {@link Option}s
     */
    public Option[] getOptions() {
        return options.toArray(new Option[0]);
    }

    /**
     * Checks if non-recognized options or arguments exists.
     *
     * @return true if non-recognized options or arguments exists; false otherwise
     */
    public boolean hasArgs() {
        return !args.isEmpty();
    }

    /**
     * Retrieve any left-over non-recognized options and arguments.
     *
     * @return remaining items passed in but not parsed as an array
     */
    public String[] getArgs() {
        return args.toArray(new String[0]);
    }

    /**
     * Retrieve any left-over non-recognized options and arguments.
     *
     * @return remaining items passed in but not parsed as a {@link List}
     */
    public List<String> getArgList() {
        return args;
    }

    /**
     * Add left-over unrecognized option/argument.
     *
     * @param arg the unrecognized option/argument
     */
    protected void addArg(String arg) {
        args.add(arg);
    }

}
