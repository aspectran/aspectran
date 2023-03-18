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

import com.aspectran.core.util.StringUtils;
import com.aspectran.shell.command.Command;
import com.aspectran.shell.console.ShellConsole;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A formatter of help messages for command line options.
 */
public class HelpFormatter {
    
    private static final String NEW_LINE = System.lineSeparator();

    /** Default number of characters per line */
    public static final int DEFAULT_WIDTH = 76;

    /** Default number of characters per line */
    public static final int DEFAULT_MAX_LEFT_WIDTH = 15;

    /** Default padding to the left of each line */
    public static final int DEFAULT_LEFT_PAD = 3;

    /** number of space characters to be prefixed to each description line */
    public static final int DEFAULT_DESC_PAD = 3;

    /** the string to display at the beginning of the usage statement */
    private static final String DEFAULT_SYNTAX_PREFIX = "Usage: ";

    /** Default name for an argument */
    private static final String DEFAULT_ARG_NAME = "arg";

    /** Default prefix for shortOpts */
    public static final String OPTION_PREFIX = "-";

    /** Default prefix for long Option */
    public static final String LONG_OPTION_PREFIX = "--";

    /** Default separator displayed between a long Option and its value */
    private static final String LONG_OPTION_SEPARATOR = " ";

    /** The opening pointy bracket to wrap an argument */
    private static final String ARG_BRACKET_OPEN = "<";

    /** The closing pointy bracket to wrap an argument */
    private static final String ARG_BRACKET_CLOSE = ">";

    /** The opening square bracket to indicate optional option or argument */
    private static final String OPTIONAL_BRACKET_OPEN = "[";

    /** The closing square bracket to indicate optional option or argument */
    private static final String OPTIONAL_BRACKET_CLOSE = "]";

    private int width = DEFAULT_WIDTH;

    private int maxLeftWidth = DEFAULT_MAX_LEFT_WIDTH;

    private int leftPad = DEFAULT_LEFT_PAD;

    private int descPad = DEFAULT_DESC_PAD;

    private String syntaxPrefix = DEFAULT_SYNTAX_PREFIX;

    private String argName = DEFAULT_ARG_NAME;

    /**
     * Comparator used to sort the options when they output in help text.
     * Defaults to case-insensitive alphabetical sorting by option key.
     */
    private Comparator<Option> optionComparator;

    private final ShellConsole console;

