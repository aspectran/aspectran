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

import com.aspectran.shell.console.DefaultConsole;
import org.junit.Assert;
import org.junit.Test;

/**
 * <p>Created: 2017. 11. 12.</p>
 */
public class DefaultOptionParserTest {

    @Test
    public void testLS() {
        OptionParser parser = new DefaultOptionParser();

        Options options = new Options();
        options.addOption("a", "all", false, "do not hide entries starting with .");
        options.addOption("A", "almost-all", false, "do not list implied . and ..");
        options.addOption("b", "escape", false, "print octal escapes for nongraphic characters");
        options.addOption(Option.builder().longOpt("block-size")
                .desc("use SIZE-byte blocks")
                .hasArg()
                .argName("SIZE")
                .valueType(OptionValueType.INT)
                .build());
        options.addOption("B", "ignore-backups", false, "do not list implied entried ending with ~");
        options.addOption("c", false, "with -lt: sort by, and show, ctime (time of last "
                + "modification of file status information) with "
                + "-l:show ctime and sort by name otherwise: sort "
                + "by ctime");
        options.addOption("C", false, "list entries by columns");
        options.addOption(Option.builder("f")
                .desc("files")
                .hasArgs()
                .argName("FILE")
                .valueSeparator(' ')
                .build());
        options.addOption(Option.builder("z")
                .desc("files2")
                .hasArgs()
                .argName("FILE2")
                .valueSeparator(' ')
                .build());

        String[] args = new String[] {
                "--block-size=10",
                "-f",
                "A.txt",
                "B.txt",
                "-z=C.txt D.txt",
                "-b"
        };

        try {
            ParsedOptions parsedOptions = parser.parse(options, args);

            Assert.assertEquals(parsedOptions.getParsedValue("block-size"), Integer.valueOf(10));
            Assert.assertEquals(parsedOptions.getValues("f")[0], "A.txt");
            Assert.assertEquals(parsedOptions.getValues("f")[1], "B.txt");
            Assert.assertEquals(parsedOptions.getValues("z")[0], "C.txt");
            Assert.assertEquals(parsedOptions.getValues("z")[1], "D.txt");
            Assert.assertTrue(parsedOptions.hasOption("b"));

            System.out.println("------------------");
            for (String s : parsedOptions.getValues("f")) {
                System.out.println(s);
            }
            System.out.println("------------------");
            for (String s : parsedOptions.getValues("z")) {
                System.out.println(s);
            }
            System.out.println("------------------");

            HelpFormatter formatter = new HelpFormatter(new DefaultConsole());
            formatter.printHelp("ls [OPTION]... [FILE]...", options);
        } catch(OptionParserException exp) {
            System.out.println( "Unexpected exception: " + exp.getMessage() );
        }
    }

}