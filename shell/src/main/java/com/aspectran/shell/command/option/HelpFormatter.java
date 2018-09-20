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

import com.aspectran.shell.console.Console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * A formatter of help messages for command line options.
 *
 * <p>Example:</p>
 * 
 * <pre>
 * Options options = new Options();
 * options.addOption(OptionBuilder.withLongOpt("file")
 *                                .withDescription("The file to be processed")
 *                                .hasArg()
 *                                .withArgName("FILE")
 *                                .isRequired()
 *                                .create('f'));
 * options.addOption(OptionBuilder.withLongOpt("version")
 *                                .withDescription("Print the version of the application")
 *                                .create('v'));
 * options.addOption(OptionBuilder.withLongOpt("help").create('h'));
 * 
 * String header = "Do something useful with an input file\n\n";
 * String footer = "\nPlease report issues at http://example.com/issues";
 * 
 * HelpFormatter formatter = new HelpFormatter();
 * formatter.printHelp("myapp", header, options, footer, true);
 * </pre>
 * 
 * This produces the following output:
 * 
 * <pre>
 * usage: myapp -f &lt;FILE&gt; [-h] [-v]
 * Do something useful with an input file
 * 
 *  -f,--file &lt;FILE&gt;   The file to be processed
 *  -h,--help
 *  -v,--version       Print the version of the application
 * 
 * Please report issues at http://example.com/issues
 * </pre>
 */
public class HelpFormatter {

    /** Default number of characters per line */
    public static final int DEFAULT_WIDTH = 74;

    /** Default padding to the left of each line */
    public static final int DEFAULT_LEFT_PAD = 3;

    /** number of space characters to be prefixed to each description line */
    public static final int DEFAULT_DESC_PAD = 3;

    /** the string to display at the beginning of the usage statement */
    private static final String DEFAULT_SYNTAX_PREFIX = "usage: ";

    /** Default prefix for shortOpts */
    private static final String DEFAULT_OPT_PREFIX = "-";

    /** Default prefix for long Option */
    private static final String DEFAULT_LONG_OPT_PREFIX = "--";

    /** Default separator displayed between a long Option and its value */
    private static final String DEFAULT_LONG_OPT_SEPARATOR = " ";

    /** Default name for an argument */
    private static final String DEFAULT_ARG_NAME = "arg";

    private int defaultWidth = DEFAULT_WIDTH;

    private int defaultLeftPad = DEFAULT_LEFT_PAD;

    private int defaultDescPad = DEFAULT_DESC_PAD;

    private String defaultSyntaxPrefix = DEFAULT_SYNTAX_PREFIX;

    private String defaultNewLine = System.getProperty("line.separator");

    private String defaultOptPrefix = DEFAULT_OPT_PREFIX;

    private String defaultLongOptPrefix = DEFAULT_LONG_OPT_PREFIX;

    private String defaultArgName = DEFAULT_ARG_NAME;

    /**
     * Comparator used to sort the options when they output in help text.
     * Defaults to case-insensitive alphabetical sorting by option key.
     */
    protected Comparator<Option> optionComparator = new OptionComparator();

    /** The separator displayed between the long option and its value. */
    private String longOptSeparator = DEFAULT_LONG_OPT_SEPARATOR;

    private final Console console;

    /**
     * Creates a help formatter.
     *
     * @param console the console to which the help will be written
     */
    public HelpFormatter(Console console) {
        this.console = console;
    }

    /**
     * Sets the 'width'.
     *
     * @param width the new value of 'width'
     */
    public void setWidth(int width) {
        this.defaultWidth = width;
    }

    /**
     * Returns the 'width'.
     *
     * @return the 'width'
     */
    public int getWidth() {
        return defaultWidth;
    }

    /**
     * Sets the 'leftPadding'.
     *
     * @param padding the new value of 'leftPadding'
     */
    public void setLeftPadding(int padding) {
        this.defaultLeftPad = padding;
    }

    /**
     * Returns the 'leftPadding'.
     *
     * @return the 'leftPadding'
     */
    public int getLeftPadding() {
        return defaultLeftPad;
    }

    /**
     * Sets the 'descPadding'.
     *
     * @param padding the new value of 'descPadding'
     */
    public void setDescPadding(int padding) {
        this.defaultDescPad = padding;
    }

    /**
     * Returns the 'descPadding'.
     *
     * @return the 'descPadding'
     */
    public int getDescPadding() {
        return defaultDescPad;
    }

    /**
     * Sets the 'syntaxPrefix'.
     *
     * @param prefix the new value of 'syntaxPrefix'
     */
    public void setSyntaxPrefix(String prefix) {
        this.defaultSyntaxPrefix = prefix;
    }

