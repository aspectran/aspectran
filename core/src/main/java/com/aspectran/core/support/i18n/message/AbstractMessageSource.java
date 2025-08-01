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

import com.aspectran.utils.annotation.jsr305.NonNull;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;

/**
 * <p>This class is a clone of org.springframework.context.support.AbstractMessageSource</p>
 *
 * Abstract implementation of the {@link HierarchicalMessageSource} interface,
 * implementing common handling of message variants, making it easy
 * to implement a specific strategy for a concrete MessageSource.
 *
 * <p>Subclasses must implement the abstract {@link #resolveCode}
 * method. For efficient resolution of messages without arguments, the
 * {@link #resolveCodeWithoutArguments} method should be overridden
 * as well, resolving messages without a MessageFormat being involved.</p>
 *
 * <p><b>Note:</b> By default, message texts are only parsed through
 * MessageFormat if arguments have been passed in for the message. In case
 * of no arguments, message texts will be returned as-is. As a consequence,
 * you should only use MessageFormat escaping for messages with actual
 * arguments, and keep all other messages unescaped. If you prefer to
 * escape all messages, set the "alwaysUseMessageFormat" flag to "true".</p>
 *
 * <p>Created: 2016. 2. 8.</p>
 */
public abstract class AbstractMessageSource extends MessageSourceSupport implements HierarchicalMessageSource {

    private MessageSource parentMessageSource;

    private Properties commonMessages;

    private boolean useCodeAsDefaultMessage = false;

    @Override
    public void setParentMessageSource(MessageSource parent) {
        this.parentMessageSource = parent;
    }

    @Override
    public MessageSource getParentMessageSource() {
        return this.parentMessageSource;
    }

    /**
     * Specify locale-independent common messages, with the message code as key
     * and the full message String (may contain argument placeholders) as value.
     * @param commonMessages the common messages
     */
    public void setCommonMessages(Properties commonMessages) {
        this.commonMessages = commonMessages;
    }

    /**
     * Return a Properties object defining locale-independent common messages, if any.
     * @return the common messages
     */
    protected Properties getCommonMessages() {
        return this.commonMessages;
    }

    /**
     * Set whether to use the message code as default message instead of
     * throwing a NoSuchMessageException. Useful for development and debugging.
     * Default is "false".
     * <p>Note: In case of a MessageSourceResolvable with multiple codes
     * (like a FieldError) and a MessageSource that has a parent MessageSource,
     * do <i>not</i> activate "useCodeAsDefaultMessage" in the <i>parent</i>:
     * Else, you'll get the first code returned as message by the parent,
     * without attempts to check further codes.</p>
     * <p>To be able to work with "useCodeAsDefaultMessage" turned on in the parent,
     * AbstractMessageSource and AbstractApplicationContext contain special checks
     * to delegate to the internal {@link #getMessageInternal} method if available.
     * In general, it is recommended to just use "useCodeAsDefaultMessage" during
     * development and not rely on it in production in the first place, though.</p>
     * @param useCodeAsDefaultMessage whether use code as default message
     * @see #getMessage(String, Object[], Locale) #getMessage(String, Object[], Locale)
     */
    public void setUseCodeAsDefaultMessage(boolean useCodeAsDefaultMessage) {
        this.useCodeAsDefaultMessage = useCodeAsDefaultMessage;
    }

    /**
     * Return whether to use the message code as default message instead of
     * throwing a NoSuchMessageException. Useful for development and debugging.
     * Default is "false".
     * <p>Alternatively, consider overriding the {@link #getDefaultMessage}
     * method to return a custom fallback message for an unresolvable code.</p>
     * @return whether use code as default message
     * @see #getDefaultMessage(String) #getDefaultMessage(String)
     */
    protected boolean isUseCodeAsDefaultMessage() {
        return this.useCodeAsDefaultMessage;
    }

    @Override
    @NonNull
    public final String getMessage(String code, Locale locale) throws NoSuchMessageException {
        return getMessage(code, (Object[])null, locale);
    }

    @Override
    public final String getMessage(String code, String defaultMessage, Locale locale) throws NoSuchMessageException {
        return getMessage(code, null, defaultMessage, locale);
    }

