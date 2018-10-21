package com.aspectran.core.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static junit.framework.TestCase.assertEquals;

/**
 * <p>Created: 21/10/2018</p>
 */
public class PropertiesLoaderUtilsTest {

    private String oldPassword;

    @Before
    public void saveProperties() {
        oldPassword = System.getProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY);
    }

    @After
    public void restoreProperties() {
        if (oldPassword == null) {
            System.clearProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY);
        } else {
            System.setProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY, oldPassword);
        }
    }

    @Test
    public void testLoadProperties() throws IOException {
        String password = "abcd1234()";
        System.setProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY, password);
        Properties props = PropertiesLoaderUtils.loadProperties("test.encrypted.properties");
        assertEquals(props.getProperty("name"), "Aspectran");
        assertEquals(props.getProperty("passwd"), "1234");
    }

}