    /**
     * Returns the 'syntaxPrefix'.
     *
     * @return the 'syntaxPrefix'
     */
    public String getSyntaxPrefix() {
        return defaultSyntaxPrefix;
    }

    /**
     * Sets the 'newLine'.
     *
     * @param newline the new value of 'newLine'
     */
    public void setNewLine(String newline) {
        this.defaultNewLine = newline;
    }

    /**
     * Returns the 'newLine'.
     *
     * @return the 'newLine'
     */
    public String getNewLine() {
        return defaultNewLine;
    }

    /**
     * Sets the 'optPrefix'.
     *
     * @param prefix the new value of 'optPrefix'
     */
    public void setOptPrefix(String prefix) {
        this.defaultOptPrefix = prefix;
    }

    /**
     * Returns the 'optPrefix'.
     *
     * @return the 'optPrefix'
     */
    public String getOptPrefix() {
        return defaultOptPrefix;
    }

    /**
     * Sets the 'longOptPrefix'.
     *
     * @param prefix the new value of 'longOptPrefix'
     */
    public void setLongOptPrefix(String prefix) {
        this.defaultLongOptPrefix = prefix;
    }

    /**
     * Returns the 'longOptPrefix'.
     *
     * @return the 'longOptPrefix'
     */
    public String getLongOptPrefix() {
        return defaultLongOptPrefix;
    }

    /**
     * Set the separator displayed between a long option and its value.
     * Ensure that the separator specified is supported by the parser used,
     * typically ' ' or '='.
     * 
     * @param longOptSeparator the separator, typically ' ' or '='.
     */
    public void setLongOptSeparator(String longOptSeparator) {
        this.longOptSeparator = longOptSeparator;
    }

    /**
     * Returns the separator displayed between a long option and its value.
     * 
     * @return the separator
     */
    public String getLongOptSeparator() {
        return longOptSeparator;
    }

    /**
     * Sets the 'argName'.
     *
     * @param name the new value of 'argName'
     */
    public void setArgName(String name) {
        this.defaultArgName = name;
    }

    /**
     * Returns the 'argName'.
     *
     * @return the 'argName'
     */
    public String getArgName() {
        return defaultArgName;
    }

    /**
     * Comparator used to sort the options when they output in help text.
     * Defaults to case-insensitive alphabetical sorting by option key.
     *
     * @return the {@link Comparator} currently in use to sort the options
     */
    public Comparator<Option> getOptionComparator() {
        return optionComparator;
    }

    /**
     * Set the comparator used to sort the options when they output in help text.
     * Passing in a null comparator will keep the options in the order they were declared.
     *
     * @param comparator the {@link Comparator} to use for sorting the options
     */
    public void setOptionComparator(Comparator<Option> comparator) {
        this.optionComparator = comparator;
    }

    /**
     * Print the help for {@code options} with the specified
     * command line syntax.  This method prints help information to
     * System.out.
     *
     * @param cmdLineSyntax the syntax for this application
     * @param options the Options instance
     */
    public void printHelp(String cmdLineSyntax, Options options) {
        printHelp(getWidth(), cmdLineSyntax, null, options, null, false);
    }

    /**
     * Print the help for {@code options} with the specified
     * command line syntax.  This method prints help information to
     * System.out.
     *
     * @param cmdLineSyntax the syntax for this application
     * @param options the Options instance
     * @param autoUsage whether to print an automatically generated usage statement
     */
    public void printHelp(String cmdLineSyntax, Options options, boolean autoUsage) {
        printHelp(getWidth(), cmdLineSyntax, null, options, null, autoUsage);
    }

    /**
     * Print the help for {@code options} with the specified
     * command line syntax.  This method prints help information to
     * System.out.
     *
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the beginning of the help
     * @param options the Options instance
     * @param footer the banner to display at the end of the help
     */
    public void printHelp(String cmdLineSyntax, String header, Options options, String footer) {
        printHelp(cmdLineSyntax, header, options, footer, false);
    }

    /**
     * Print the help for {@code options} with the specified
     * command line syntax.  This method prints help information to
     * System.out.
     *
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the beginning of the help
     * @param options the Options instance
     * @param footer the banner to display at the end of the help
     * @param autoUsage whether to print an automatically generated usage statement
     */
    public void printHelp(String cmdLineSyntax, String header, Options options, String footer, boolean autoUsage) {
        printHelp(getWidth(), cmdLineSyntax, header, options, footer, autoUsage);
    }