    @Override
    @NonNull
    public final String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        String msg = getMessageInternal(code, args, locale);
        if (msg != null) {
            return msg;
        }
        String fallback = getDefaultMessage(code);
        if (fallback != null) {
            return fallback;
        }
        throw new NoSuchMessageException(code, locale);
    }

    @Override
    public final String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        String msg = getMessageInternal(code, args, locale);
        if (msg != null) {
            return msg;
        }
        if (defaultMessage == null) {
            String fallback = getDefaultMessage(code);
            if (fallback != null) {
                return fallback;
            }
        }
        return renderDefaultMessage(defaultMessage, args, locale);
    }

    /**
     * Resolve the given code and arguments as message in the given Locale,
     * returning {@code null} if not found. Does <i>not</i> fall back to
     * the code as default message. Invoked by {@code getMessage} methods.
     * @param code the code to lookup up, such as 'calculator.noRateSet'
     * @param args array of arguments that will be filled in for params within the message
     * @param locale the Locale in which to do the lookup
     * @return the resolved message, or {@code null} if not found
     * @see #getMessage(String, Object[], String, Locale) #getMessage(String, Object[], String, Locale)
     * @see #getMessage(String, Object[], Locale) #getMessage(String, Object[], Locale)
     * @see #setUseCodeAsDefaultMessage #setUseCodeAsDefaultMessage
     */
    protected String getMessageInternal(String code, Object[] args, Locale locale) {
        if (code == null) {
            return null;
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        if (!isAlwaysUseMessageFormat() && (args == null || args.length == 0)) {
            // Optimized resolution: no arguments to apply,
            // therefore no MessageFormat needs to be involved.
            // Note that the default implementation still uses MessageFormat;
            // this can be overridden in specific subclasses.
            String message = resolveCodeWithoutArguments(code, locale);
            if (message != null) {
                return message;
            }
        } else {
            if (args == null) {
                args = new Object[0];
            }
            MessageFormat messageFormat = resolveCode(code, locale);
            if (messageFormat != null) {
                synchronized (messageFormat) {
                    return messageFormat.format(args);
                }
            }
        }

        // Check locale-independent common messages for the given message code.
        Properties commonMessages = getCommonMessages();
        if (commonMessages != null) {
            String commonMessage = commonMessages.getProperty(code);
            if (commonMessage != null) {
                return formatMessage(commonMessage, args, locale);
            }
        }

        // Not found -> check parent, if any.
        return getMessageFromParent(code, args, locale);
    }

    /**
     * Try to retrieve the given message from the parent MessageSource, if any.
     * @param code the code to lookup up, such as 'calculator.noRateSet'
     * @param args array of arguments that will be filled in for params within the message
     * @param locale the Locale in which to do the lookup
     * @return the resolved message, or {@code null} if not found
     * @see #getParentMessageSource() #getParentMessageSource()
     */
    protected String getMessageFromParent(String code, Object[] args, Locale locale) {
        MessageSource parent = getParentMessageSource();
        if (parent != null) {
            if (parent instanceof AbstractMessageSource) {
                // Call internal method to avoid getting the default code back
                // in case of "useCodeAsDefaultMessage" being activated.
                return ((AbstractMessageSource) parent).getMessageInternal(code, args, locale);
            } else {
                // Check parent MessageSource, returning null if not found there.
                return parent.getMessage(code, args, null, locale);
            }
        }
        // Not found in parent either.
        return null;
    }

    /**
     * Return a fallback default message for the given code, if any.
     * <p>Default is to return the code itself if "useCodeAsDefaultMessage" is activated,
     * or return no fallback else. In case of no fallback, the caller will usually
     * receive a NoSuchMessageException from {@code getMessage}.</p>
     * @param code the message code that we couldn't resolve and that we didn't receive an explicit default message for
     * @return the default message to use, or {@code null} if none
     * @see #setUseCodeAsDefaultMessage #setUseCodeAsDefaultMessage
     */
    protected String getDefaultMessage(String code) {
        if (isUseCodeAsDefaultMessage()) {
            return code;
        } else {
            return null;
        }
    }

    /**
     * Subclasses can override this method to resolve a message without arguments
     * in an optimized fashion, i.e. to resolve without involving a MessageFormat.
     * <p>The default implementation <i>does</i> use MessageFormat, through
     * delegating to the {@link #resolveCode} method. Subclasses are encouraged
     * to replace this with optimized resolution.</p>
     * <p>Unfortunately, {@code java.text.MessageFormat} is not implemented
     * in an efficient fashion. In particular, it does not detect that a message
     * pattern doesn't contain argument placeholders in the first place. Therefore,
     * it is advisable to circumvent MessageFormat for messages without arguments.</p>
     * @param code the code of the message to resolve
     * @param locale the Locale to resolve the code for (subclasses are encouraged to support internationalization)
     * @return the message String, or {@code null} if not found
     * @see #resolveCode #resolveCode
     * @see java.text.MessageFormat
     */
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        MessageFormat messageFormat = resolveCode(code, locale);
        if (messageFormat != null) {
            synchronized (messageFormat) {
                return messageFormat.format(new Object[0]);
            }
        } else {
            return null;
        }
    }

    /**
     * Subclasses must implement this method to resolve a message.
     * <p>Returns a MessageFormat instance rather than a message String,
     * to allow for appropriate caching of MessageFormats in subclasses.
     * <p><b>Subclasses are encouraged to provide optimized resolution
     * for messages without arguments, not involving MessageFormat.</b>
     * See the {@link #resolveCodeWithoutArguments} javadoc for details.</p>
     * @param code the code of the message to resolve
     * @param locale the Locale to resolve the code for (subclasses are encouraged to support internationalization)
     * @return the MessageFormat for the message, or {@code null} if not found
     * @see #resolveCodeWithoutArguments(String, java.util.Locale) #resolveCodeWithoutArguments(String, java.util.Locale)
     */
    protected abstract MessageFormat resolveCode(String code, Locale locale);

}
