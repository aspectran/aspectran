package com.aspectran.shell.command;

import com.aspectran.shell.service.ShellAspectranService;

public abstract class AbstractCommand implements Command {

    private ShellAspectranService service;

    public AbstractCommand(ShellAspectranService service) {
        this.service = service;
    }

    public ShellAspectranService getService() {
        return service;
    }

}
