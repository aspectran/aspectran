package com.aspectran.shell.command;

import com.aspectran.shell.service.ShellAspectranService;
import com.beust.jcommander.JCommander;

public abstract class AbstractCommand implements Command {

    private ShellAspectranService service;

    private JCommander jc;

    public AbstractCommand(ShellAspectranService service) {
        this.service = service;
    }

    public ShellAspectranService getService() {
        return service;
    }

    protected <T> T parse(String[] args, T options) {
        jc = JCommander.newBuilder()
                .addObject(options)
                .args(args)
                .build();
        jc.setProgramName(getDescriptor().getName());
        return options;
    }

    protected void printUsage() {
        if (jc != null) {
            jc.usage();
        }
    }

}