    /**
     * Print the help for {@code options} with the specified
     * command line syntax.  This method prints help information to
     * System.out.
     *
     * @param width the number of characters to be displayed on each line
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the beginning of the help
     * @param options the Options instance
     * @param footer the banner to display at the end of the help
     */
    public void printHelp(int width, String cmdLineSyntax, String header, Options options, String footer) {
        printHelp(width, cmdLineSyntax, header, options, footer, false);
    }

    /**
     * Print the help for {@code options} with the specified
     * command line syntax.  This method prints help information to
     * System.out.
     *
     * @param width the number of characters to be displayed on each line
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the beginning of the help
     * @param options the Options instance
     * @param footer the banner to display at the end of the help
     * @param autoUsage whether to print an automatically generated usage statement
     */
    public void printHelp(int width, String cmdLineSyntax, String header,
                          Options options, String footer, boolean autoUsage) {
        printHelp(width, cmdLineSyntax, header, options, getLeftPadding(), getDescPadding(), footer, autoUsage);
    }

    /**
     * Print the help for {@code options} with the specified
     * command line syntax.
     *
     * @param width the number of characters to be displayed on each line
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the beginning of the help
     * @param options the Options instance
     * @param leftPad the number of characters of padding to be prefixed to each line
     * @param descPad the number of characters of padding to be prefixed to each description line
     * @param footer the banner to display at the end of the help
     * @throws IllegalStateException if there is no room to print a line
     */
    public void printHelp(int width, String cmdLineSyntax,
                          String header, Options options, int leftPad,
                          int descPad, String footer) {
        printHelp(width, cmdLineSyntax, header, options, leftPad, descPad, footer, false);
    }


    /**
     * Print the help for {@code options} with the specified
     * command line syntax.
     *
     * @param width the number of characters to be displayed on each line
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the beginning of the help
     * @param options the Options instance
     * @param leftPad the number of characters of padding to be prefixed to each line
     * @param descPad the number of characters of padding to be prefixed to each description line
     * @param footer the banner to display at the end of the help
     * @param autoUsage whether to print an automatically generated usage statement
     * @throws IllegalStateException if there is no room to print a line
     */
    public void printHelp(int width, String cmdLineSyntax,
                          String header, Options options, int leftPad,
                          int descPad, String footer, boolean autoUsage) {
        if (cmdLineSyntax == null || cmdLineSyntax.length() == 0) {
            throw new IllegalArgumentException("cmdLineSyntax not provided");
        }
        if (autoUsage) {
            printUsage(width, cmdLineSyntax, options);
        } else {
            printUsage(width, cmdLineSyntax);
        }
        if (header != null && header.trim().length() > 0) {
            printWrapped(width, header);
        }
        if (!options.isEmpty()) {
            printOptions(width, options, leftPad, descPad);
        }
        if (footer != null && footer.trim().length() > 0) {
            printWrapped(width, footer);
        }
    }

    /**
     * Prints the usage statement for the specified command.
     *
     * @param width the number of characters to display per line
     * @param commandName the command name
     * @param options the command line Options
     */
    public void printUsage(int width, String commandName, Options options) {
        // initialise the string builder
        StringBuilder sb = new StringBuilder(getSyntaxPrefix()).append(commandName).append(" ");

        // create a list for processed option groups
        Collection<OptionGroup> processedGroups = new ArrayList<>();

        List<Option> optList = new ArrayList<>(options.getOptions());
        if (optList.size() > 1 && getOptionComparator() != null) {
            optList.sort(getOptionComparator());
        }

        // iterate over the options
        for (Iterator<Option> it = optList.iterator(); it.hasNext();) {
            // get the next Option
            Option option = it.next();

            // check if the option is part of an OptionGroup
            OptionGroup group = options.getOptionGroup(option);

            // if the option is part of a group 
            if (group != null) {
                // and if the group has not already been processed
                if (!processedGroups.contains(group)) {
                    // add the group to the processed list
                    processedGroups.add(group);

                    // add the usage clause
                    appendOptionGroup(sb, group);
                }

                // otherwise the option was displayed in the group
                // previously so ignore it.
            }
            // if the Option is not part of an OptionGroup
            else {
                appendOption(sb, option, option.isRequired());
            }

            if (it.hasNext()) {
                sb.append(" ");
            }
        }

        // call printWrapped
        printWrapped(width, sb.toString().indexOf(' ') + 1, sb.toString());
    }

