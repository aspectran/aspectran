/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import org.junit.Test;

import java.util.Locale;

import static junit.framework.TestCase.assertEquals;

/**
 * <p>Created: 2016. 3. 13.</p>
 */
public class ResourceBundleMessageSourceTest {

    @Test
    public void testMessage() throws Exception {
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

}