package com.aspectran.core.util.thread;

import java.util.concurrent.TimeUnit;

public interface Scheduler {

    interface Task {

        boolean cancel();

    }

    Task schedule(Runnable task, long delay, TimeUnit unit);

}
