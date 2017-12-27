package com.aspectran.daemon;

import com.aspectran.daemon.service.DaemonService;

public interface Daemon {

    DaemonService getService();

}
