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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Base class for {@link MessageSource} implementations, providing common support
 * infrastructure.
 *
 * <p>This class provides a foundation for message formatting, handling {@link java.text.MessageFormat}
 * instances and caching them for efficiency. It does not implement the concrete
 * message resolution methods defined in the {@link MessageSource} interface, such
 * as {@code getMessage}.
 *
 * <p>Key features include:
 * <ul>
 *     <li>Support for caching {@code MessageFormat} objects to avoid repeated parsing.</li>
 *     <li>An {@code alwaysUseMessageFormat} flag to enforce strict formatting rules
 *     for all messages.</li>
 *     <li>Template methods like {@link #renderDefaultMessage} and {@link #resolveArguments}
 *     that subclasses can override to customize behavior.</li>
 * </ul>
 *
 * <p>{@link AbstractMessageSource} derives from this class, providing concrete
 * {@code getMessage} implementations that delegate to a central template
 * method for message code resolution.
 *
 * <p>Created: 2016. 3. 12.</p>
 */
public class MessageSourceSupport {

    private static final MessageFormat INVALID_MESSAGE_FORMAT = new MessageFormat("");

    /** Logger available to subclasses */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean alwaysUseMessageFormat = false;

    /**
     * Cache to hold already generated MessageFormats per message.
     * Used for passed-in default messages. MessageFormats for resolved
     * codes are cached on a specific basis in subclasses.
     */
    private final Map<String, Map<Locale, MessageFormat>> messageFormatsPerMessage =
            new HashMap<>();

    /**
     * Set whether to always apply the MessageFormat rules, parsing even
     * messages without arguments.
     * <p>Default is "false": Messages without arguments are by default
     * returned as-is, without parsing them through MessageFormat.
     * Set this to "true" to enforce MessageFormat for all messages,
     * expecting all message texts to be written with MessageFormat escaping.</p>
     * <p>For example, MessageFormat expects a single quote to be escaped
     * as "''". If your message texts are all written with such escaping,
     * even when not defining argument placeholders, you need to set this
     * flag to "true". Else, only message texts with actual arguments
     * are supposed to be written with MessageFormat escaping.</p>
     * @param alwaysUseMessageFormat whether always use message format
     * @see java.text.MessageFormat
     */
    public void setAlwaysUseMessageFormat(boolean alwaysUseMessageFormat) {
        this.alwaysUseMessageFormat = alwaysUseMessageFormat;
    }

    /**
     * Return whether to always apply the MessageFormat rules, parsing even
     * messages without arguments.
     * @return whether always use message format
     */
    protected boolean isAlwaysUseMessageFormat() {
        return this.alwaysUseMessageFormat;
    }

    /**
     * Render the given default message String. The default message is
     * passed in as specified by the caller and can be rendered into
     * a fully formatted default message shown to the user.
     * <p>The default implementation passes the String to {@code formatMessage},
     * resolving any argument placeholders found in them. Subclasses may override
     * this method to plug in custom processing of default messages.</p>
     * @param defaultMessage the passed-in default message String
     * @param args array of arguments that will be filled in for params within the message, or {@code null} if none.
     * @param locale the Locale used for formatting
     * @return the rendered default message (with resolved arguments)
     * @see #formatMessage(String, Object[], java.util.Locale)
     */
    protected String renderDefaultMessage(String defaultMessage, Object[] args, Locale locale) {
        return formatMessage(defaultMessage, args, locale);
    }

    /**
     * Format the given message String, using cached MessageFormats.
     * By default invoked for passed-in default messages, to resolve
     * any argument placeholders found in them.
     * @param msg the message to format
     * @param args array of arguments that will be filled in for params within the message, or {@code null} if none
     * @param locale the Locale used for formatting
     * @return the formatted message (with resolved arguments)
     */
    protected String formatMessage(String msg, Object[] args, Locale locale) {
        if (msg == null || (!this.alwaysUseMessageFormat && (args == null || args.length == 0))) {
            return msg;
        }
        MessageFormat messageFormat = null;
        synchronized (this.messageFormatsPerMessage) {
            Map<Locale, MessageFormat> messageFormatsPerLocale = this.messageFormatsPerMessage.get(msg);
            if (messageFormatsPerLocale != null) {
                messageFormat = messageFormatsPerLocale.get(locale);
            } else {
                messageFormatsPerLocale = new HashMap<>();
                this.messageFormatsPerMessage.put(msg, messageFormatsPerLocale);
            }
            if (messageFormat == null) {
                try {
                    messageFormat = createMessageFormat(msg, locale);
                } catch (IllegalArgumentException ex) {
                    // invalid message format - probably not intended for formatting,
                    // rather using a message structure with no arguments involved
                    if (this.alwaysUseMessageFormat) {
                        throw ex;
                    }
                    // silently proceed with raw message if format not enforced
                    messageFormat = INVALID_MESSAGE_FORMAT;
                }
                messageFormatsPerLocale.put(locale, messageFormat);
            }
        }
        if (messageFormat == INVALID_MESSAGE_FORMAT) {
            return msg;
        }
        synchronized (messageFormat) {
            return messageFormat.format(args);
        }
    }

    /**
     * Create a MessageFormat for the given message and Locale.
     * @param msg the message to create a MessageFormat for
     * @param locale the Locale to create a MessageFormat for
     * @return the MessageFormat instance
     */
    protected MessageFormat createMessageFormat(String msg, Locale locale) {
        return new MessageFormat((msg != null ? msg : ""), locale);
    }

    /**
     * Template method for resolving argument objects.
     * <p>The default implementation simply returns the given argument array as-is.
     * Can be overridden in subclasses in order to resolve special argument types.</p>
     * @param args the original argument array
     * @param locale the Locale to resolve against
     * @return the resolved argument array
     */
    protected Object[] resolveArguments(Object[] args, Locale locale) {
        return args;
    }

}
