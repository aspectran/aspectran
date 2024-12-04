package com.aspectran.utils.apon;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

class AponReaderCloseableTest {

    @Test
    void testReaderClose() throws AponParseException {
        String apon = """
            aspectran: {
                settings: {
                    transletNameSuffix: .job
                }
                bean: {
                    id: *
                    scan: test.**.*Schedule
                    mask: test.**.*
                    scope: singleton
                }
            }
            """;

        StringReader reader = new StringReader(apon);
        try (AponReaderCloseable aponReader = new AponReaderCloseable(reader)) {
            aponReader.read();
        }
    }

}
