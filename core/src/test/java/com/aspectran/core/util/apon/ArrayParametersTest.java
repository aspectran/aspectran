package com.aspectran.core.util.apon;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2019-06-28</p>
 */
class ArrayParametersTest {

    @Test
    void readArrayParameters() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  param1: 111\n");
        sb.append("  param2: 222\n");
        sb.append("}\n");
        sb.append("{\n");
        sb.append("  param3: 333\n");
        sb.append("  param4: 444\n");
        sb.append("}\n");

        ArrayParameters arrayParameters = new ArrayParameters(sb.toString());
        String s1 = arrayParameters.toString();

        AponReader aponReader = new AponReader(sb.toString());
        String s2 = aponReader.read(new ArrayParameters()).toString();

        assertEquals(s1, s2);
    }

}
