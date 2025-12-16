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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * The default command option parser.
 */
public class DefaultOptionParser implements OptionParser {

    /** The parsed options instance. */
    private ParsedOptions parsedOptions;

    /** The current options. */
    private Options options;

    /**
     * Flag indicating how unrecognized tokens are handled. {@code true} to stop
     * the parsing and add the remaining tokens to the args list.
     * {@code false} to throw an exception.
     */
    private boolean skipParsingAtNonOption;

    /** The token currently processed. */
    private String currentToken;

    /** The last option parsed. */
    private Option currentOption;

    /** The required options and groups expected to be found when parsing the command line. */
    private List<Object> expectedOpts;

    /** Flag indicating if partial matching of long options is supported. */
    private final boolean allowPartialMatching;

    /**
     * Creates a new DefaultParser instance with partial matching enabled.
     * <p>By "partial matching" we mean that given the following code:
     * <pre>
     *     {@code
     *     Options options = new Options();
     *     options.addOption(new Option("d", "debug", false, "Turn on debug."));
     *     options.addOption(new Option("e", "extract", false, "Turn on extract."));
     *     options.addOption(new Option("o", "option", true, "Turn on option with argument."));
     *     }
     * </pre></p>
     * with "partial matching" turned on, {@code -de} only matches the
     * {@code "debug"} option. However, with "partial matching" disabled,
     * {@code -de} would enable both {@code debug} as well as
     * {@code extract} options.
     */
    public DefaultOptionParser() {
        this(false);
    }

    /**
     * Create a new DefaultParser instance with the specified partial matching policy.
     * <p>
     * By "partial matching" we mean that given the following code:
     * <pre>
     *     {@code
     *          Options options = new Options();
     *      options.addOption(new Option("d", "debug", false, "Turn on debug."));
     *      options.addOption(new Option("e", "extract", false, "Turn on extract."));
     *      options.addOption(new Option("o", "option", true, "Turn on option with argument."));
     *      }
     * </pre>
     * with "partial matching" turned on, {@code -de} only matches the
     * {@code "debug"} option. However, with "partial matching" disabled,
     * {@code -de} would enable both {@code debug} as well as
     * {@code extract} options.
     * @param allowPartialMatching if partial matching of long options shall be enabled
     */
    public DefaultOptionParser(boolean allowPartialMatching) {
        this.allowPartialMatching = allowPartialMatching;
    }

    public ParsedOptions parse(Options options, String[] args) throws OptionParserException {
        return parse(options, args, null);
    }

    /**
     * Parse the arguments according to the specified options and properties.
     * @param options the specified Options
     * @param args the command line arguments
     * @param properties command line option name-value pairs
     * @return the list of atomic option and value tokens
     * @throws OptionParserException if there are any problems encountered
     *      while parsing the command line tokens
     */
    public ParsedOptions parse(Options options, String[] args, Properties properties)
            throws OptionParserException {
        return parse(options, args, properties, false);
    }

    public ParsedOptions parse(Options options, String[] args, boolean skipParsingAtNonOption)
            throws OptionParserException {
        return parse(options, args, null, skipParsingAtNonOption);
    }

    /**
     * Parse the arguments according to the specified options and properties.
     * @param options the specified Options
     * @param args the command line arguments
     * @param properties command line option name-value pairs
     * @param skipParsingAtNonOption if {@code true} an unrecognized argument stops
     *     the parsing and the remaining arguments are added to the
     *     {@link ParsedOptions}s args list. If {@code false} an unrecognized
     *     argument triggers a ParseException.
     * @return the list of atomic option and value tokens
     * @throws OptionParserException if there are any problems encountered
     *      while parsing the command line tokens
     */
    public ParsedOptions parse(@NonNull Options options, String[] args, Properties properties,
                               boolean skipParsingAtNonOption)
            throws OptionParserException {
        this.options = options;
        this.skipParsingAtNonOption = skipParsingAtNonOption;
        this.currentOption = null;
        this.expectedOpts = new ArrayList<>(options.getRequiredOptions());

        // clear the data from the groups
        for (OptionGroup group : options.getOptionGroups()) {
            group.setSelected(null);
        }

        this.parsedOptions = new ParsedOptions();

        if (args != null) {
            for (String argument : args) {
                handleToken(argument);
            }
        }

        // check the arguments of the last option
        checkRequiredOptionValues();

        // add the default options
        handleProperties(properties);

        checkRequiredOptions();

        return this.parsedOptions;
    }

