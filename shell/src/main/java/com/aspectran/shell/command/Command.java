package com.aspectran.shell.command;

import java.util.Map;

/**
 * The Command interface is there to allow Commander to delegate tasks.
 */
public interface Command {

    /**
     * This method will be called as the starting point to execute the logic
     * for the action mapped to this command.
     *
     * @param args
     * @return
     */
    String execute(String[] args) throws Exception;

    /**
     * This method returns an instance of Command.Descriptor.
     * The descriptor is meta information about the command.
     *
     * @return
     */
    Command.Descriptor getDescriptor();

    /**
     * An interface that can be used to describe the the functionality of the
     * command implementation.  This is a very important concept in a text-driven
     * environment such as a command-line user interface.
     */
    interface Descriptor {

        /**
         * The purpose of the namespace is to provide an identifier to group
         * commands without relying on class name or other convoluted approaches
         * to group commands.
         *
         * @return the command's namespace
         */
        String getNamespace();

        /**
         * Implementation of this method should return a simple string (with no spaces)
         * that identifies the action mapped to this command.
         *
         * @return the name of the action mapped to this command.
         */
        String getName();

        /**
         * This method should return a descriptive text about the command
         * it is attached to.
         *
         * @return
         */
        String getDescription();

        /**
         * Implementation of this method should return helpful hint on how
         * to use the associated command and further description of options that
         * are supported by the command.
         *
         * @return
         */
        String getUsage();

        /**
         * Use this method is to provide a map of the command arguments.
         *
         * @return Map<String, String> key is argument, value = description of arg.
         */
        Map<String, String> getArguments();

    }

}