    /**
     * Appends the usage clause for an OptionGroup to a StringBuilder.  
     * The clause is wrapped in square brackets if the group is required.
     * The display of the options is handled by appendOption.
     *
     * @param sb the StringBuilder to append to
     * @param group the group to append
     * @see #appendOption(StringBuilder, Option, boolean)
     */
    private void appendOptionGroup(StringBuilder sb, OptionGroup group) {
        if (!group.isRequired()) {
            sb.append("[");
        }

        List<Option> optList = new ArrayList<>(group.getOptions());
        if (optList.size() > 1 && getOptionComparator() != null) {
            optList.sort(getOptionComparator());
        }

        // for each option in the OptionGroup
        for (Iterator<Option> it = optList.iterator(); it.hasNext();) {
            // whether the option is required or not is handled at group level
            appendOption(sb, it.next(), true);
            if (it.hasNext()) {
                sb.append(" | ");
            }
        }

        if (!group.isRequired()) {
            sb.append("]");
        }
    }

    /**
     * Appends the usage clause for an Option to a StringBuilder.  
     *
     * @param sb the StringBuilder to append to
     * @param option the Option to append
     * @param required whether the Option is required or not
     */
    private void appendOption(StringBuilder sb, Option option, boolean required) {
        if (!required) {
            sb.append("[");
        }

        if (option.getOpt() != null) {
            sb.append("-").append(option.getOpt());
        } else {
            sb.append("--").append(option.getLongOpt());
        }
        
        // if the Option has a value and a non blank arg name
        if (option.hasArg() && (option.getArgName() == null || option.getArgName().length() != 0)) {
            sb.append(option.getOpt() == null ? longOptSeparator : " ");
            sb.append("<").append(option.getArgName() != null ? option.getArgName() : getArgName()).append(">");
        }
        
        // if the Option is not a required option
        if (!required) {
            sb.append("]");
        }
    }

    /**
     * Print the cmdLineSyntax to the specified writer, using the
     * specified width.
     *
     * @param width the number of characters per line for the usage statement
     * @param cmdLineSyntax the usage statement
     */
    public void printUsage(int width, String cmdLineSyntax) {
        int argPos = cmdLineSyntax.indexOf(' ') + 1;
        printWrapped(width, getSyntaxPrefix().length() + argPos, getSyntaxPrefix() + cmdLineSyntax);
    }

    /**
     * Print the help for the specified Options to the specified writer, 
     * using the specified width, left padding and description padding.
     *
     * @param width the number of characters to display per line
     * @param options the command line Options
     * @param leftPad the number of characters of padding to be prefixed to each line
     * @param descPad the number of characters of padding to be prefixed to each description line
     */
    public void printOptions(int width, Options options, int leftPad, int descPad) {
        StringBuilder sb = new StringBuilder();
        renderOptions(sb, width, options, leftPad, descPad);
        console.writeLine(sb.toString());
    }

    /**
     * Print the specified text to the specified PrintWriter.
     *
     * @param width the number of characters to display per line
     * @param text the text to be written to the PrintWriter
     */
    public void printWrapped(int width, String text) {
        printWrapped(width, 0, text);
    }

    /**
     * Print the specified text to the specified PrintWriter.
     *
     * @param width the number of characters to display per line
     * @param nextLineTabStop the position on the next line for the first tab
     * @param text the text to be written to the PrintWriter
     */
    public void printWrapped(int width, int nextLineTabStop, String text) {
        StringBuilder sb = new StringBuilder(text.length());
        renderWrappedTextBlock(sb, width, nextLineTabStop, text);
        console.writeLine(sb.toString());
    }

