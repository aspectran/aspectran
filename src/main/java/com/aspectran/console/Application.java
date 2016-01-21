/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.console;

import com.aspectran.console.service.ConsoleAspectranService;

/**
 * The Aspectran Console Application.
 * 
 * Created by gulendol on 2016. 1. 17..
 */
public class Application {
	
	private static final String DEFAULT_ASPECTRAN_CONFIG_FILE = "aspectran-config.apon";

    public static void main(String[] args) {
        String aspectranConfigFile = null;

        if(args.length > 0)
            aspectranConfigFile = args[0];
        else
        	aspectranConfigFile = DEFAULT_ASPECTRAN_CONFIG_FILE;

        ConsoleAspectranService aspectranService = null;
        int exitStatus = 0;

        try {
            aspectranService = ConsoleAspectranService.newInstance(aspectranConfigFile);

            while(true) {
                String command = System.console().readLine("Command> ");

                if("restart".equals(command)) {
                    aspectranService.restart();
                    System.out.println("Aspectran service has been restarted.");
                } else if("pause".equals(command)) {
                    aspectranService.pause();
                    System.out.println("Aspectran service has been paused.");
                } else if("resume".equals(command)) {
                    aspectranService.resume();
                    System.out.println("Aspectran service has been resumed.");
                } else if("quit".equals(command)) {
                    System.out.println("Good bye.");
                    break;
                } else {
                    aspectranService.service(command);
                }

                System.out.println();
            }

        } catch(Exception e) {
            e.printStackTrace();
            exitStatus = 1;
        } finally {
            if(aspectranService != null) {
                System.out.println("Do not terminate the server while the all scoped bean destroying.");

                boolean cleanlyDestoryed = aspectranService.dispose();

                if (cleanlyDestoryed) {
                    System.out.println("Successfully destroyed ConsoleAspectranService.");
                } else {
                    System.out.println("ConsoleAspectranService were not destroyed cleanly.");
                }
            }
        }

        System.exit(exitStatus);
    }

}
