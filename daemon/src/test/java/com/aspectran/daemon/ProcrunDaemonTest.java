package com.aspectran.daemon;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.io.IOException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProcrunDaemonTest {

    @Test
    @Order(1)
    void startProcrunDaemon() throws IOException {
        File baseDir = new File("./target/test-classes");
        String[] args = { baseDir.getCanonicalPath(), "config/aspectran-config.apon" };
        ProcrunDaemon.start(args, 500);
    }

    @Test
    @Order(2)
    void stopProcrunDaemon() {
        ProcrunDaemon.stop();
    }

}