    /**
     * Creates a help formatter.
     * @param console the console to which the help will be written
     */
    public HelpFormatter(ShellConsole console) {
        this.console = console;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getMaxLeftWidth() {
        return maxLeftWidth;
    }

    public void setMaxLeftWidth(int maxLeftWidth) {
        this.maxLeftWidth = maxLeftWidth;
    }

    public int getLeftPad() {
        return leftPad;
    }

    public void setLeftPad(int leftPad) {
        this.leftPad = leftPad;
    }

    public int getDescPad() {
        return descPad;
    }

    public void setDescPad(int descPad) {
        this.descPad = descPad;
    }

    public String getSyntaxPrefix() {
        return syntaxPrefix;
    }

    public void setSyntaxPrefix(String prefix) {
        this.syntaxPrefix = prefix;
    }

    public String getArgName() {
        return argName;
    }

    public void setArgName(String name) {
        this.argName = name;
    }

    /**
     * Comparator used to sort the options when they output in help text.
     * Defaults to case-insensitive alphabetical sorting by option key.
     * @return the {@link Comparator} currently in use to sort the options
     */
    public Comparator<Option> getOptionComparator() {
        return optionComparator;
    }

    /**
     * Set the comparator used to sort the options when they output in help text.
     * Passing in a null comparator will keep the options in the order they were declared.
     * @param comparator the {@link Comparator} to use for sorting the options
     */
    public void setOptionComparator(Comparator<Option> comparator) {
        this.optionComparator = comparator;
    }

    /**
     * Print the help with the given Command object.
     * @param command the Command instance
     */
    public void printHelp(Command command) {
        if (command.getDescriptor().getUsage() != null) {
            printUsage(command.getDescriptor().getUsage());
        } else {
            printUsage(command);
        }
        int leftWidth = printOptions(command.getOptions());
        printArguments(command.getArgumentsList(), leftWidth);
    }

    /**
     * Print the help for {@code options} with the specified
     * command line syntax.
     * @param cmdLineSyntax the usage statement
     */
    public void printUsage(String cmdLineSyntax) {
        int nextLineTabStop = getSyntaxPrefix().length() + cmdLineSyntax.indexOf(' ') + 1;
        printWrapped( + nextLineTabStop, getSyntaxPrefix() + cmdLineSyntax);
    }

    /**
     * Prints the usage statement for the specified command.
     * @param command the Command instance
     */
    public void printUsage(Command command) {
        String commandName = command.getDescriptor().getName();
        StringBuilder sb = new StringBuilder(getSyntaxPrefix()).append(commandName).append(" ");

        // create a list for processed option groups
        Collection<OptionGroup> processedGroups = new ArrayList<>();

        Collection<Option> optList = command.getOptions().getAllOptions();
        if (optList.size() > 1 && getOptionComparator() != null) {
            List<Option> optList2 = new ArrayList<>(optList);
            optList2.sort(getOptionComparator());
            optList = optList2;
        }

        for (Iterator<Option> it = optList.iterator(); it.hasNext();) {
            // get the next Option
            Option option = it.next();
            // check if the option is part of an OptionGroup
            OptionGroup group = command.getOptions().getOptionGroup(option);
            if (group != null) {
                // if the group has not already been processed
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

        for (Arguments arguments : command.getArgumentsList()) {
            sb.append(" ");
            appendArguments(sb, arguments);
        }

        printWrapped(getSyntaxPrefix().length() + commandName.length() + 1, sb.toString());
    }

    /**
     * Appends the usage clause for an OptionGroup to a StringBuilder.  
     * The clause is wrapped in square brackets if the group is required.
     * The display of the options is handled by appendOption.
     * @param sb the StringBuilder to append to
     * @param group the group to append
     */
    private void appendOptionGroup(StringBuilder sb, OptionGroup group) {
        if (!group.isRequired()) {
            sb.append(OPTIONAL_BRACKET_OPEN);
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
            sb.append(OPTIONAL_BRACKET_CLOSE);
        }
    }

    /**
     * Appends the usage clause for an Option to a StringBuilder.  
     * @param sb the StringBuilder to append to
     * @param option the Option to append
     * @param required whether the Option is required or not
     */
    private void appendOption(StringBuilder sb, Option option, boolean required) {
        if (!required) {
            sb.append(OPTIONAL_BRACKET_OPEN);
        }
        if (option.getName() != null) {
            sb.append(OPTION_PREFIX).append(option.getName());
        } else {
            sb.append(LONG_OPTION_PREFIX).append(option.getLongName());
        }
        // if the Option has a value and a non blank arg name
        if (option.hasValue() && (option.getValueName() == null || !option.getValueName().isEmpty())) {
            sb.append(option.isWithEqualSign() ? '=' : LONG_OPTION_SEPARATOR);
            sb.append(ARG_BRACKET_OPEN).append(option.getValueName() != null ? option.getValueName() : getArgName()).append(ARG_BRACKET_CLOSE);
        }
        if (!required) {
            sb.append(OPTIONAL_BRACKET_CLOSE);
        }
    }

    private void appendArguments(StringBuilder sb, Arguments arguments) {
        if (!arguments.isRequired()) {
            sb.append(OPTIONAL_BRACKET_OPEN);
        }
        sb.append(ARG_BRACKET_OPEN);
        for (Iterator<String> it = arguments.keySet().iterator(); it.hasNext();) {
            String name = it.next();
            if (name.startsWith(ARG_BRACKET_OPEN) && name.endsWith(ARG_BRACKET_CLOSE)) {
                name = name.substring(1, name.length() - 1);
            }
            sb.append(name);
            if (it.hasNext()) {
                sb.append("|");
            }
        }
        sb.append(ARG_BRACKET_CLOSE);
        if (!arguments.isRequired()) {
            sb.append(OPTIONAL_BRACKET_CLOSE);
        }
    }

    /**
     * Print the help for the specified Options to the specified writer, 
     * using the specified width, left padding and description padding.
     * @param options the Options instance
     * @return the longest opt string's length
     */
    public int printOptions(Options options) {
        Collection<Option> optList = options.getAllOptions();
        StringBuilder sb = new StringBuilder();
        int leftWidth = 0;
        if (!options.isEmpty()) {
            leftWidth = renderOptions(sb, optList);
            if (sb.length() > 0) {
                if (StringUtils.hasLength(options.getTitle())) {
                    console.writeLine(options.getTitle());
                }
                console.writeLine(sb.toString());
            }
        }
        return leftWidth;
    }

    public void printArguments(List<Arguments> argumentsList, int leftWidth) {
        StringBuilder sb = new StringBuilder();
        for (Arguments arguments : argumentsList) {
            renderArguments(sb, arguments, leftWidth);
            if (sb.length() > 0) {
                if (StringUtils.hasLength(arguments.getTitle())) {
                    console.writeLine(arguments.getTitle());
                }
                console.write(sb.toString());
                sb.setLength(0);
            }
        }
    }

    /**
     * Print the specified text to the specified PrintWriter.
     * @param text the text to be written to the PrintWriter
     */
    public void printWrapped(String text) {
        printWrapped(0, text);
    }

    /**
     * Print the specified text to the specified PrintWriter.
     * @param nextLineTabStop the position on the next line for the first tab
     * @param text the text to be written to the PrintWriter
     */
    private void printWrapped(int nextLineTabStop, String text) {
        StringBuilder sb = new StringBuilder(text.length());
        renderWrappedTextBlock(sb, nextLineTabStop, text);
        console.writeLine(sb.toString());
    }

    /**
     * Render the specified Options and return the rendered Options
     * in a StringBuilder.
     * @param sb the StringBuilder to place the rendered Options into
     * @param options the command line Options
     * @return the longest opt string's length
     */
    private int renderOptions(StringBuilder sb, Collection<Option> options) {
        String lpad = OptionUtils.createPadding(getLeftPad());
        String dpad = OptionUtils.createPadding(getDescPad());

        // first create list containing only <lpad>-a,--aaa where 
        // -a is opt and --aaa is long opt; in parallel look for 
        // the longest opt string this list will be then used to 
        // sort options ascending
        int leftWidth = 0;
        List<StringBuilder> lineBufList = new ArrayList<>();
        Collection<Option> optList = options;
        if (optList.size() > 1 && getOptionComparator() != null) {
            List<Option> optList2 = new ArrayList<>(optList);
            optList2.sort(getOptionComparator());
            optList = optList2;
        }
        for (Option option : optList) {
            StringBuilder lineBuf = new StringBuilder();
            if (option.getName() == null) {
                lineBuf.append(lpad).append(LONG_OPTION_PREFIX).append(option.getLongName());
            } else {
                lineBuf.append(lpad).append(OPTION_PREFIX).append(option.getName());
                if (option.hasLongName()) {
                    lineBuf.append(", ").append(LONG_OPTION_PREFIX).append(option.getLongName());
                }
            }
            if (option.hasValue()) {
                String argName = option.getValueName();
                if (argName != null && argName.isEmpty()) {
                    // if the option has a blank arg name
                    lineBuf.append(' ');
                } else {
                    lineBuf.append(option.isWithEqualSign() ? '=' : LONG_OPTION_SEPARATOR);
                    lineBuf.append(ARG_BRACKET_OPEN).append(argName != null ? argName : getArgName()).append(ARG_BRACKET_CLOSE);
                }
            }
            lineBufList.add(lineBuf);
            leftWidth = (lineBuf.length() > leftWidth ? lineBuf.length() : leftWidth);
        }

        if (leftWidth > getMaxLeftWidth()) {
            leftWidth = getMaxLeftWidth();
        }

        int x = 0;
        for (Iterator<Option> it = optList.iterator(); it.hasNext();) {
            Option option = it.next();
            StringBuilder lineBuf = lineBufList.get(x++);
            if (lineBuf.length() <= leftWidth) {
                if (lineBuf.length() < leftWidth) {
                    lineBuf.append(OptionUtils.createPadding(leftWidth - lineBuf.length()));
                }
                lineBuf.append(dpad);
                if (option.getDescription() != null) {
                    lineBuf.append(option.getDescription());
                }
                renderWrappedText(sb, getWidth(), leftWidth + getDescPad(), lineBuf.toString());
            } else {
                sb.append(lineBuf).append(NEW_LINE);
                if (option.getDescription() != null) {
                    String line = OptionUtils.createPadding(leftWidth + getDescPad()) + option.getDescription();
                    renderWrappedText(sb, getWidth(), leftWidth + getDescPad(), line);
                }
            }
            if (it.hasNext()) {
                sb.append(NEW_LINE);
            }
        }
        return leftWidth;
    }

    private void renderArguments(StringBuilder sb, Arguments arguments, int leftWidth) {
        String lpad = OptionUtils.createPadding(getLeftPad());
        String dpad = OptionUtils.createPadding(getDescPad());

        int max = leftWidth;
        for (String arg : arguments.keySet()) {
            if (getLeftPad() + arg.length() > max) {
                max = getLeftPad() + arg.length();
            }
        }
        if (max > getMaxLeftWidth()) {
            max = getMaxLeftWidth();
        }

        for (Map.Entry<String, String> entry : arguments.entrySet()) {
            String arg = entry.getKey();
            String desc = entry.getValue();
            StringBuilder buf = new StringBuilder(lpad).append(arg);
            if (buf.length() <= max) {
                if (buf.length() < max) {
                    buf.append(OptionUtils.createPadding(max - buf.length()));
                }
                buf.append(dpad);
                if (desc != null) {
                    buf.append(desc);
                }
                renderWrappedText(sb, getWidth(), max + getDescPad(), buf.toString());
            } else {
                sb.append(buf).append(NEW_LINE);
                if (desc != null) {
                    String line = OptionUtils.createPadding(max + getDescPad()) + desc;
                    renderWrappedText(sb, getWidth(), max + getDescPad(), line);
                }
            }
            sb.append(NEW_LINE);
        }
    }

    /**
     * Render the specified text width a maximum width. This method differs
     * from renderWrappedText by not removing leading spaces after a new line.
     * @param sb the StringBuilder to place the rendered text into
     * @param nextLineTabStop the position on the next line for the first tab
     * @param text the text to be rendered
     */
    private void renderWrappedTextBlock(StringBuilder sb, int nextLineTabStop, String text) {
        try {
            BufferedReader in = new BufferedReader(new StringReader(text));
            String line;
            boolean firstLine = true;
            while ((line = in.readLine()) != null) {
                if (!firstLine) {
                    sb.append(NEW_LINE);
                } else {
                    firstLine = false;
                }
                renderWrappedText(sb, getWidth(), nextLineTabStop, line);
            }
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * Render the specified text and return the rendered Options
     * in a StringBuilder.
     * @param sb the StringBuilder to place the rendered text into
     * @param width the number of characters to display per line
     * @param nextLineTabStop the position on the next line for the first tab
     * @param text the text to be rendered
     */
    public static void renderWrappedText(StringBuilder sb, int width, int nextLineTabStop, String text) {
        int pos = OptionUtils.findWrapPos(text, width, 0);
        if (pos == -1) {
            sb.append(OptionUtils.rtrim(text));
            return;
        }
        sb.append(OptionUtils.rtrim(text.substring(0, pos))).append(NEW_LINE);
        if (nextLineTabStop >= width) {
            // stops infinite loop happening
            nextLineTabStop = 1;
        }
        // all following lines must be padded with nextLineTabStop space characters
        String padding = OptionUtils.createPadding(nextLineTabStop);
        String line = text;
        while (true) {
            line = padding + line.substring(pos).trim();
            pos = OptionUtils.findWrapPos(line, width, 0);
            if (pos == -1) {
                sb.append(line);
                return;
            }
            if (line.length() > width && pos == nextLineTabStop - 1) {
                pos = width;
            }
            sb.append(OptionUtils.rtrim(line.substring(0, pos))).append(NEW_LINE);
        }
    }

}
