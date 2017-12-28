package com.aspectran.daemon;

import com.aspectran.daemon.command.CommandPoller;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.DaemonCommander;
import com.aspectran.daemon.service.DaemonService;

public interface Daemon {

    DaemonService getService();

    DaemonCommander getCommander();

    CommandPoller getCommandPoller();

    CommandRegistry getCommandRegistry();

}
