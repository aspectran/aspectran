/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.support.i18n.message;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link ResourceBundleMessageSource} and related i18n support.
 *
 * <p>Created: 2016. 3. 13.</p>
 */
class ResourceBundleMessageSourceTest {

    @Test
    void testMessage() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBasename("locale.messages");

        Object[] args = new Object[] {"Aspectran"};
        String msg1 = messageSource.getMessage("hello", args, Locale.ENGLISH);
        String msg2 = messageSource.getMessage("hello", args, Locale.KOREAN);
        String msg3 = messageSource.getMessage("hello", args, Locale.JAPANESE);
        String msg4 = messageSource.getMessage("hello", args, Locale.FRENCH);
        String msg5 = messageSource.getMessage("hello", args, Locale.GERMAN);

        assertEquals("Hello, Aspectran!", msg1);
        assertEquals("안녕하세요, Aspectran!", msg2);
        assertEquals("こんにちは、 Aspectran!", msg3);
        assertEquals("Bonjour, Aspectran!", msg4);
        assertEquals("Guten Tag, Aspectran!", msg5);
    }

    @Test
    void testMessageWithoutArguments() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBasename("locale.messages");

        String msg = messageSource.getMessage("hello", Locale.ENGLISH);
        assertEquals("Hello, {0}!", msg);
    }

    @Test
    void testDefaultMessage() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("locale.messages");

        String msg1 = messageSource.getMessage("nonexistent.code", "Default Message", Locale.ENGLISH);
        assertEquals("Default Message", msg1);

        String msg2 = messageSource.getMessage("nonexistent.code", new Object[] {"World"}, "Default {0}", Locale.ENGLISH);
        assertEquals("Default World", msg2);
    }

    @Test
    void testNoSuchMessageException() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("locale.messages");

        assertThrows(NoSuchMessageException.class, () ->
                messageSource.getMessage("nonexistent.code", Locale.ENGLISH));

        assertThrows(NoSuchMessageException.class, () ->
                messageSource.getMessage("nonexistent.code", new Object[] {"Aspectran"}, Locale.ENGLISH));
    }

    @Test
    void testUseCodeAsDefaultMessage() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("locale.messages");
        messageSource.setUseCodeAsDefaultMessage(true);

        String msg = messageSource.getMessage("nonexistent.code", Locale.ENGLISH);
        assertEquals("nonexistent.code", msg);
    }

    @Test
    void testCommonMessages() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("locale.messages");

        Properties commonMessages = new Properties();
        commonMessages.setProperty("common.greeting", "Hi, {0}!");
        messageSource.setCommonMessages(commonMessages);

        String msg = messageSource.getMessage("common.greeting", new Object[] {"Aspectran"}, Locale.ENGLISH);
        assertEquals("Hi, Aspectran!", msg);
    }

    @Test
    void testParentMessageSource() {
        ResourceBundleMessageSource parentSource = new ResourceBundleMessageSource();
        parentSource.setBasename("locale.messages");

        DelegatingMessageSource childSource = new DelegatingMessageSource();
        childSource.setParentMessageSource(parentSource);

        assertEquals(parentSource, childSource.getParentMessageSource());

        String msg = childSource.getMessage("hello", new Object[] {"Child"}, Locale.ENGLISH);
        assertEquals("Hello, Child!", msg);

        String fallback = childSource.getMessage("not.found", "Fallback", Locale.ENGLISH);
        assertEquals("Fallback", fallback);
    }

    @Test
    void testMessageSourceResourceBundle() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("locale.messages");

        MessageSourceResourceBundle bundle = new MessageSourceResourceBundle(messageSource, Locale.ENGLISH);

        assertEquals(Locale.ENGLISH, bundle.getLocale());
        assertTrue(bundle.containsKey("hello"));
        assertFalse(bundle.containsKey("nonexistent.code"));
        assertEquals("Hello, {0}!", bundle.getObject("hello"));
        assertThrows(UnsupportedOperationException.class, bundle::getKeys);
    }

    @Test
    void testToString() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("locale.messages");

        String str = messageSource.toString();
        assertNotNull(str);
        assertTrue(str.contains("locale.messages"));

        DelegatingMessageSource delegatingSource = new DelegatingMessageSource();
        delegatingSource.setParentMessageSource(messageSource);
        assertNotNull(delegatingSource.toString());
    }

}
