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
package com.aspectran.web.support.tags;

import com.aspectran.core.support.i18n.message.NoSuchMessageException;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.web.support.util.JavaScriptUtils;
import com.aspectran.web.support.util.TagUtils;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;

import java.io.IOException;
import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The {@code <message>} tag looks up a message in the scope of this page.
 * Messages are resolved using the ApplicationContext and thus support
 * internationalization.
 *
 * <p>Detects an HTML escaping setting, either on this tag instance, the page level,
 * or the {@code web.xml} level. Can also apply JavaScript escaping.</p>
 *
 * <p>If "code" isn't set or cannot be resolved, "text" will be used as default
 * message. Thus, this tag can also be used for HTML escaping of any texts.</p>
 *
 * <p>Message arguments can be specified via the {@link #setArguments(Object) arguments}
 * attribute or by using nested {@code <aspectran:argument>} tags.</p>
 *
 * <table>
 * <caption>Attribute Summary</caption>
 * <thead>
 * <tr>
 * <th>Attribute</th>
 * <th>Required?</th>
 * <th>Runtime Expression?</th>
 * <th>Description</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>arguments</td>
 * <td>false</td>
 * <td>true</td>
 * <td>Set optional message arguments for this tag, as a (comma-)delimited
 * String (each String argument can contain JSP EL), an Object array (used as
 * argument array), or a single Object (used as single argument).</td>
 * </tr>
 * <tr>
 * <td>argumentSeparator</td>
 * <td>false</td>
 * <td>true</td>
 * <td>The separator character to be used for splitting the arguments string
 * value; defaults to a 'comma' (',').</td>
 * </tr>
 * <tr>
 * <td>code</td>
 * <td>false</td>
 * <td>true</td>
 * <td>The code (key) to use when looking up the message.
 * If code is not provided, the text attribute will be used.</td>
 * </tr>
 * <tr>
 * <td>htmlEscape</td>
 * <td>false</td>
 * <td>true</td>
 * <td>Set HTML escaping for this tag, as boolean value.
 * Overrides the default HTML escaping setting for the current page.</td>
 * </tr>
 * <tr>
 * <td>javaScriptEscape</td>
 * <td>false</td>
 * <td>true</td>
 * <td>Set JavaScript escaping for this tag, as boolean value.
 * Default is false.</td>
 * </tr>
 * <tr>
 * <td>scope</td>
 * <td>false</td>
 * <td>true</td>
 * <td>The scope to use when exporting the result to a variable. This attribute
 * is only used when var is also set. Possible values are page, request, session
 * and application.</td>
 * </tr>
 * <tr>
 * <td>text</td>
 * <td>false</td>
 * <td>true</td>
 * <td>Default text to output when a message for the given code could not be
 * found. If both text and code are not set, the tag will output null.</td>
 * </tr>
 * <tr>
 * <td>var</td>
 * <td>false</td>
 * <td>true</td>
 * <td>The string to use when binding the result to the page, request, session
 * or application scope. If not specified, the result gets outputted to the writer
 * (i.e. typically directly to the JSP).</td>
 * </tr>
 * </tbody>
 * </table>
 */
public class MessageTag extends HtmlEscapingAwareTag implements ArgumentAware {

    @Serial
    private static final long serialVersionUID = -1411894383022986406L;

    /**
     * Default separator for splitting an arguments String: a comma (",").
     */
    public static final String DEFAULT_ARGUMENT_SEPARATOR = ",";

    @Nullable
    private String code;

    @Nullable
    private Object arguments;

    private String argumentSeparator = DEFAULT_ARGUMENT_SEPARATOR;

    private List<Object> nestedArguments = Collections.emptyList();

    @Nullable
    private String text;

    @Nullable
    private String var;

    private String scope = TagUtils.SCOPE_PAGE;

    private boolean javaScriptEscape = false;

    /**
     * Set the message code for this tag.
     */
    public void setCode(@Nullable String code) {
        this.code = code;
    }

    /**
     * Set optional message arguments for this tag, as a comma-delimited
     * String (each String argument can contain JSP EL), an Object array
     * (used as argument array), or a single Object (used as single argument).
     */
    public void setArguments(@Nullable Object arguments) {
        this.arguments = arguments;
    }

    /**
     * Set the separator to use for splitting an arguments String.
     * Default is a comma (",").
     * @see #setArguments
     */
    public void setArgumentSeparator(String argumentSeparator) {
        this.argumentSeparator = argumentSeparator;
    }

    @Override
    public void addArgument(@Nullable Object argument) throws JspTagException {
        this.nestedArguments.add(argument);
    }

    /**
     * Set the message text for this tag.
     */
    public void setText(@Nullable String text) {
        this.text = text;
    }

    /**
     * Set PageContext attribute name under which to expose
     * a variable that contains the resolved message.
     *
     * @see #setScope
     * @see jakarta.servlet.jsp.PageContext#setAttribute
     */
    public void setVar(@Nullable String var) {
        this.var = var;
    }

    /**
     * Set the scope to export the variable to.
     * Default is SCOPE_PAGE ("page").
     *
     * @see #setVar
     * @see com.aspectran.web.support.util.TagUtils#SCOPE_PAGE
     * @see jakarta.servlet.jsp.PageContext#setAttribute
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Set JavaScript escaping for this tag, as boolean value.
     * Default is "false".
     */
    public void setJavaScriptEscape(boolean javaScriptEscape) {
        this.javaScriptEscape = javaScriptEscape;
    }

    @Override
    protected final int doStartTagInternal() throws Exception {
        this.nestedArguments = new LinkedList<>();
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Resolves the message, escapes it if demanded,
     * and writes it to the page (or exposes it as variable).
     *
     * @see #resolveMessage()
     * @see com.aspectran.web.support.util.HtmlUtils#htmlEscape(String)
     * @see JavaScriptUtils#javaScriptEscape(String)
     * @see #writeMessage(String)
     */
    @Override
    public int doEndTag() throws JspException {
        try {
            // Resolve the unescaped message.
            String msg = resolveMessage();
            // HTML and/or JavaScript escape, if demanded.
            msg = htmlEscape(msg);
            msg = this.javaScriptEscape ? JavaScriptUtils.javaScriptEscape(msg) : msg;
            // Expose as variable, if demanded, else write to the page.
            if (this.var != null) {
                this.pageContext.setAttribute(this.var, msg, TagUtils.getScope(this.scope));
            } else {
                writeMessage(msg);
            }
            return EVAL_PAGE;
        } catch (IOException ex) {
            throw new JspTagException(ex.getMessage(), ex);
        } catch (NoSuchMessageException ex) {
            throw new JspTagException(getNoSuchMessageExceptionDescription(ex));
        }
    }

    @Override
    public void release() {
        super.release();
        this.arguments = null;
    }

    /**
     * Resolve the specified message into a concrete message String.
     * The returned message String should be unescaped.
     */
    protected String resolveMessage() throws JspException, NoSuchMessageException {
        if (this.code != null || this.text != null) {
            // We have a code or default text that we need to resolve.
            Object[] argumentsArray = resolveArguments(this.arguments);
            if (!this.nestedArguments.isEmpty()) {
                argumentsArray = appendArguments(argumentsArray, this.nestedArguments.toArray());
            }
            if (this.text != null) {
                // We have a fallback text to consider.
                String msg = getCurrentActivity().getTranslet().getMessage(this.code, argumentsArray, this.text);
                return (msg != null ? msg : "");
            } else {
                // We have no fallback text to consider.
                return getCurrentActivity().getTranslet().getMessage(this.code, argumentsArray);
            }
        }
        throw new JspTagException("No resolvable message");
    }

    private Object[] appendArguments(@Nullable Object[] sourceArguments, Object[] additionalArguments) {
        if (ObjectUtils.isEmpty(sourceArguments)) {
            return additionalArguments;
        }
        Object[] arguments = new Object[sourceArguments.length + additionalArguments.length];
        System.arraycopy(sourceArguments, 0, arguments, 0, sourceArguments.length);
        System.arraycopy(additionalArguments, 0, arguments, sourceArguments.length, additionalArguments.length);
        return arguments;
    }

    /**
     * Resolve the given arguments Object into an arguments array.
     * @param arguments the specified arguments Object
     * @return the resolved arguments as array
     * @see #setArguments
     */
    @Nullable
    protected Object[] resolveArguments(@Nullable Object arguments) {
        if (arguments instanceof String str) {
            String[] stringArray = StringUtils.split(str, this.argumentSeparator);
            if (stringArray.length == 1) {
                Object argument = stringArray[0];
                if (argument != null && argument.getClass().isArray()) {
                    return ObjectUtils.toObjectArray(argument);
                } else {
                    return new Object[] { argument };
                }
            } else {
                return stringArray;
            }
        } else if (arguments instanceof Object[] arr) {
            return arr;
        } else if (arguments instanceof Collection<?> collection) {
            return collection.toArray();
        } else if (arguments != null) {
            // Assume a single argument object.
            return new Object[] { arguments };
        } else {
            return null;
        }
    }

    /**
     * Write the message to the page.
     * <p>Can be overridden in subclasses, e.g. for testing purposes.</p>
     * @param msg the message to write
     * @throws IOException if writing failed
     */
    protected void writeMessage(String msg) throws IOException {
        super.pageContext.getOut().write(String.valueOf(msg));
    }

    /**
     * Return default exception message.
     */
    protected String getNoSuchMessageExceptionDescription(@NonNull NoSuchMessageException ex) {
        return ex.getMessage();
    }

}