    /**
     * Sets the values of Options using the values in {@code properties}.
     * @param properties the value properties to be processed
     * @throws OptionParserException if option parsing fails
     */
    private void handleProperties(Properties properties) throws OptionParserException {
        if (properties == null) {
            return;
        }

        for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements();) {
            String name = e.nextElement().toString();
            Option opt = options.getOption(name);
            if (opt == null) {
                throw new UnrecognizedOptionException("Default option wasn't defined", name);
            }

            // if the option is part of a group, check if another option of the group has been selected
            OptionGroup group = options.getOptionGroup(opt);
            boolean selected = (group != null && group.getSelected() != null);
            if (!parsedOptions.hasOption(name) && !selected) {
                // get the value from the properties
                String value = properties.getProperty(name);
                if (opt.hasValue()) {
                    if (opt.getValues() == null || opt.getValues().length == 0) {
                        opt.addValue(value);
                    }
                } else if (!("yes".equalsIgnoreCase(value)
                        || "true".equalsIgnoreCase(value)
                        || "1".equalsIgnoreCase(value))) {
                    // if the value is not yes, true or 1 then don't add the option to the ParsedOptions
                    continue;
                }
                handleOption(opt);
                currentOption = null;
            }
        }
    }

    /**
     * Handle any command line token.
     * @param token the command line token to handle
     * @throws OptionParserException if option parsing fails
     */
    private void handleToken(String token) throws OptionParserException {
        currentToken = token;
        if (!"--".equals(token)) {
            if (currentOption != null && currentOption.acceptsValue() &&
                    !currentOption.isWithEqualSign() && isArgument(token)) {
                String t = OptionUtils.stripLeadingAndTrailingQuotes(token);
                currentOption.addValue(t);
            } else if (token.startsWith("--")) {
                String t = OptionUtils.stripLeadingHyphens(token);
                handleLongOption(t);
            } else if (token.startsWith("-") && token.length() > 1) {
                String t = OptionUtils.stripLeadingHyphens(token);
                handleShortAndLongOption(t);
            } else {
                handleUnknownToken(token);
            }
        }
        if (currentOption != null && !currentOption.acceptsValue()) {
            currentOption = null;
        }
    }

    /**
     * Handles the following tokens:
     * <pre>
     * --L
     * --L=V
     * --L V
     * --l
     * </pre>
     * @param token the command line token to handle
     * @throws OptionParserException if option parsing fails
     */
    private void handleLongOption(@NonNull String token) throws OptionParserException {
        if (token.indexOf('=') == -1) {
            handleLongOptionWithoutEqual(token);
        } else {
            handleLongOptionWithEqual(token);
        }
    }

    /**
     * Handles the following tokens:
     * <pre>
     * --L
     * -L
     * --l
     * -l
     * </pre>
     * @param token the command line token to handle
     * @throws OptionParserException if option parsing fails
     */
    private void handleLongOptionWithoutEqual(String token) throws OptionParserException {
        List<String> matchingOpts = getMatchingLongOptions(token);
        if (matchingOpts.isEmpty()) {
            handleUnknownToken(currentToken);
        } else if (matchingOpts.size() > 1 && !options.hasLongOption(token)) {
            throw new AmbiguousOptionException(token, matchingOpts);
        } else {
            String key = (options.hasLongOption(token) ? token : matchingOpts.getFirst());
            handleOption(options.getOption(key));
        }
    }

    /**
     * Handles the following tokens:
     * <pre>
     * --L=V
     * -L=V
     * --l=V
     * -l=V
     * </pre>
     * @param token the command line token to handle
     * @throws OptionParserException if option parsing fails
     */
    private void handleLongOptionWithEqual(@NonNull String token) throws OptionParserException {
        int pos = token.indexOf('=');
        String name = token.substring(0, pos);
        String value = token.substring(pos + 1);
        List<String> matchingOpts = getMatchingLongOptions(name);
        if (matchingOpts.isEmpty()) {
            handleUnknownToken(currentToken);
        } else if (matchingOpts.size() > 1 && !options.hasLongOption(name)) {
            throw new AmbiguousOptionException(name, matchingOpts);
        } else {
            String key = (options.hasLongOption(name) ? name : matchingOpts.getFirst());
            Option option = options.getOption(key);
            if (option.acceptsValue()) {
                handleOption(option);
                currentOption.addValue(value);
                currentOption = null;
            } else {
                handleUnknownToken(currentToken);
            }
        }
    }

    /**
     * Handles the following tokens:
     * <pre>
     * -S
     * -SV
     * -S V
     * -S=V
     * -S1S2
     * -S1S2 V
     * -SV1=V2
     *
     * -L
     * -LV
     * -L V
     * -L=V
     * -l
     * </pre>
     * @param token the command line token to handle
     * @throws OptionParserException if option parsing fails
     */
    private void handleShortAndLongOption(@NonNull String token) throws OptionParserException {
        if (token.length() == 1) {
            // -S
            if (options.hasShortOption(token)) {
                handleOption(options.getOption(token));
            } else {
                handleUnknownToken(currentToken);
            }
            return;
        }
        int pos = token.indexOf('=');
        if (pos == -1) {
            // no equal sign found (-xxx)
            if (options.hasShortOption(token)) {
                handleOption(options.getOption(token));
            } else if (!getMatchingLongOptions(token).isEmpty()) {
                // -L or -l
                handleLongOptionWithoutEqual(token);
            } else {
                // look for a long prefix (-Xmx512m)
                String name = getLongPrefix(token);
                if (name != null) {
                    Option option = options.getOption(name);
                    if (!option.isWithEqualSign() && option.acceptsValue()) {
                        handleOption(options.getOption(name));
                        currentOption.addValue(token.substring(name.length()));
                        currentOption = null;
                        return;
                    }
                }
                handleUnknownToken(currentToken);
            }
        } else {
            // equal sign found (-xxx=yyy)
            String name = token.substring(0, pos);
            String value = token.substring(pos + 1);
            // -S=V
            Option option = options.getOption(name);
            if (option != null && option.acceptsValue()) {
                handleOption(option);
                currentOption.addValue(value);
                currentOption = null;
            } else {
                // -L=V or -l=V
                handleLongOptionWithEqual(token);
            }
        }
    }

    /**
     * Handles an unknown token. If the token starts with a dash an
     * UnrecognizedOptionException is thrown. Otherwise, the token is added
     * to the arguments of the command line. If the skipParsingAtNonOption flag
     * is set, this stops the parsing and the remaining tokens are added
     * as-is in the arguments of the command line.
     * @param token the command line token to handle
     * @throws OptionParserException if option parsing fails
     */
    private void handleUnknownToken(@NonNull String token) throws OptionParserException {
        if (token.startsWith("-") && token.length() > 1 && !skipParsingAtNonOption) {
            throw new UnrecognizedOptionException("Unrecognized option: " + token, token);
        }
        parsedOptions.addArg(token);
    }

    private void handleOption(Option option) throws OptionParserException {
        // check the previous option before handling the next one
        checkRequiredOptionValues();
        try {
            option = option.clone();
        } catch (CloneNotSupportedException e) {
            throw new OptionParserException("A CloneNotSupportedException was thrown: " + e.getMessage() + "; " +
                    "Class " + option.getClass() + " must implement the Cloneable interface");
        }
        updateRequiredOptions(option);
        parsedOptions.addOption(option);
        if (option.hasValue()) {
            currentOption = option;
        } else {
            currentOption = null;
        }
    }

    /**
     * Removes the option or its group from the list of expected elements.
     */
    private void updateRequiredOptions(@NonNull Option option) throws AlreadySelectedException {
        if (option.isRequired()) {
            expectedOpts.remove(option.getKey());
        }
        // if the option is in an OptionGroup make that option the selected option of the group
        if (options.getOptionGroup(option) != null) {
            OptionGroup group = options.getOptionGroup(option);
            if (group.isRequired()) {
                expectedOpts.remove(group);
            }
            group.setSelected(option);
        }
    }

    /**
     * Throws a {@link MissingOptionException} if all required options are not present.
     * @throws MissingOptionException if any of the required Options are not present
     */
    private void checkRequiredOptions() throws MissingOptionException {
        // if there are required options that have not been processed
        if (!expectedOpts.isEmpty()) {
            throw new MissingOptionException(expectedOpts);
        }
    }

    /**
     * Throw a {@link MissingOptionValueException} if the current option
     * didn't receive the number of values expected.
     */
    private void checkRequiredOptionValues() throws OptionParserException {
        if (currentOption != null && currentOption.requiresValue()) {
            throw new MissingOptionValueException(currentOption);
        }
    }

    /**
     * Returns true is the token is a valid argument.
     * @param token the command line token to handle
     * @return true if the token is a valid argument
     */
    private boolean isArgument(String token) {
        return (!isOption(token) || isNegativeNumber(token));
    }

    /**
     * Check if the token is a negative number.
     * @param token the command line token to handle
     * @return true if the token is a negative number
     */
    private boolean isNegativeNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Tells if the token looks like an option.
     * @param token the command line token to handle
     * @return true if the token looks like an option
     */
    private boolean isOption(String token) {
        return (isLongOption(token) || isShortOption(token));
    }

    /**
     * Tells if the token looks like a short option.
     * @param token the command line token to handle
     * @return true if the token like a short option
     */
    private boolean isShortOption(@NonNull String token) {
        // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
        if (!token.startsWith("-") || token.length() == 1) {
            return false;
        }
        // remove leading "-" and "=value"
        int pos = token.indexOf("=");
        String name = (pos == -1 ? token.substring(1) : token.substring(1, pos));
        if (options.hasShortOption(name)) {
            return true;
        }
        // check for several concatenated short options
        return (!name.isEmpty() && options.hasShortOption(String.valueOf(name.charAt(0))));
    }

    /**
     * Tells if the token looks like a long option.
     * @param token the command line token to handle
     * @return true if the token like a long option
     */
    private boolean isLongOption(@NonNull String token) {
        if (!token.startsWith("-") || token.length() == 1) {
            return false;
        }
        int pos = token.indexOf("=");
        String t = (pos == -1 ? token : token.substring(0, pos));
        if (!getMatchingLongOptions(t).isEmpty()) {
            // long or partial long options (--L, -L, --L=V, -L=V, --l, --l=V)
            return true;
        }
        if (getLongPrefix(token) != null && !token.startsWith("--")) {
            // -LV
            return true;
        }
        return false;
    }

    /**
     * Returns a list of matching option strings for the given token, depending
     * on the selected partial matching policy.
     * @param token the token (may contain leading dashes)
     * @return the list of matching option strings or an empty list if no
     *      matching option could be found
     */
    private List<String> getMatchingLongOptions(String token) {
        if (allowPartialMatching) {
            return options.getMatchingOptions(token);
        } else {
            List<String> matches = new ArrayList<>(1);
            if (options.hasLongOption(token)) {
                Option option = options.getOption(token);
                matches.add(option.getLongName());
            }
            return matches;
        }
    }

    /**
     * Search for a prefix that is the long name of an option (-Xmx512m).
     * @param token the command line token to handle
     */
    private String getLongPrefix(@NonNull String token) {
        String name = null;
        for (int i = token.length() - 2; i > 1; i--) {
            String prefix = token.substring(0, i);
            if (options.hasLongOption(prefix)) {
                name = prefix;
                break;
            }
        }
        return name;
    }

}
