/*
 * Copyright (c) 2008-2022 The Aspectran Project
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

import com.aspectran.core.context.expr.ExpressionEvaluator;
import com.aspectran.core.context.expr.TokenEvaluation;
import com.aspectran.core.context.expr.TokenEvaluator;
import com.aspectran.core.lang.Nullable;
import com.aspectran.core.util.ObjectUtils;
import com.aspectran.web.support.util.JavaScriptUtils;
import com.aspectran.web.support.util.TagUtils;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.PageContext;

import java.io.IOException;

import static com.aspectran.web.support.tags.TokenTag.TOKEN_EVALUATOR_PAGE_ATTRIBUTE;

/**
 * The {@code <eval>} tag evaluates an Aspectran expression (SpEL) and either prints
 * the result or assigns it to a variable. Supports the standard JSP evaluation
 * context consisting of implicit variables and scoped attributes.
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
 * <p>Created: 2021/02/05</p>
 */
public class EvalTag extends HtmlEscapingAwareTag {

    @Nullable
    private String expression;

    @Nullable
    private String var;

    private int scope = PageContext.PAGE_SCOPE;

    private boolean javaScriptEscape = false;

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
            if (var != null) {
                Object result = ExpressionEvaluator.evaluate(this.expression, tokenEvaluator);
                super.pageContext.setAttribute(var, result, scope);
            } else {
                try {
                    Object result = ExpressionEvaluator.evaluate(this.expression, tokenEvaluator);
                    String str = ObjectUtils.getDisplayString(result);
                    str = htmlEscape(str);
                    if (javaScriptEscape) {
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
