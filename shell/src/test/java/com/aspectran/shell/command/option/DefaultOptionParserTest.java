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

import com.aspectran.shell.console.DefaultShellConsole;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>Created: 2017. 11. 12.</p>
 */
class DefaultOptionParserTest {

    @Test
    void testOptions() {
        OptionParser parser = new DefaultOptionParser();

        Options options = new Options();
        options.addOption(Option.builder("a")
                .longName("all")
                .desc("do not hide entries starting with .")
                .build());
        options.addOption(Option.builder("A")
                .longName("almost-all")
                .desc("do not list implied . and ..")
                .build());
        options.addOption(Option.builder("b")
                .longName("escape")
                .desc("print octal escapes for nongraphic characters")
                .build());
        options.addOption(Option.builder("B")
                .longName("ignore-backups")
                .desc("do not list implied entried ending with ~")
                .build());
        options.addOption(Option.builder("c")
                .desc("with -lt: sort by, and show, ctime (time of last "
                        + "modification of file status information) with "
                        + "-l:show ctime and sort by name otherwise: sort "
                        + "by ctime")
                .build());
        options.addOption(Option.builder("C")
                .desc("list entries by columns")
                .build());
        options.addOption(Option.builder().longName("block-size")
                .desc("use SIZE-byte blocks")
                .valueName("SIZE")
                .valueType(OptionValueType.INT)
                .withEqualSign()
                .build());
        options.addOption(Option.builder("f")
                .desc("files")
                .hasValues()
                .valueName("file")
                .build());
        options.addOption(Option.builder("z")
                .desc("files2")
                .hasValues()
                .valueName("files")
                .build());
        options.addOption(Option.builder().longName("target")
                .desc("target")
                .valueName("target")
                .withEqualSign()
                .optionalValue()
                .build());
        options.addOption(Option.builder("long_but_short_option_name")
                .desc("Long but short option name Long but short option name")
                .valueName("pause")
                .withEqualSign()
                .optionalValue()
                .build());

        String[] args = new String[] {
                "--block-size=10",
                "-f",
                "A.txt",
                "B.txt",
                "-z",
                "C.txt",
                "D.txt",
                "-b",
                "--target",
                "args"
        };

        Arguments arguments = new Arguments();
        arguments.put("<command>", "Print list of all aspects or those filtered by the given name or those filtered by the given name");
        arguments.put("<long_command_name>", "Print list of all aspects or those filtered by the given name or those filtered by the given name");
        List<Arguments> argumentsList = new ArrayList<>();
        argumentsList.add(arguments);

        try {
            ParsedOptions parsedOptions = parser.parse(options, args);

            HelpFormatter formatter = new HelpFormatter(new DefaultShellConsole());
            int leftPadSize = formatter.printOptions(options);
            formatter.printArguments(argumentsList, leftPadSize);

            assertEquals(parsedOptions.getTypedValue("block-size"), Integer.valueOf(10));
            assertEquals("A.txt", parsedOptions.getValues("f")[0]);
            assertEquals("B.txt", parsedOptions.getValues("f")[1]);
            assertEquals("C.txt", parsedOptions.getValues("z")[0]);
            assertEquals("D.txt", parsedOptions.getValues("z")[1]);
            assertTrue(parsedOptions.hasOption("b"));
            assertNull(parsedOptions.getValues("target"));
        } catch (OptionParserException exp) {
            System.out.println( "Unexpected exception: " + exp.getMessage() );
        }
    }

    @Test
    void testOptionGroup() {
        OptionParser parser = new DefaultOptionParser();

        OptionGroup og1 = new OptionGroup();
        og1.addOption(Option.builder("a")
                .longName("all")
                .desc("do not hide entries starting with .")
                .build());
        og1.addOption(Option.builder("A")
                .longName("almost-all")
                .desc("do not list implied . and ..")
                .build());

        OptionGroup og2 = new OptionGroup();
        og2.addOption(Option.builder("b")
                .longName("escape")
                .desc("print octal escapes for nongraphic characters")
                .build());
        og2.addOption(Option.builder("B")
                .longName("ignore-backups")
                .desc("do not list implied entries ending with ~")
                .build());

        OptionGroup og3 = new OptionGroup();
        og3.addOption(Option.builder("c")
                .desc("with -lt: sort by, and show, ctime (time of last "
                        + "modification of file status information) with "
                        + "-l:show ctime and sort by name otherwise: sort "
                        + "by ctime")
                .build());
        og3.addOption(Option.builder("C")
                .desc("list entries by columns")
                .build());

        String[] args = new String[] {
                "-a",
                "-b",
                "-c"
        };

        Options options = new Options();
        options.setTitle("Grouped options");
        options.addOptionGroup(og1);
        options.addOptionGroup(og2);
        options.addOptionGroup(og3);

        try {
            ParsedOptions parsedOptions = parser.parse(options, args);

            HelpFormatter formatter = new HelpFormatter(new DefaultShellConsole());
            formatter.printOptions(options);

            assertTrue(parsedOptions.hasOption("a"));
            assertTrue(parsedOptions.hasOption("b"));
            assertTrue(parsedOptions.hasOption("c"));
        } catch (OptionParserException exp) {
            System.out.println( "Unexpected exception: " + exp.getMessage() );
        }
    }

