/*
 * Copyright (c) 2008-2017 The Aspectran Project
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
package com.aspectran.console;

import com.aspectran.console.inout.ConsoleInout;
import com.aspectran.console.inout.ConsoleTerminatedException;
import com.aspectran.console.service.ConsoleAspectranService;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Console Command Handler.
 *
 * <p>Created: 2017. 6. 3.</p>
 */
public class ConsoleCommand {

    private static final Log log = LogFactory.getLog(ConsoleCommand.class);

    private final ConsoleAspectranService service;

    private final ConsoleInout consoleInout;

    public ConsoleCommand(ConsoleAspectranService service) {
        this.service = service;
        this.consoleInout = service.getConsoleInout();
    }

    public void perform() {
        try {
            loop:
            while (true) {
                String command = consoleInout.readCommand();
                if (command == null) {
                    continue;
                }
                command = command.trim();
                if (command.isEmpty()) {
                    continue;
                }

                switch (command) {
                    case "restart":
                        service.restart();
                        break;
                    case "pause":
                        service.pause();
                        break;
                    case "resume":
                        service.resume();
                        break;
                    case "desc on":
                        log.info("Description On");
                        service.setShowDescription(true);
                        break;
                    case "desc off":
                        log.info("Description Off");
                        service.setShowDescription(false);
                        break;
                    case "help":
                        service.printUsage();
                        break ;
                    case "clear":
                        consoleInout.clearScreen();
                        break ;
                    case "mem":
                        mem(false);
                        break;
                    case "gc":
                        mem(true);
                        break;
                    case "quit":
                        break loop;
                    default:
                        service.serve(command);
                        consoleInout.writeLine();
                }
            }
        } catch (ConsoleTerminatedException e) {
            // Will be shutdown
        } catch (Exception e) {
            log.error("An error occurred while executing the console command", e);
        } finally {
            if (service.isActive()) {
                log.info("Do not terminate this application while destroying all scoped beans");
            }
        }
    }

    /**
     * Displays memory usage.
     *
     * @param gc if true, perform a garbage collection
     */
    private void mem(boolean gc) throws Exception {
        long total = Runtime.getRuntime().totalMemory();
        long before = Runtime.getRuntime().freeMemory();

        consoleInout.setStyle("yellow");
        consoleInout.write("   Total memory: ");
        consoleInout.setStyle("fg:off");
        consoleInout.writeLine(StringUtils.convertToHumanFriendlyByteSize(total));
        consoleInout.setStyle("yellow");
        consoleInout.write("   Used memory: ");
        consoleInout.setStyle("fg:off");
        consoleInout.writeLine(StringUtils.convertToHumanFriendlyByteSize(total - before));
        if (gc) {
            // Let the finilizer finish its work and remove objects from its queue
            System.gc(); // asyncronous garbage collector might already run
            System.gc(); // to make sure it does a full gc call it twice
            System.runFinalization();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // do nothing
            }

            long after = Runtime.getRuntime().freeMemory();

            consoleInout.setStyle("yellow");
            consoleInout.write("   Free memory before GC: ");
            consoleInout.setStyle("fg:off");
            consoleInout.writeLine(StringUtils.convertToHumanFriendlyByteSize(before));
            consoleInout.setStyle("yellow");
            consoleInout.write("   Free memory after GC: ");
            consoleInout.setStyle("fg:off");
            consoleInout.writeLine(StringUtils.convertToHumanFriendlyByteSize(after));
            consoleInout.setStyle("yellow");
            consoleInout.write("   Memory gained with GC: ");
            consoleInout.setStyle("fg:off");
            consoleInout.writeLine(StringUtils.convertToHumanFriendlyByteSize(after - before));
        } else {
            consoleInout.setStyle("yellow");
            consoleInout.write("   Free memory: ");
            consoleInout.setStyle("fg:off");
            consoleInout.writeLine(StringUtils.convertToHumanFriendlyByteSize(before));
        }
        consoleInout.writeLine();
        consoleInout.offStyle();
    }

}
