package com.aspectran.utils.apon;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AponWriterCloseableTest {

    @Test
    void testWriterClose() throws IOException {
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

        Parameters ps = AponReader.read(apon);
        String expected = apon.replace("\n", AponFormat.SYSTEM_NEW_LINE);

        StringWriter writer = new StringWriter();
        try (AponWriterCloseable aponWriter = new AponWriterCloseable(writer)) {
            aponWriter.indentString("    ");
            aponWriter.write(ps);
            assertEquals(expected, writer.toString());
        }
    }

}