    @Test
    void testOptions2() {
        OptionParser parser = new DefaultOptionParser();

        Options options = new Options();
        options.addOption(Option.builder("p")
                .longName("password")
                .desc("password")
                .valueName("password")
                .withEqualSign()
                .optionalValue()
                .build());
        options.addOption(Option.builder("h")
                .longName("help")
                .desc("Display help")
                .build());

        String[] args = new String[] {
                "-password=yes",
                "-password=no",
                "-h",
                "-help",
                "arg1",
                "arg2"
        };

        try {
            ParsedOptions parsedOptions = parser.parse(options, args, true);

            HelpFormatter formatter = new HelpFormatter(new DefaultShellConsole());
            formatter.printOptions(options);

            assertEquals("[yes, no]", Arrays.toString(parsedOptions.getValues("password")));
            assertTrue(parsedOptions.hasOption("help"));
            assertEquals("[arg1, arg2]", Arrays.toString(parsedOptions.getArgs()));
        } catch (OptionParserException exp) {
            System.out.println( "Unexpected exception: " + exp.getMessage() );
        }
    }

    @Test
    void testRequiredOption() {
        OptionParser parser = new DefaultOptionParser();
        Options options = new Options();
        options.addOption(Option.builder("r").required().desc("required option").build());

        String[] args = new String[] {};

        try {
            parser.parse(options, args);
        } catch (MissingOptionException e) {
            assertEquals("Missing required option: r", e.getMessage());
        } catch (OptionParserException e) {
            System.out.println("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void testTypedValues() throws OptionParserException {
        OptionParser parser = new DefaultOptionParser();
        Options options = new Options();
        options.addOption(Option.builder("i").hasValue().valueType(OptionValueType.INT).build());
        options.addOption(Option.builder("b").hasValue().valueType(OptionValueType.BOOLEAN).build());
        options.addOption(Option.builder("l").hasValue().valueType(OptionValueType.LONG).build());
        options.addOption(Option.builder("f").hasValue().valueType(OptionValueType.FLOAT).build());
        options.addOption(Option.builder("d").hasValue().valueType(OptionValueType.DOUBLE).build());

        String[] args = new String[] {
                "-i", "100",
                "-b", "true",
                "-l", "1234567890123",
                "-f", "10.5",
                "-d", "20.123456789"
        };

        ParsedOptions parsedOptions = parser.parse(options, args);

        assertEquals(Integer.valueOf(100), parsedOptions.getTypedValue("i"));
        assertEquals(Boolean.TRUE, parsedOptions.getTypedValue("b"));
        assertEquals(Long.valueOf(1234567890123L), parsedOptions.getTypedValue("l"));
        assertEquals(Float.valueOf(10.5f), parsedOptions.getTypedValue("f"));
        assertEquals(Double.valueOf(20.123456789), parsedOptions.getTypedValue("d"));
    }

    @Test
    void testOptionGroupExclusivity() {
        OptionParser parser = new DefaultOptionParser();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("a").desc("option a").build());
        group.addOption(Option.builder("b").desc("option b").build());
        options.addOptionGroup(group);

        String[] args = new String[] { "-a", "-b" };

        try {
            parser.parse(options, args);
        } catch (AlreadySelectedException e) {
            assertTrue(e.getMessage().contains("The option 'b' was specified but an option from this group has already been selected: 'a'"));
        } catch (OptionParserException e) {
            System.out.println("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void testUnrecognizedOption() {
        OptionParser parser = new DefaultOptionParser();
        Options options = new Options();
        options.addOption(Option.builder("a").desc("option a").build());

        String[] args = new String[] { "-z" };

        try {
            parser.parse(options, args);
        } catch (UnrecognizedOptionException e) {
            assertEquals("Unrecognized option: -z", e.getMessage());
        } catch (OptionParserException e) {
            System.out.println("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void testPartialMatching() throws OptionParserException {
        OptionParser parser = new DefaultOptionParser(true); // Enable partial matching
        Options options = new Options();
        options.addOption(Option.builder().longName("debug").desc("debug option").build());
        options.addOption(Option.builder().longName("verbose").desc("verbose option").build());

        String[] args = new String[] { "--deb" };

        ParsedOptions parsedOptions = parser.parse(options, args);

        assertTrue(parsedOptions.hasOption("debug"));
    }

}