    /**
     * Render the specified Options and return the rendered Options
     * in a StringBuilder.
     *
     * @param sb the StringBuilder to place the rendered Options into
     * @param width the number of characters to display per line
     * @param options the command line Options
     * @param leftPad the number of characters of padding to be prefixed to each line
     * @param descPad the number of characters of padding to be prefixed to each description line
     * @return the StringBuilder with the rendered Options contents
     */
    protected StringBuilder renderOptions(StringBuilder sb, int width, Options options, int leftPad, int descPad) {
        String lpad = OptionUtils.createPadding(leftPad);
        String dpad = OptionUtils.createPadding(descPad);

        // first create list containing only <lpad>-a,--aaa where 
        // -a is opt and --aaa is long opt; in parallel look for 
        // the longest opt string this list will be then used to 
        // sort options ascending
        int max = 0;
        List<StringBuilder> prefixList = new ArrayList<>();
        List<Option> optList = options.helpOptions();
        if (optList.size() > 1 && getOptionComparator() != null) {
            optList.sort(getOptionComparator());
        }
        for (Option option : optList) {
            StringBuilder optBuf = new StringBuilder();
            if (option.getOpt() == null) {
                optBuf.append(lpad).append("   ").append(getLongOptPrefix()).append(option.getLongOpt());
            } else {
                optBuf.append(lpad).append(getOptPrefix()).append(option.getOpt());

                if (option.hasLongOpt()) {
                    optBuf.append(',').append(getLongOptPrefix()).append(option.getLongOpt());
                }
            }
            if (option.hasArg()) {
                final String argName = option.getArgName();
                if (argName != null && argName.length() == 0) {
                    // if the option has a blank arg name
                    optBuf.append(' ');
                } else {
                    optBuf.append(option.hasLongOpt() ? longOptSeparator : " ");
                    optBuf.append("<").append(argName != null ? option.getArgName() : getArgName()).append(">");
                }
            }
            prefixList.add(optBuf);
            max = (optBuf.length() > max ? optBuf.length() : max);
        }

        int x = 0;
        for (Iterator<Option> it = optList.iterator(); it.hasNext();) {
            Option option = it.next();
            StringBuilder optBuf = new StringBuilder(prefixList.get(x++).toString());
            if (optBuf.length() < max) {
                optBuf.append(OptionUtils.createPadding(max - optBuf.length()));
            }
            optBuf.append(dpad);
            int nextLineTabStop = max + descPad;
            if (option.getDescription() != null) {
                optBuf.append(option.getDescription());
            }
            renderWrappedText(sb, width, nextLineTabStop, optBuf.toString());
            if (it.hasNext()) {
                sb.append(getNewLine());
            }
        }
        return sb;
    }

    /**
     * Render the specified text and return the rendered Options
     * in a StringBuilder.
     *
     * @param sb the StringBuilder to place the rendered text into
     * @param width the number of characters to display per line
     * @param nextLineTabStop the position on the next line for the first tab
     * @param text the text to be rendered
     * @return the StringBuilder with the rendered Options contents
     */
    protected StringBuilder renderWrappedText(StringBuilder sb, int width, int nextLineTabStop, String text) {
        int pos = OptionUtils.findWrapPos(text, width, 0);
        if (pos == -1) {
            sb.append(OptionUtils.rtrim(text));
            return sb;
        }

        sb.append(OptionUtils.rtrim(text.substring(0, pos))).append(getNewLine());

        if (nextLineTabStop >= width) {
            // stops infinite loop happening
            nextLineTabStop = 1;
        }

        // all following lines must be padded with nextLineTabStop space characters
        String padding = OptionUtils.createPadding(nextLineTabStop);
        while (true) {
            text = padding + text.substring(pos).trim();
            pos = OptionUtils.findWrapPos(text, width, 0);
            if (pos == -1) {
                sb.append(text);
                return sb;
            }
            if (text.length() > width && pos == nextLineTabStop - 1) {
                pos = width;
            }
            sb.append(OptionUtils.rtrim(text.substring(0, pos))).append(getNewLine());
        }
    }

    /**
     * Render the specified text width a maximum width. This method differs
     * from renderWrappedText by not removing leading spaces after a new line.
     *
     * @param sb the StringBuilder to place the rendered text into
     * @param width the number of characters to display per line
     * @param nextLineTabStop the position on the next line for the first tab
     * @param text the text to be rendered
     */
    private Appendable renderWrappedTextBlock(StringBuilder sb, int width, int nextLineTabStop, String text) {
        try {
            BufferedReader in = new BufferedReader(new StringReader(text));
            String line;
            boolean firstLine = true;
            while ((line = in.readLine()) != null) {
                if (!firstLine) {
                    sb.append(getNewLine());
                } else {
                    firstLine = false;
                }
                renderWrappedText(sb, width, nextLineTabStop, line);
            }
        } catch (IOException e) {
            // ignore
        }
        return sb;
    }

    /**
     * This class implements the {@code Comparator} interface
     * for comparing Options.
     */
    private static class OptionComparator implements Comparator<Option>, Serializable {

        private static final long serialVersionUID = -4277822882012181887L;

        /**
         * Compares its two arguments for order. Returns a negative
         * integer, zero, or a positive integer as the first argument
         * is less than, equal to, or greater than the second.
         *
         * @param opt1 the first Option to be compared
         * @param opt2 the second Option to be compared
         * @return a negative integer, zero, or a positive integer as
         *         the first argument is less than, equal to, or greater than the
         *         second
         */
        @Override
        public int compare(Option opt1, Option opt2) {
            return opt1.getKey().compareToIgnoreCase(opt2.getKey());
        }

    }

}
