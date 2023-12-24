/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.context.expr.TokenEvaluation;
import com.aspectran.core.context.expr.TokenEvaluator;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.web.support.util.JavaScriptUtils;
import com.aspectran.web.support.util.TagUtils;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.PageContext;

import java.io.IOException;

/**
 * The {@code <token>} tag evaluates a Token expression and either prints
 * the result or assigns it to a variable.
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
 * <td>type</td>
 * <td>true</td>
 * <td>true</td>
 * <td>The type of the token to evaluate.</td>
 * </tr>
 * <tr>
 * <td>expression</td>
 * <td>true</td>
 * <td>true</td>
 * <td>The expression to evaluate.</td>
 * </tr>
 * <tr>
 * <td>htmlEscape</td>
 * <td>false</td>
 * <td>true</td>
 * <td>Set HTML escaping for this tag, as a boolean value.
 * Overrides the default HTML escaping setting for the current page.</td>
 * </tr>
 * <tr>
 * <td>javaScriptEscape</td>
 * <td>false</td>
 * <td>true</td>
 * <td>Set JavaScript escaping for this tag, as a boolean value.
 * Default is false.</td>
 * </tr>
 * <tr>
 * <td>scope</td>
 * <td>false</td>
 * <td>true</td>
 * <td>The scope for the var. 'application', 'session', 'request' and 'page'
 * scopes are supported. Defaults to page scope. This attribute has no effect
 * unless the var attribute is also defined.</td>
 * </tr>
 * <tr>
 * <td>var</td>
 * <td>false</td>
 * <td>true</td>
 * <td>The name of the variable to export the evaluation result to.
 * If not specified the evaluation result is converted to a String and written
 * as output.</td>
 * </tr>
 * </tbody>
 * </table>
 *
 * <p>Created: 2020/05/31</p>
 */
public class TokenTag extends HtmlEscapingAwareTag {

    private static final long serialVersionUID = -3988228472260819648L;

    /**
     * {@link jakarta.servlet.jsp.PageContext} attribute for the
     * page-level {@link TokenEvaluator} instance.
     */
    protected static final String TOKEN_EVALUATOR_PAGE_ATTRIBUTE =
            "com.aspectran.web.support.tags.TOKEN_EVALUATOR";

    @Nullable
    private String type;

    @Nullable
    private String expression;

    @Nullable
    private String var;

    private int scope = PageContext.PAGE_SCOPE;

    private boolean javaScriptEscape = false;

    /**
     * Set the toke type to evaluate.
     */
    public void setType(@Nullable String type) {
        this.type = type;
    }

    /**
     * Set the expression to evaluate.
     */
    public void setExpression(@Nullable String expression) {
        this.expression = expression;
    }

    /**
     * Set the variable name to expose the evaluation result under.
     * Defaults to rendering the result to the current JspWriter.
     */
    public void setVar(@Nullable String var) {
        this.var = var;
    }

    /**
     * Set the scope to export the evaluation result to.
     * This attribute has no meaning unless var is also defined.
     */
    public void setScope(String scope) {
        this.scope = TagUtils.getScope(scope);
    }

    /**
     * Set JavaScript escaping for this tag, as boolean value.
     * Default is "false".
     */
    public void setJavaScriptEscape(boolean javaScriptEscape) {
        this.javaScriptEscape = javaScriptEscape;
    }

    @Override
    public int doStartTagInternal() throws JspException {
        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {
        TokenEvaluator tokenEvaluator =
                (TokenEvaluator)super.pageContext.getAttribute(TOKEN_EVALUATOR_PAGE_ATTRIBUTE);
        if (tokenEvaluator == null) {
            tokenEvaluator = new TokenEvaluation(getCurrentActivity());
            super.pageContext.setAttribute(TOKEN_EVALUATOR_PAGE_ATTRIBUTE, tokenEvaluator);
        }
        try {
            if (this.type == null) {
                throw new IllegalArgumentException("No token type set");
            }
            TokenType tokenType = TokenType.resolve(this.type);
            if (tokenType == null) {
                throw new IllegalArgumentException("Unknown token type: " + this.type);
            }
            Token[] tokens = TokenParser.parse(Token.format(tokenType, this.expression));
            if (this.var != null) {
                Object result = tokenEvaluator.evaluate(tokens);
                super.pageContext.setAttribute(this.var, result, this.scope);
            } else {
                try {
                    Object result = tokenEvaluator.evaluate(tokens);
                    String str = ObjectUtils.getDisplayString(result);
                    str = htmlEscape(str);
                    if (this.javaScriptEscape) {
                        str = JavaScriptUtils.javaScriptEscape(str);
                    }
                    super.pageContext.getOut().print(str);
                } catch (IOException ex) {
                    throw new JspException(ex);
                }
            }
            return EVAL_PAGE;
        } catch (Exception ex) {
            throw new JspTagException(ex.getMessage(), ex);
        }
    }

}
